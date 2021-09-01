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

import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** This class listens for events and sends out Hourglass chat notifications. */
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
            sendSleepMessage(player);
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
        if (player.level.isClientSide() == false && event.updateWorld() == true
                && player.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            sendWakeMessage(player);
        }
    }

    /**
     * Event listener that is called at morning when sleep has completed in a dimension.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepFinishedEvent(SleepFinishedTimeEvent event) {
        if (ServerLevelWrapper.isServerLevel(event.getWorld())) {
            ServerLevelWrapper levelWrapper = new ServerLevelWrapper(event.getWorld());
            sendMorningMessage(levelWrapper);
        }
    }

    /**
     * Sends a message to all targeted players informing them that a player has started sleeping.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#inBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#bedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#bedMessageType}.
     *
     * @param player  the player who started sleeping
     */
    public static void sendSleepMessage(Player player) {
        String templateMessage = SERVER_CONFIG.inBedMessage.get();
        TimeService timeService = TimeServiceManager.service;
        if (templateMessage.isEmpty() || timeService == null
                || !SERVER_CONFIG.enableSleepFeature.get()
                || timeService.levelWrapper.level.players().size() <= 1) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), new ServerLevelWrapper(player.level));
    }

    /**
     * Sends a message to all targeted players informing them that a player has left their bed.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#outOfBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#bedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#bedMessageType}.
     *
     * @param player  the player who left their bed
     */
    public static void sendWakeMessage(Player player) {
        String templateMessage = SERVER_CONFIG.outOfBedMessage.get();
        TimeService timeService = TimeServiceManager.service;
        if (templateMessage.isEmpty() || timeService == null
                || !SERVER_CONFIG.enableSleepFeature.get()
                || timeService.levelWrapper.level.players().size() <= 1) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.bedMessageType.get())
                .setVariable("player", player.getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping() - 1))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.bedMessageTarget.get(), new ServerLevelWrapper(player.level));
    }

    /**
     * Sends a message to all targeted players informing them that the night has passed in level
     * after being accelerated by sleeping players.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#morningMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#morningMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#morningMessageType}.
     *
     * @param levelWrapper  the level that night has passed in
     */
    public static void sendMorningMessage(ServerLevelWrapper levelWrapper) {
        String templateMessage = SERVER_CONFIG.morningMessage.get();
        TimeService timeService = TimeServiceManager.service;
        if (templateMessage.isEmpty() || timeService == null
                || !SERVER_CONFIG.enableSleepFeature.get()) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.morningMessageType.get())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString((int) (100D * sleepStatus.getRatio())))
                .bake().send(SERVER_CONFIG.morningMessageTarget.get(), levelWrapper);

        // JSON version to implement later:
        // ITextComponent morningMessage = ITextComponent.Serializer.fromJson(HourglassConfig.SERVER.morningMessageJson.get());
    }

}
