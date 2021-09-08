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

package net.lavabucket.hourglass.wrappers;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.HourglassMod;
import net.lavabucket.hourglass.time.SleepStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * This class acts as a wrapper for {@link ServerLevel} to increase consistency between Minecraft
 * versions.
 *
 * Since the {@link ServerLevel} class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link ServerLevel}. This class consolidates these variations into itself,
 * allowing other classes to depend on it instead.
 */
public class ServerLevelWrapper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Field sleepStatus = ObfuscationReflectionHelper.findField(ServerLevel.class, "f_143245_");
    static { sleepStatus.setAccessible(true); }

    /** The wrapped level. */
    public final ServerLevel level;

    /** The {@link ServerLevelData} of the wrapped level. */
    public final ServerLevelData levelData;

    /**
     * Instantiates a new object.
     * @param level  the level to wrap
     */
    public ServerLevelWrapper(LevelAccessor level) {
        if (!(level instanceof ServerLevel)) {
            throw new IllegalArgumentException("level must be an instance of ServerLevel.");
        }
        this.level = (ServerLevel) level;
        this.levelData = (ServerLevelData) this.level.getLevelData();

    }

    /** {@return true if the 'daylight cycle' game rule is enabled in {@link #level}} */
    public boolean daylightRuleEnabled() {
        return level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
    }

    /** {@return true if the 'weather cycle' game rule is enabled in {@link #level}} */
    public boolean weatherRuleEnabled() {
        return level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
    }

    /**
     * Sets the 'random tick speed' game rule for {@link #level}.
     * @param speed  the new random tick speed
     */
    public void setRandomTickSpeed(int speed) {
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(speed, level.getServer());
    }

    /**
     * Convenience method that returns true if the weather cycle is progressing in {@link #level}.
     * @return true if the weather cycle is progressing in {@link #level}
     */
    public boolean weatherCycleEnabled() {
        return weatherRuleEnabled() && level.dimensionType().hasSkyLight();
    }

    /**
     * Emulates the vanilla functionality for stopping weather, as access modifiers prevent calls
     * to the methods that do this in vanilla.
     */
    public void stopWeather() {
        ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        levelData.setRainTime(0);
        levelData.setRaining(false);
        levelData.setThunderTime(0);
        levelData.setThundering(false);
    }

    /**
     * Sets the {@link ServerLevel} sleep status, as access modifiers restrict access to this field.
     * @param newStatus  the new sleep status
     */
    public void setSleepStatus(SleepStatus newStatus) {
        try {
            sleepStatus.set(level, newStatus);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            LOGGER.error(HourglassMod.MARKER, "Error setting sleep status.", e);
            return;
        }
    }

    /**
     * Performs vanilla morning wakeup functionality to wake up all sleeping players.
     */
    public void wakeUpAllPlayers() {
        level.players().stream().filter(LivingEntity::isSleeping).forEach(player -> {
            player.stopSleepInBed(false, false);
        });
    }

    /**
     * {@return true if {@code level} is a derived level}
     * @param level  the level to check
     */
    public static boolean isDerived(Level level) {
        return level.getLevelData() instanceof DerivedLevelData;
    }

    /** {@return true if {@code levelAccessor} is an instance of {@link ServerLevel}} */
    public static boolean isServerLevel(LevelAccessor levelAccessor) {
        return levelAccessor instanceof ServerLevel;
    }

}
