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

import net.lavabucket.hourglass.message.factory.ConfigurableNotificationFactory;
import net.lavabucket.hourglass.message.factory.SleepNotificationFactory;
import net.lavabucket.hourglass.message.factory.TimeServiceNotificationFactory;
import net.lavabucket.hourglass.message.target.TargetContext;
import net.lavabucket.hourglass.message.target.TargetParam;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** This class listens for events and sends out Hourglass chat notifications. */
public class HourglassMessages {

    public static final ConfigurableNotificationFactory MORNING_MESSAGE =
            new TimeServiceNotificationFactory.Builder()
                .stringTarget(SERVER_CONFIG.morningMessageTarget::get)
                .type(SERVER_CONFIG.morningMessageType::get)
                .template(SERVER_CONFIG.morningMessage::get)
                .translationKey(() ->  "hourglass.messages.morning")
                .translationMode(SERVER_CONFIG.internationalMode::get)
                .create();

    public static final ConfigurableNotificationFactory ENTER_BED_MESSAGE =
            new SleepNotificationFactory.Builder()
                .stringTarget(SERVER_CONFIG.enterBedMessageTarget::get)
                .type(SERVER_CONFIG.enterBedMessageType::get)
                .template(SERVER_CONFIG.enterBedMessage::get)
                .translationKey(() ->  "hourglass.messages.enterBed")
                .translationMode(SERVER_CONFIG.internationalMode::get)
                .create();

    public static final ConfigurableNotificationFactory LEAVE_BED_MESSAGE =
            new SleepNotificationFactory.Builder()
                .stringTarget(SERVER_CONFIG.leaveBedMessageTarget::get)
                .type(SERVER_CONFIG.leaveBedMessageType::get)
                .template(SERVER_CONFIG.leaveBedMessage::get)
                .translationKey(() ->  "hourglass.messages.leaveBed")
                .translationMode(SERVER_CONFIG.internationalMode::get)
                .create();

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

            TargetContext context = new TargetContext.Builder()
                    .addParameter(TargetParam.TIME_SERVICE, service)
                    .addParameter(TargetParam.LEVEL, service.level)
                    .addParameter(TargetParam.PLAYER, player)
                    .create();

            ENTER_BED_MESSAGE.create(context).send();
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

            TargetContext context = new TargetContext.Builder()
                    .addParameter(TargetParam.TIME_SERVICE, service)
                    .addParameter(TargetParam.LEVEL, service.level)
                    .addParameter(TargetParam.PLAYER, player)
                    .create();

            LEAVE_BED_MESSAGE.create(context).send();
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


            TargetContext context = new TargetContext.Builder()
                    .addParameter(TargetParam.TIME_SERVICE, service)
                    .addParameter(TargetParam.LEVEL, service.level)
                    .create();

            MORNING_MESSAGE.create(context).send();
        }
    }

}
