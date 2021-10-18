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

import java.util.stream.Stream;

import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** This class listens for events and sends out Hourglass chat notifications. */
public class HourglassMessages {

    /** Translation key for the message that is sent at morning. */
    public static final String MORNING_KEY = "hourglass.messages.morning";
    /** Translation key for the message that is sent when a player enters a bed. */
    public static final String ENTER_BED_KEY = "hourglass.messages.enterBed";
    /** Translation key for the message that is sent when a player leaves a bed. */
    public static final String LEAVE_BED_KEY = "hourglass.messages.leaveBed";

    /**
     * Event listener that is called every tick for every player who is sleeping.
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepingCheckEvent(SleepingTimeCheckEvent event) {
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && event.getPlayer().getSleepTimer() == 2
                && event.getPlayer().getClass().equals(ServerPlayerWrapper.CLASS)
                && service != null
                && service.level.get().equals(event.getPlayer().level)
                && service.level.get().players().size() > 1
                && service.level.daylightRuleEnabled()) {

            ServerPlayerWrapper player = new ServerPlayerWrapper(event.getPlayer());
            sendEnterBedMessage(service, player);
        }
    }

    /**
     * Event listener that is called when a player gets out of bed.
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onPlayerWakeUpEvent(PlayerWakeUpEvent event) {
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && event.updateWorld() == true
                && event.getPlayer().getClass().equals(ServerPlayerWrapper.CLASS)
                && service != null
                && service.level.get().equals(event.getPlayer().level)
                && service.level.get().players().size() > 1
                && service.level.daylightRuleEnabled()) {

            ServerPlayerWrapper player = new ServerPlayerWrapper(event.getPlayer());
            sendLeaveBedMessage(service, player);
        }
    }

    /**
     * Event listener that is called at morning when sleep has completed in a dimension.
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onSleepFinishedEvent(SleepFinishedTimeEvent event) {
        TimeService service = TimeServiceManager.service;

        if (SERVER_CONFIG.enableSleepFeature.get() == true
                && service != null
                && service.level.get().equals(event.getWorld())
                && service.level.daylightRuleEnabled()) {

            sendMorningMessage(service);
        }
    }

    /**
     * Sends a message to targeted players informing them that a player has entered their bed.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#enterBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#enterBedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#enterBedMessageType}.
     *
     * @param timeService  the {@code TimeService} managing the level the player is in
     * @param player  the player who started sleeping
     */
    public static void sendEnterBedMessage(TimeService timeService, ServerPlayerWrapper player) {
        final SleepStatus sleepStatus = timeService.sleepStatus;

        final MessageBuilder builder = new MessageBuilder()
                .setVariable("player", player.get().getDisplayName())
                .setVariable("sleepingPlayers", sleepStatus.amountSleeping())
                .setVariable("totalPlayers", sleepStatus.amountActive())
                .setVariable("sleepingPercentage", sleepStatus.percentage());

        TextWrapper message;
        if (SERVER_CONFIG.internationalMode.get()) {
            message = builder.buildFromTranslation(ENTER_BED_KEY);
        } else {
            String template = SERVER_CONFIG.enterBedMessage.get();

            if (template.isEmpty()) {
                return;
            }

            message = builder.buildFromTemplate(template);
        }

        ChatType type = SERVER_CONFIG.enterBedMessageType.get();
        MessageTargetType target = SERVER_CONFIG.enterBedMessageTarget.get();
        send(message, type, target, timeService.level);
    }

    /**
     * Sends a message to targeted players informing them that a player has left their bed.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#leaveBedMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#leaveBedMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#leaveBedMessageType}.
     *
     * @param timeService  the {@code TimeService} managing the level the player is in
     * @param player  the player who left their bed
     */
    public static void sendLeaveBedMessage(TimeService timeService, ServerPlayerWrapper player) {
        final SleepStatus sleepStatus = timeService.sleepStatus;

        final MessageBuilder builder = new MessageBuilder()
                .setVariable("player", player.get().getDisplayName())
                .setVariable("sleepingPlayers", sleepStatus.amountSleeping() - 1)
                .setVariable("totalPlayers", sleepStatus.amountActive())
                .setVariable("sleepingPercentage", sleepStatus.percentage());

        TextWrapper message;
        if (SERVER_CONFIG.internationalMode.get()) {
            message = builder.buildFromTranslation(LEAVE_BED_KEY);
        } else {
            String template = SERVER_CONFIG.leaveBedMessage.get();

            if (template.isEmpty()) {
                return;
            }

            message = builder.buildFromTemplate(template);
        }

        ChatType type = SERVER_CONFIG.leaveBedMessageType.get();
        MessageTargetType target = SERVER_CONFIG.leaveBedMessageTarget.get();
        send(message, type, target, timeService.level);
    }

    /**
     * Sends a message to targeted players informing them that night has passed in {@code level} by
     * sleeping.
     *
     * The message is set by {@link HourglassConfig.ServerConfig#morningMessage}.
     * The target is set by {@link HourglassConfig.ServerConfig#morningMessageTarget}.
     * The message type is set by {@link HourglassConfig.ServerConfig#morningMessageType}.
     *
     * @param timeService  the {@code TimeService} managing the level
     */
    public static void sendMorningMessage(TimeService timeService) {
        final SleepStatus sleepStatus = timeService.sleepStatus;

        final MessageBuilder builder = new MessageBuilder()
                .setVariable("sleepingPlayers", sleepStatus.amountSleeping())
                .setVariable("totalPlayers", sleepStatus.amountActive())
                .setVariable("sleepingPercentage", sleepStatus.percentage());

        TextWrapper message;
        if (SERVER_CONFIG.internationalMode.get()) {
            message = builder.buildFromTranslation(MORNING_KEY);
        } else {
            String template = SERVER_CONFIG.morningMessage.get();

            if (template.isEmpty()) {
                return;
            }

            message = builder.buildFromTemplate(template);
        }

        ChatType type = SERVER_CONFIG.morningMessageType.get();
        MessageTargetType target = SERVER_CONFIG.morningMessageTarget.get();
        send(message, type, target, timeService.level);
    }

    /**
     * Sends the message to the specified targets. If {@code target} is MessageTarget.ALL,
     * {@code level} may be null.
     *
     * @param message  the text component to send
     * @param target  the target of the message
     * @param type  the {@code ChatType} of the message
     * @param level  the level that generated the message
     */
    public static void send(TextWrapper message, ChatType type, MessageTargetType target,
            ServerLevelWrapper level) {
        Stream<ServerPlayerWrapper> players;

        if (target == MessageTargetType.ALL) {
            players = level.get().getServer().getPlayerList().getPlayers().stream()
                    .map(ServerPlayerWrapper::new);
        } else {
            players = level.get().players().stream().map(ServerPlayerWrapper::new);
        }

        if (target == MessageTargetType.SLEEPING) {
            players = players.filter(ServerPlayerWrapper::isSleeping);
        }

        players.forEach(player -> player.get().sendMessage(message.get(), type, Util.NIL_UUID));
    }

}
