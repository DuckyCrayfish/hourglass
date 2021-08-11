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
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Helper that uses reflection to access private or protected vanilla members.
 */
public class VanillaTimeHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Field allPlayersSleeping = ObfuscationReflectionHelper.findField(ServerWorld.class, "field_73068_P");
    private static final Field serverLevelData = ObfuscationReflectionHelper.findField(ServerWorld.class, "field_241103_E_");
    private static final Method wakeUpAllPlayers = ObfuscationReflectionHelper.findMethod(ServerWorld.class, "func_229856_ab_");
    private static final Method stopWeather = ObfuscationReflectionHelper.findMethod(ServerWorld.class, "func_73051_P");

    static {
        allPlayersSleeping.setAccessible(true);
        serverLevelData.setAccessible(true);
        wakeUpAllPlayers.setAccessible(true);
        stopWeather.setAccessible(true);
    }

    /**
     * Call at the beginning of every world-tick on the server to prevent the vanilla sleep mechanic.
     *
     * @param world  the ServerWorld to prevent sleep on
     */
    public static void preventVanillaSleep(ServerWorld world) {
        try {
            allPlayersSleeping.setBoolean(world, false);
        } catch (IllegalAccessException e) {
            LOGGER.warn(HourglassMod.MARKER, "Error preventing vanilla sleep - could not access ServerWorld#allPlayersSleeping field.");
        }
    }

    /**
     * Invoke the vanilla {@link net.minecraft.world.server.ServerWorld#stopWeather()} private method.
     *
     * @param world  the ServerWorld to invoke the method on
     */
    public static void stopWeather(ServerWorld world) {
        try {
            stopWeather.invoke(world);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error(HourglassMod.MARKER, "Failed to stop weather - could not access ServerWorld#stopWeather() method.", e);
        }
    }

    /**
     * Invokes the vanilla {@link net.minecraft.world.server.ServerWorld#wakeUpAllPlayers()} private method.
     * Wakes all currently sleeping players.
     *
     * @param world  the ServerWorld to wake all sleeping players on
     */
    public static void wakeUpAllPlayers(ServerWorld world) {
        try {
            wakeUpAllPlayers.invoke(world);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error(HourglassMod.MARKER, "Failed to wake players - could not access ServerWorld#wakeAllPlayers() method.", e);
        }
    }

    /**
     * Retrieves the {@link net.minecraft.world.server.ServerWorld#serverLevelData} protected field.
     *
     * @param world  the ServerWorld to fetch the field of
     */
    public static IServerWorldInfo getServerLevelData(ServerWorld world) {
        try {
            return (IServerWorldInfo) serverLevelData.get(world);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error(HourglassMod.MARKER, "Error accelerating weather - Failed to retrieve server level data.", e);
            return null;
        }
    }

}
