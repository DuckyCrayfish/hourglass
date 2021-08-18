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

package net.lavabucket.hourglass.time;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.HourglassMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Helper that uses reflection to access private or protected vanilla members.
 */
public class VanillaTimeHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Field sleepStatus = ObfuscationReflectionHelper.findField(ServerLevel.class, "f_143245_");
    private static final Field serverLevelData = ObfuscationReflectionHelper.findField(ServerLevel.class, "f_8549_");
    private static final Method wakeUpAllPlayers = ObfuscationReflectionHelper.findMethod(ServerLevel.class, "m_8804_");

    static {
        sleepStatus.setAccessible(true);
        serverLevelData.setAccessible(true);
        wakeUpAllPlayers.setAccessible(true);
    }

    /**
     * Sets the private final field sleepStatus of {@link ServerLevel}.
     *
     * @param world  the object whose field should be set
     * @param newStatus  the new field
     */
    public static void setSleepStatus(ServerLevel world, SleepStatus newStatus) {
        try {
            sleepStatus.set(world, newStatus);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            LOGGER.error(HourglassMod.MARKER, "Error setting sleep status.", e);
            return;
        }
    }

    /**
     * Emulate the vanilla functionality for stopping weather.
     *
     * @param world  the level to stop weather in
     */
    public static void stopWeather(ServerLevel world) {
        ServerLevelData levelData;
        try {
            levelData = (ServerLevelData) serverLevelData.get(world);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error(HourglassMod.MARKER, "Error accelerating weather - Failed to retrieve server level data.", e);
            return;
        }
        levelData.setRainTime(0);
        levelData.setRaining(false);
        levelData.setThunderTime(0);
        levelData.setThundering(false);
    }

    /**
     * Invokes the vanilla {@link net.minecraft.world.server.ServerLevel#wakeUpAllPlayers()} private method.
     * Wakes all currently sleeping players.
     *
     * @param world  the ServerLevel to wake all sleeping players on
     */
    public static void wakeUpAllPlayers(ServerLevel world) {
        try {
            wakeUpAllPlayers.invoke(world);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error(HourglassMod.MARKER, "Failed to wake players - could not access ServerLevel#wakeAllPlayers() method.", e);
        }
    }

    /**
     * Retrieves the {@link net.minecraft.world.server.ServerLevel#serverLevelData} protected field.
     *
     * @param world  the ServerLevel to fetch the field of
     */
    public static ServerLevelData getServerLevelData(ServerLevel world) {
        try {
            return (ServerLevelData) serverLevelData.get(world);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error(HourglassMod.MARKER, "Error accelerating weather - Failed to retrieve server level data.", e);
            return null;
        }
    }

}
