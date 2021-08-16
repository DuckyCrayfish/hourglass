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

package net.lavabucket.hourglass.chat;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import org.apache.commons.lang3.BooleanUtils;

import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.time.ServerTimeHandler;
import net.lavabucket.hourglass.time.SleepState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HourglassMessages {

    /**
     * Event listener that is called every tick for every player who is sleeping.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepingCheckEvent(SleepingTimeCheckEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.level.isClientSide() && player.isSleeping() && player.getSleepTimer() == 1
                && player.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            sendSleepMessage(event.getPlayer());
        }
    }

    /**
     * Event listener that is called when a player gets out of bed.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        PlayerEntity player = event.getPlayer();
        if (event.getPlayer().level.isClientSide() == false && event.updateWorld() == true
                && player.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            sendWakeMessage(event.getPlayer());
        }
    }

    /**
     * Event listener that is called at morning when sleep has completed in a dimension.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepFinishedEvent(SleepFinishedTimeEvent event) {
        if (event.getWorld() instanceof ServerWorld) {
            sendMorningMessage((ServerWorld) event.getWorld());
        }
    }

    /**
     * Sends a message to all targeted players informing them that a player has started sleeping.
     *
     * The message is set by {@link HourglassConfig#inBedMessage}.
     * The target is set by {@link HourglassConfig#bedMessageTarget}.
     * The message type is set by {@link HourglassConfig#bedMessageType}.
     *
     * @param player  the player who started sleeping
     */
    public static void sendSleepMessage(PlayerEntity player) {
        String templateMessage = SERVER_CONFIG.inBedMessage.get();
        ServerTimeHandler timeHandler = ServerTimeHandler.instance;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null || timeHandler.world.players().size() <= 1) {
            return;
        }

        SleepState sleepState = timeHandler.sleepState;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepState.totalPlayerCount))
                .setVariable("sleepingPlayers", Integer.toString(sleepState.sleepingPlayerCount))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepState.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), (ServerWorld) player.level);
    }

    /**
     * Sends a message to all targeted players informing them that a player has left their bed.
     *
     * The message is set by {@link HourglassConfig#outOfBedMessage}.
     * The target is set by {@link HourglassConfig#bedMessageTarget}.
     * The message type is set by {@link HourglassConfig#bedMessageType}.
     *
     * @param player  the player who left their bed
     */
    public static void sendWakeMessage(PlayerEntity player) {
        String templateMessage = SERVER_CONFIG.outOfBedMessage.get();
        ServerTimeHandler timeHandler = ServerTimeHandler.instance;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null || timeHandler.world.players().size() <= 1) {
            return;
        }

        SleepState sleepState = timeHandler.sleepState;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepState.totalPlayerCount))
                .setVariable("sleepingPlayers", Integer.toString(sleepState.sleepingPlayerCount - 1))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepState.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), (ServerWorld) player.level);
    }

    /**
     * Sends a message to all targeted players informing them that the night has passed in world
     * after being accelerated by sleeping players.
     *
     * The message is set by {@link HourglassConfig#morningMessage}.
     * The target is set by {@link HourglassConfig#morningMessageTarget}.
     * The message type is set by {@link HourglassConfig#morningMessageType}.
     *
     * @param world  the world that night has passed in
     */
    public static void sendMorningMessage(ServerWorld world) {
        String templateMessage = SERVER_CONFIG.morningMessage.get();
        ServerTimeHandler timeHandler = ServerTimeHandler.instance;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null) {
            return;
        }

        SleepState sleepState = timeHandler.sleepState;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.morningMessageType.get())
                .setVariable("totalPlayers", Integer.toString(sleepState.totalPlayerCount))
                .setVariable("sleepingPlayers", Integer.toString(sleepState.sleepingPlayerCount))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepState.getRatio())))
                .bake().send(SERVER_CONFIG.morningMessageTarget.get(), world);

        // JSON version to implement later:
        // ITextComponent morningMessage = ITextComponent.Serializer.fromJson(HourglassConfig.SERVER.morningMessageJson.get());
    }

}
