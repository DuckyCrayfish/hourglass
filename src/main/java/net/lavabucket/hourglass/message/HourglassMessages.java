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

package net.lavabucket.hourglass.message;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
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
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && event.getPlayer().getSleepTimer() == 1
                && event.getPlayer().getClass() == ServerPlayerWrapper.playerClass
                && service != null
                && service.level.get().equals(event.getPlayer().level)
                && service.level.get().players().size() > 1
                && service.level.daylightRuleEnabled()) {

            sendEnterBedMessage(new ServerPlayerWrapper(event.getPlayer()));
        }
    }

    /**
     * Event listener that is called when a player gets out of bed.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && event.updateWorld() == true
                && event.getPlayer().getClass() == ServerPlayerWrapper.playerClass
                && service != null
                && service.level.get().equals(event.getPlayer().level)
                && service.level.get().players().size() > 1
                && service.level.daylightRuleEnabled()) {

            sendLeaveBedMessage(new ServerPlayerWrapper(event.getPlayer()));
        }
    }

    /**
     * Event listener that is called at morning when sleep has completed in a dimension.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepFinishedEvent(SleepFinishedTimeEvent event) {
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && service != null
                && service.level.get().equals(event.getWorld())
                && service.level.daylightRuleEnabled()) {

            ServerLevelWrapper level = new ServerLevelWrapper(event.getWorld());
            sendMorningMessage(level);
        }
    }

    /**
     * Sends a message to all targeted players informing them that a player has entered their bed.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#enterBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#enterBedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#enterBedMessageType}.
     *
     * @param player  the player who started sleeping
     */
    public static void sendEnterBedMessage(ServerPlayerWrapper player) {
        String templateMessage = SERVER_CONFIG.enterBedMessage.get();
        TimeService timeService = TimeServiceManager.service;

        if (templateMessage.isEmpty() || timeService == null) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.enterBedMessageType.get())
                .setVariable("player", player.get().getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString(sleepStatus.percentage()))
                .bake().send(SERVER_CONFIG.enterBedMessageTarget.get(), player.getLevel());
    }

    /**
     * Sends a message to all targeted players informing them that a player has left their bed.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#leaveBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#leaveBedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#leaveBedMessageType}.
     *
     * @param player  the player who left their bed
     */
    public static void sendLeaveBedMessage(ServerPlayerWrapper player) {
        String templateMessage = SERVER_CONFIG.leaveBedMessage.get();
        TimeService timeService = TimeServiceManager.service;

        if (templateMessage.isEmpty() || timeService == null) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.leaveBedMessageType.get())
                .setVariable("player", player.get().getGameProfile().getName())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping() - 1))
                .setVariable("sleepingPercentage", Integer.toString(sleepStatus.percentage()))
                .bake().send(SERVER_CONFIG.leaveBedMessageTarget.get(), player.getLevel());
    }

    /**
     * Sends a message to all targeted players informing them that the night has passed in level
     * after being accelerated by sleeping players.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#morningMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#morningMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#morningMessageType}.
     *
     * @param level  the level that night has passed in
     */
    public static void sendMorningMessage(ServerLevelWrapper level) {
        String templateMessage = SERVER_CONFIG.morningMessage.get();
        TimeService timeService = TimeServiceManager.service;

        if (templateMessage.isEmpty() || timeService == null) {
            return;
        }

        SleepStatus sleepStatus = timeService.sleepStatus;

        new TemplateMessage().setTemplate(templateMessage)
                .setType(SERVER_CONFIG.morningMessageType.get())
                .setVariable("totalPlayers", Integer.toString(sleepStatus.amountActive()))
                .setVariable("sleepingPlayers", Integer.toString(sleepStatus.amountSleeping()))
                .setVariable("sleepingPercentage", Integer.toString(sleepStatus.percentage()))
                .bake().send(SERVER_CONFIG.morningMessageTarget.get(), level);

        // JSON version to implement later:
        // ITextComponent morningMessage = ITextComponent.Serializer
                // .fromJson(HourglassConfig.SERVER.morningMessageJson.get());
    }

}
