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
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import net.lavabucket.hourglass.HourglassMod;
import net.lavabucket.hourglass.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import net.minecraftforge.fmllegacy.network.ConfigSync;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.FMLHandshakeMessages.S2CConfigData;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

/**
 * A very quick and very dirty method of synchronizing server configuration with clients.
 *
 * Forge syncs server configs with clients by default on login, but leaves clients out-of-date if
 * the configuration changes after login. This class handles server configuration changes and
 * pushes them to the clients using the same methods Forge uses during the initial login sync.
 */
public class ConfigSynchronizer {

    /**
     * Listens for a config reload event and initializes a configuration sync if the server config
     * was changed.
     *
     * @param event  the config reload event, provided by the event bus
     */
    @SubscribeEvent
    public static void onModConfigEvent(final Reloading event) {
        final ModConfig config = event.getConfig();
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null
                && server.isRunning()
                && config.getSpec() == HourglassConfig.SERVER_CONFIG.spec) {

            syncConfigWithClients();
        }
    }

    /**
     * Returns the configuration message class. Removes the need to import the message class where
     * the network channel message is registered.
     *
     * @return the configuration message class
     */
    public static Class<S2CConfigData> getMessageClass() {
        return S2CConfigData.class;
    }

    /**
     * Encodes an S2CConfigData config data object and writes it to a PacketBuffer.
     *
     * @param config  the config data object to encode
     * @param buffer  the buffer to write the object to
     */
    public static void encode(S2CConfigData config, FriendlyByteBuf buffer) {
        buffer.writeUtf(config.getFileName());
        buffer.writeByteArray(config.getBytes());
    }

    /**
     * Attempts to decode an S2CConfigData object from the given PacketBuffer.
     *
     * @param buffer  the buffer to read the object data from
     * @return the config data object that was read from the buffer
     */
    public static S2CConfigData decode(FriendlyByteBuf buffer) {
        return S2CConfigData.decode(buffer);
    }

    /**
     * Handle a received configuration data packet. Applies the new configuration using the same
     * method Forge uses to sync the config with clients on login.
     *
     * @param configData  the configuration data packet
     * @param context  the network message context
     */
    public static void handle(S2CConfigData configData, Supplier<Context> context) {
        ConfigSync.INSTANCE.receiveSyncedConfig(configData, context);
        context.get().setPacketHandled(true);
    }

    /**
     * Synchronizes the mod server config file with all clients currently connected. Produces a log
     * on failure.
     */
    @SuppressWarnings("unchecked")
    public static void syncConfigWithClients() {
        LogManager.getLogger().info("Synchronizing server config with clients.");

        try {
            Field configsByModField = ConfigTracker.class.getDeclaredField("configsByMod");
            configsByModField.setAccessible(true);

            ConfigTracker configTracker = ConfigTracker.INSTANCE;
            ModConfig modConfig =
                    ((Map<String, Map<Type, ModConfig>>) configsByModField.get(configTracker))
                    .get(HourglassMod.ID)
                    .get(ModConfig.Type.SERVER);
            byte[] configData = Files.readAllBytes(modConfig.getFullPath());
            S2CConfigData message = new S2CConfigData(modConfig.getFileName(), configData);
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), message);

        } catch (Exception e) {
            LogManager.getLogger().error("Failed to sync server config with clients.", e);
        }
    }

}
