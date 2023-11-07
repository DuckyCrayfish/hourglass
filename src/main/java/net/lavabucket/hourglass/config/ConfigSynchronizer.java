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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import net.lavabucket.hourglass.Hourglass;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
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
            var configsByMod = (Map<String, Map<Type, ModConfig>>) configsByModField.get(configTracker);

            ModConfig modConfig = configsByMod.get(Hourglass.MOD_ID).get(ModConfig.Type.SERVER);
            Path configFilePath = modConfig.getFullPath();
            String configFileName = modConfig.getFileName();
            byte[] configRawData = Files.readAllBytes(configFilePath);
            ConfigData configData = new ConfigData(configFileName, configRawData);
            NetworkInitialization.PLAY.send(configData, PacketDistributor.ALL.noArg());

        } catch (Exception e) {
            LogManager.getLogger().error("Failed to sync server config with clients.", e);
        }
    }

}
