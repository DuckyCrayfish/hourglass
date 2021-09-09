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

import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Creates {@link TimeService} objects and passes events to them.
 */
public class TimeServiceManager {

    public static TimeService service;

    /**
     * Called from the Forge EventBus during a SleepingTimeCheckEvent, a forge event that is
     * called once per tick for every player who is currently sleeping.
     *
     * @param event  the event provided by forge from the EventBus
     */
    @SubscribeEvent
    public static void onSleepingCheckEvent(SleepingTimeCheckEvent event) {
        long time = event.getPlayer().level.getDayTime() % 24000;
        if (time >= 23460 || time == 0) {
            event.setResult(Result.ALLOW);
        }
    }

    /**
     * Event listener that is called when a new level is loaded.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (ServerLevelWrapper.isServerLevel(event.getWorld())) {
            ServerLevelWrapper level = new ServerLevelWrapper(event.getWorld());
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
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (service != null && service.level.get() == event.getWorld()) {
            service = null;
        }
    }

    /**
     * Event listener that is called every tick per level.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START
                && service != null && service.level.get() == event.world) {
            service.tick();
        }
    }

}
