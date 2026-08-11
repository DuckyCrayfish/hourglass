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

import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Creates {@link TimeService} objects and passes events to them.
 */
public class TimeServiceManager {

    /** The Overworld {@code TimeService} object. null if Overworld not loaded. */
    public static TimeService service;
    /** The earliest time at which players are no longer allowed to sleep in vanilla. */
    public static final Time VANILLA_SLEEP_END = new Time(23460);

    /**
     * Modifies permitted sleep times. Only applies to players in levels controlled by Hourglass
     * while sleep feature is enabled.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCanPlayerSleepEvent(CanPlayerSleepEvent event) {
        if (
            service == null
            || event.getVanillaProblem() != BedSleepingProblem.NOT_POSSIBLE_NOW
            || event.getVanillaProblem() != event.getProblem()
            || !service.level.get().equals(event.getEntity().level())
            || HourglassConfig.SERVER_CONFIG.enableSleepFeature.get() == false
        ) {
            return;
        }

        // Allow players to sleep through dawn until daytime 0.
        // In vanilla, players are not allowed to sleep during dawn, however, a sleep cycle ends
        // at daytime 0 (after dawn).
        if (service.getDayTime().timeOfDay().compareTo(VANILLA_SLEEP_END) >= 0) {
            event.setProblem(null);
        }

        // Allow players to sleep during the day if allowDaySleep is enabled.
        if (HourglassConfig.SERVER_CONFIG.allowDaySleep.get()) {
            event.setProblem(null);
        }
    }

    /**
     * Modifies permitted sleep times. Only applies to players in levels controlled by Hourglass
     * while sleep feature is enabled.
     *
     * <p>Called once per tick for every player who is currently sleeping. Event result determines
     * if sleep is allowed to continue at the current time.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCanContinueSleepingEvent(CanContinueSleepingEvent event) {
        if (
            service == null
            || event.getProblem() != BedSleepingProblem.NOT_POSSIBLE_NOW
            || event.mayContinueSleeping() == true
            || !service.level.get().equals(event.getEntity().level())
            || HourglassConfig.SERVER_CONFIG.enableSleepFeature.get() == false
        ) {
            return;
        }

        // Allow players to sleep through dawn until daytime 0.
        // In vanilla, players are not allowed to sleep during dawn, however, a sleep cycle ends
        // at daytime 0 (after dawn).
        if (service.getDayTime().timeOfDay().compareTo(VANILLA_SLEEP_END) >= 0) {
            event.setContinueSleeping(true);
        }

        // Allow players to sleep during the day if allowDaySleep is enabled.
        if (HourglassConfig.SERVER_CONFIG.allowDaySleep.get()) {
            event.setContinueSleeping(true);
        }
    }

    /**
     * Event listener that is called when a new level is loaded.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (ServerLevelWrapper.isServerLevel(event.getLevel())) {
            ServerLevelWrapper level = new ServerLevelWrapper(event.getLevel());
            if (level.get().equals(level.get().getServer().overworld())) {
                service = new TimeService(level);
            }
        }
    }

    /**
     * Event listener that is called when a level is unloaded.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (service != null && service.level.get() == event.getLevel()) {
            service = null;
        }
    }

    /**
     * Event listener that is called every tick per level.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldTick(LevelTickEvent.Pre event) {
        var level = event.getLevel();

        if (!level.isClientSide() && service != null && service.level.get() == level) {
            service.tick();
        }
    }

}
