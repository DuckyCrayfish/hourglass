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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.lavabucket.hourglass.Hourglass;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import net.minecraftforge.network.NetworkInitialization;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.packets.ConfigData;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * A very quick and very dirty method of synchronizing server configuration with clients.
 *
 * Forge syncs server configs with clients by default on login, but leaves clients out-of-date if
 * the configuration changes after login. This class handles server configuration changes and
 * pushes them to the clients using the same methods Forge uses during the initial login sync.
 */
public class ConfigSynchronizer {

    /** Class logger. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** The set of mod IDs who are registered for automatic syncing. */
    private final Set<String> registeredMods = new HashSet<>();

    /**
     * Creates a new instance.
     * @param eventBus  the mod event bus
     */
    public ConfigSynchronizer(IEventBus eventBus) {
        eventBus.addListener(this::onConfigReload);
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

            try {
                sync(config);
            } catch (Exception e) {
                LOGGER.error(Hourglass.MARKER,
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

    /**
     * Syncs the server config of the mod with a mod ID of {@code modId} to all clients connected
     * to the running server.
     *
     * @param modId  the mod ID of the mod with the server config to sync
     *
     * @throws IllegalStateException if a server is not running
     * @throws IllegalArgumentException if the mod does not have a registered server config
     * @throws IOException if an I/O error occurs while reading the config file
     */
    @SuppressWarnings("null")
    public void sync(final String modId) throws IOException {
        final ConfigTracker tracker = ConfigTracker.INSTANCE;
        final Set<ModConfig> serverConfigs = tracker.configSets().get(ModConfig.Type.SERVER);
        final ModConfig modConfig = serverConfigs.stream()
                .filter(config -> config.getModId().equals(modId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No server config found for mod ID " + modId));

        sync(modConfig);
    }

    /**
     * Syncs the server config {@code modConfig} with all clients currently connected to the server.
     *
     * @param modConfig  the server config to sync
     *
     * @throws IllegalStateException if a server is not running
     * @throws IllegalArgumentException if {@code modConfig} is not a server config
     * @throws IOException if an I/O error occurs while reading the config file
     */
    public void sync(@Nonnull final ModConfig modConfig) throws IOException {
        if (modConfig.getType() != ModConfig.Type.SERVER) {
            throw new IllegalArgumentException("modConfig must be a server config");
        }

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null || !server.isRunning()) {
            throw new IllegalStateException(
                    "Cannot sync server config with clients: server is not running.");
        }

        Path configFilePath = modConfig.getFullPath();
        String configFileName = modConfig.getFileName();
        byte[] configRawData = Files.readAllBytes(configFilePath);

        LOGGER.debug(Hourglass.MARKER,
                "Synchronizing {} server config with clients.", modConfig.getModId());
        ConfigData configData = new ConfigData(configFileName, configRawData);
        NetworkInitialization.PLAY.send(configData, PacketDistributor.ALL.noArg());
    }

}
