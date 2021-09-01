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

package net.lavabucket.hourglass.utils;

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
 * This class helps to circumvent restricted access to vanilla members via reflection or feature
 * emulation.
 */
public final class VanillaAccessHelper {

    /** Private constructor to disallow instantiation. */
    private VanillaAccessHelper() {}

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Field sleepStatus = ObfuscationReflectionHelper.findField(ServerLevel.class, "f_143245_");
    private static final Method wakeUpAllPlayers = ObfuscationReflectionHelper.findMethod(ServerLevel.class, "m_8804_");

    static {
        sleepStatus.setAccessible(true);
        wakeUpAllPlayers.setAccessible(true);
    }

    /**
     * Sets the private final field sleepStatus of {@link ServerLevel}.
     *
     * @param level  the object whose field should be set
     * @param newStatus  the new field
     */
    public static void setSleepStatus(ServerLevel level, SleepStatus newStatus) {
        try {
            sleepStatus.set(level, newStatus);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            LOGGER.error(HourglassMod.MARKER, "Error setting sleep status.", e);
            return;
        }
    }

    /**
     * Emulate the vanilla functionality for stopping weather.
     *
     * @param level  the level in which weather should be stopped
     */
    public static void stopWeather(ServerLevel level) {
        ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        levelData.setRainTime(0);
        levelData.setRaining(false);
        levelData.setThunderTime(0);
        levelData.setThundering(false);
    }

    /**
     * Performs vanilla morning wakeup functionality to wake up all sleeping players.
     *
     * @param level  the level in which all players should be woken up
     */
    public static void wakeUpAllPlayers(ServerLevel level) {
        try {
            wakeUpAllPlayers.invoke(level);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error(HourglassMod.MARKER, "Failed to wake players - could not access ServerLevel#wakeAllPlayers() method.", e);
        }
    }

}
