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
import java.util.HashMap;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;

import org.apache.logging.log4j.LogManager;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.time.SleepStatus;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper.UnableToFindFieldException;

/**
 * This class acts as a wrapper for {@link ServerWorld} to increase consistency between Minecraft
 * versions.
 *
 * Since the {@link ServerWorld} class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link ServerWorld}. This class consolidates these variations into itself,
 * allowing other classes to depend on it instead.
 */
public class ServerLevelWrapper extends Wrapper<ServerWorld> {

    // Store classes at the top to minimize file changes between Minecraft versions.
    private static final Class<ServerWorld> levelClass = ServerWorld.class;
    private static final Class<IServerWorldInfo> levelDataClass = IServerWorldInfo.class;
    private static final Class<DerivedWorldInfo> derivedLevelDataClass = DerivedWorldInfo.class;

    /** The {@link IServerWorldInfo} of the wrapped level. */
    public final IServerWorldInfo levelData;

    /**
     * Instantiates a new object.
     * @param level  the server level to wrap
     */
    public ServerLevelWrapper(IWorld level) {
        super(levelClass.cast(level));
        this.levelData = levelDataClass.cast(this.get().getLevelData());
    }

    /** {@return true if the 'daylight cycle' game rule is enabled in {@link #wrapped}} */
    public boolean daylightRuleEnabled() {
        return wrapped.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
    }

    /** {@return true if the 'weather cycle' game rule is enabled in {@link #wrapped}} */
    public boolean weatherRuleEnabled() {
        return wrapped.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
    }

    /**
     * Sets the 'random tick speed' game rule for {@link #wrapped}.
     * @param speed  the new random tick speed
     */
    public void setRandomTickSpeed(int speed) {
        // Vanilla code is missing integer gamerule setter due to tree-shaking. Workaround:
        CommandSource commandSource = new CommandSource(null, null, null, null, 0, null, null, get().getServer(), null);

        HashMap<String, ParsedArgument<CommandSource, ?>> arguments = new HashMap<>();
        arguments.put("value", new ParsedArgument<CommandSource,Integer>(0, 0, speed));

        CommandContext<CommandSource> commandContext = new CommandContext<CommandSource>(commandSource, null, arguments, null, null, null, null, null, null, false);

        get().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).setFromArgument(commandContext, "value");
    }

    /**
     * Convenience method that returns true if the weather cycle is progressing in {@link #wrapped}.
     * @return true if the weather cycle is progressing in {@link #wrapped}
     */
    public boolean weatherCycleEnabled() {
        return weatherRuleEnabled() && wrapped.dimensionType().hasSkyLight();
    }

    /**
     * Emulates the vanilla functionality for stopping weather, as access modifiers prevent calls
     * to the methods that do this in vanilla.
     */
    public void stopWeather() {
        levelData.setRainTime(0);
        levelData.setRaining(false);
        levelData.setThunderTime(0);
        levelData.setThundering(false);
    }

    /**
     * Sets the {@link ServerWorld} sleep status using reflection, as access modifiers prevent this.
     * In Minecraft versions lower than 1.17 this method should do nothing.
     *
     * @param newStatus  the new sleep status
     */
    public void setSleepStatus(SleepStatus newStatus) {
        return;
    }

    /**
     * Prevents vanilla sleep functionality when called every tick.
     *
     * <p> In Minecraft versions greater than 1.17, this method should do nothing.
     */
    public void preventVanillaSleep() {
        try {
            Field allPlayersSleeping = ObfuscationReflectionHelper.findField(ServerWorld.class, "field_73068_P");
            allPlayersSleeping.setAccessible(true);
            allPlayersSleeping.setBoolean(get(), false);
        } catch (UnableToFindFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LogManager.getLogger().warn(Hourglass.MARKER, "Error preventing vanilla sleep - could not access ServerWorld#allPlayersSleeping field.");
        }
    }

    /**
     * Performs vanilla morning wakeup functionality to wake up all sleeping players.
     */
    public void wakeUpAllPlayers() {
        wrapped.players().stream()
                .map(player -> new ServerPlayerWrapper(player))
                .filter(ServerPlayerWrapper::isSleeping)
                .forEach(player -> player.get().stopSleepInBed(false, false));
    }

    /**
     * {@return true if {@code level} is a derived level}
     * @param level  the level to check
     */
    public static boolean isDerived(IWorld level) {
        return level != null && level.getLevelData().getClass() == derivedLevelDataClass;
    }

    /** {@return true if {@code level} is an instance of a server level} */
    public static boolean isServerLevel(IWorld level) {
        return level != null && level.getClass() == levelClass;
    }

}
