/*
 * Copyright (C) 2021 Nick Iacullo
 *
 * This file is part of Hourglass.
 *
 * Hourglass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hourglass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hourglass.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.lavabucket.hourglass.config;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.utils.ReflectionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import net.minecraftforge.fmllegacy.network.ConfigSync;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.FMLHandshakeMessages.S2CConfigData;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

/**
 * A very quick and very dirty method of synchronizing server configuration with clients.
 *
 * Forge syncs server configs with clients by default on login, but leaves clients out-of-date if
 * the configuration changes after login. This class handles server configuration changes and
 * pushes them to the clients using the same methods Forge uses during the initial login sync.
 */
public class ConfigSynchronizer {

    /** Network channel name. */
    private static final ResourceLocation NETWORK_CHANNEL_NAME =
            new ResourceLocation(Hourglass.MOD_ID, "config_sync");
    /** Current protocol version used to determine compatibility with other versions of this mod. */
    private static final String PROTOCOL_VERSION = "1.0";
    /** Reference to reflected field used in this class. */
    private static final Field CONFIGS_BY_MOD_FIELD = ReflectionHelper.CONFIGS_BY_MOD_FIELD;
    /** Class logger. */
    private static final Logger LOGGER = LogManager.getLogger();
    /** Log4j marker. */
    private static final Marker MARKER = Hourglass.MARKER;

    /** An ID of {@value #CONFIG_MESSAGE_ID} for the {@code ConfigSynchronizer} message. */
    protected static final byte CONFIG_MESSAGE_ID = 1;

    /** The set of mod IDs who are registered for automatic syncing. */
    private final Set<String> registeredMods;
    /** The network channel. */
    private final SimpleChannel channel;

    /**
     * Creates a new instance.
     * @param eventBus  the mod event bus
     */
    public ConfigSynchronizer(IEventBus eventBus) {
        this.registeredMods = new HashSet<>();
        this.channel = createChannel();
        registerMessages();
        eventBus.addListener(this::onConfigReload);
    }

    /**
     * Supplies the network channel for this object.
     * Called once by the constructor.
     *
     * @return the network channel for use by this object
     */
    protected SimpleChannel createChannel() {
        return NetworkRegistry.newSimpleChannel(
                NETWORK_CHANNEL_NAME,
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);
    }

    /**
     * Registers this object's network messages to the network channel.
     * Called once by the constructor.
     *
     * <p>This method registers one message to the channel that has an ID of
     * {@value #CONFIG_MESSAGE_ID} and a packet class of {@link S2CConfigData}.
     */
    protected void registerMessages() {
        getChannel().registerMessage(CONFIG_MESSAGE_ID,
                S2CConfigData.class,
                ConfigSynchronizer::encode,
                ConfigSynchronizer::decode,
                ConfigSynchronizer::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Listens for a config reload event and initializes a configuration sync if the server config
     * was changed.
     *
     * @param event  the config reload event, provided by the event bus
     */
    private void onConfigReload(final Reloading event) {
        final ModConfig config = event.getConfig();
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final String modId = config.getModId();
        if (server != null
                && server.isRunning()
                && isRegistered(modId)
                && config.getType() == ModConfig.Type.SERVER) {

            LOGGER.debug(MARKER, "Synchronizing " + modId + " server config with clients.");
            try {
                sync(config);
            } catch (Exception e) {
                LOGGER.error(MARKER,
                        "Failed to automatically synchronize server config with clients.", e);
            }
        }
    }

    /**
     * Registers the mod with the given mod ID to receive automatic server config syncing.
     * @param modId  the mod ID of the mod to register
     * @return true if the mod ID was not already registered, false otherwise
     */
    public boolean registerMod(String modId) {
        return registeredMods.add(modId);
    }

    /**
     * Unregisters the mod with the given mod ID from receiving automatic server config syncing.
     * @param modId  the mod ID of the mod to unregister
     * @return true if the mod ID had been registered, false otherwise
     */
    public boolean unregisterMod(String modId) {
        return registeredMods.remove(modId);
    }

    /**
     * Returns true if the mod with the given mod ID is registered for automatic server config
     * syncing, false otherwise
     * @param modId  the mod ID of the mod to check
     * @return true if the mod with the given mod ID is registered for syncing, false otherwise
     */
    public boolean isRegistered(String modId) {
        return registeredMods.contains(modId);
    }

    /** {@return this object's network channel} */
    public SimpleChannel getChannel() {
        return channel;
    }

    /**
     * Syncs the server config of the mod with a mod ID of {@code modId} to all clients connected
     * to the running server.
     *
     * @throws IllegalStateException if a server is not running
     * @throws IllegalArgumentException if a server config for {@code modId} does not exist
     * @param modId  the mod ID of the mod with the server config to sync
     */
    @SuppressWarnings("unchecked")
    public void sync(final String modId) {
        final ConfigTracker tracker = ConfigTracker.INSTANCE;
        final Map<String, Map<Type, ModConfig>> configMap;

        try {
            configMap = (Map<String, Map<Type, ModConfig>>) CONFIGS_BY_MOD_FIELD.get(tracker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync server config with clients.", e);
        }
        final Map<Type, ModConfig> modConfigMap = configMap.get(modId);
        if (modConfigMap == null) {
            throw new IllegalArgumentException("No server config found for mod ID " + modId);
        }
        final ModConfig modConfig = modConfigMap.get(ModConfig.Type.SERVER);
        if (modConfig == null) {
            throw new IllegalArgumentException("No server config found for mod ID " + modId);
        }

        sync(modConfig);
    }

    /**
     * Syncs the server config {@code modConfig} with all clients currently connected to the server.
     * @throws IllegalStateException if a server is not running
     * @throws IllegalArgumentException if {@code modConfig} is not a server config
     * @param modConfig  the server config to sync
     */
    public void sync(final ModConfig modConfig) {
        if (modConfig == null) {
            throw new IllegalArgumentException("modConfig cannot be null.");
        } else if (modConfig.getType() != ModConfig.Type.SERVER) {
            throw new IllegalArgumentException("modConfig must be a server config");
        }

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null || !server.isRunning()) {
            throw new IllegalStateException(
                    "Cannot sync server config with clients: server is not running.");
        }

        Path configPath = modConfig.getFullPath();
        String configFileName = modConfig.getFileName();
        byte[] configData;

        try {
            configData = Files.readAllBytes(configPath);
        } catch (Exception e) {
            LOGGER.error(MARKER, "Failed to sync server config with clients.", e);
            return;
        }

        S2CConfigData message = new S2CConfigData(configFileName, configData);
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Encodes an S2CConfigData config data object and writes it to a PacketBuffer.
     *
     * @param config  the config data object to encode
     * @param buffer  the buffer to write the object to
     */
    private static void encode(S2CConfigData config, FriendlyByteBuf buffer) {
        buffer.writeUtf(config.getFileName());
        buffer.writeByteArray(config.getBytes());
    }

    /**
     * Attempts to decode an S2CConfigData object from the given PacketBuffer.
     *
     * @param buffer  the buffer to read the object data from
     * @return the config data object that was read from the buffer
     */
    private static S2CConfigData decode(FriendlyByteBuf buffer) {
        return S2CConfigData.decode(buffer);
    }

    /**
     * Handle a received configuration data packet. Applies the new configuration using the same
     * method Forge uses when it receives this packet at server login.
     *
     * @param configData  the configuration data packet
     * @param context  the network message context
     */
    private static void handle(S2CConfigData configData, Supplier<Context> context) {
        ConfigSync.INSTANCE.receiveSyncedConfig(configData, context);
        context.get().setPacketHandled(true);
    }

}
