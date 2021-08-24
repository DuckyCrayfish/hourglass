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
import net.lavabucket.hourglass.time.HourglassSleepStatus;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
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
        Player player = event.getPlayer();
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
        Player player = event.getPlayer();
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
        if (event.getWorld() instanceof ServerLevel) {
            sendMorningMessage((ServerLevel) event.getWorld());
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
    public static void sendSleepMessage(Player player) {
        String templateMessage = SERVER_CONFIG.inBedMessage.get();
        TimeService timeHandler = TimeServiceManager.service;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null || timeHandler.world.players().size() <= 1) {
            return;
        }

        HourglassSleepStatus sleepStatus = timeHandler.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), (ServerLevel) player.level);
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
    public static void sendWakeMessage(Player player) {
        String templateMessage = SERVER_CONFIG.outOfBedMessage.get();
        TimeService timeHandler = TimeServiceManager.service;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null || timeHandler.world.players().size() <= 1) {
            return;
        }

        HourglassSleepStatus sleepStatus = timeHandler.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping() - 1))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), (ServerLevel) player.level);
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
    public static void sendMorningMessage(ServerLevel world) {
        String templateMessage = SERVER_CONFIG.morningMessage.get();
        TimeService timeHandler = TimeServiceManager.service;
        if (templateMessage.isEmpty() || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())
                || timeHandler == null) {
            return;
        }

        HourglassSleepStatus sleepStatus = timeHandler.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.morningMessageType.get())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.morningMessageTarget.get(), world);

        // JSON version to implement later:
        // ITextComponent morningMessage = ITextComponent.Serializer.fromJson(HourglassConfig.SERVER.morningMessageJson.get());
    }

}
