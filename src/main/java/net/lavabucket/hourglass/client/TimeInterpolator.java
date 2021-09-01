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

package net.lavabucket.hourglass.client;

import net.lavabucket.hourglass.utils.TimeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This class detects time updates from the server and interpolates the changes over time to smooth
 * out any time jumps.
 */
public class TimeInterpolator {

    // Time changes should be interpolated over one tick
    private static final float interpolationDuration = 1F;

    public static TimeInterpolator instance;

    public ClientLevel level;
    private boolean initialized;
    private long lastTime;
    private long targetTime;
    private float lastPartialTickTime;
    private float timeVelocity;

    /**
     * Event listener that is called when a new level is loaded.
     *
     * When switching dimensions, this is usually called <b>before</b> {@link WorldEvent.Unload}
     * has been called for the old dimension.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ClientLevel) {
            ClientLevel level = (ClientLevel) event.getWorld();
            instance = new TimeInterpolator(level);
        }
    }

    /**
     * Event listener that is called when a level is unloaded.
     *
     * When switching dimensions, this is usually called <b>after</b> a {@link WorldEvent.Load}
     * event has been dispatched for the new level.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ClientLevel) {
            ClientLevel level = (ClientLevel) event.getWorld();
            if (instance != null && instance.level.equals(level)) {
                instance = null;
            }
        }
    }

    /**
     * Event listener that is called on every render tick by the Forge event bus.
     *
     * This function calls {@link #partialTick(float)} if the conditions are correct. It is
     * important to note that a couple render ticks can happen in a dimension after the dimension
     * has been unloaded and {@link #onWorldUnload(WorldEvent.Unload)} has been called on it.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onRenderTickEvent(RenderTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (event.phase == Phase.START
                && !minecraft.isPaused()
                && level != null
                && instance != null
                && instance.level.equals(level)) {

            instance.partialTick(event.renderTickTime);
        }
    }

    /**
     * Event listener that is called on every tick by the Forge event bus. This event continues to
     * be dispatched while a player is in the main or pause menu.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void OnClientTickEvent(ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (event.phase == Phase.END
                && !minecraft.isPaused()
                && level != null
                && instance != null) {

            instance.undoVanillaTimeTicks();
        }
    }

    /**
     * Creates a new instance.
     *
     * @param level  the level whose time this object should manage
     */
    public TimeInterpolator(ClientLevel level) {
        this.level = level;
        this.initialized = false;
    }

    /**
     * Initializes variables that need to be set after ticks have started processing.
     */
    private void init() {
        long time = level.getDayTime();
        this.targetTime = time;
        this.lastTime = time;
        this.initialized = true;
    }

    /**
     * This method is used to smooth out updates to the time of day on a frame-by-frame basis so
     * that the time of day does not appear to stutter when the time speed is fast.
     *
     * @param partialTickTime  fractional percentage of progress from last tick to the next one
     */
    public void partialTick(float partialTickTime) {
        if (!initialized) {
            init();
        }

        float timeDelta = getPartialTimeDelta(partialTickTime);
        updateTargetTime();
        interpolateTime(timeDelta);
    }

    /**
     * Calculates the amount of time that has passed since this method was last run. Measured in
     * fractions of ticks. This method assumes it is being ran at least once per tick.
     *
     * @param partialTickTime  the current partial tick time
     * @return the time that has passed since last run.
     */
    private float getPartialTimeDelta(float partialTickTime) {
        float partialTimeDelta = partialTickTime - lastPartialTickTime;
        if (partialTimeDelta < 0) partialTimeDelta += 1;
        lastPartialTickTime = partialTickTime;
        return partialTimeDelta;
    }

    /**
     * Interpolate time changes changes on a frame-by-frame basis to smooth out time updates from
     * the server.
     *
     * @param timeDelta  the amount of time that has passed since this method was last run. Measured
     * in fractions of ticks.
     */
    private void interpolateTime(float timeDelta) {
        long time = level.getDayTime();

        float omega = 2F / interpolationDuration;
        float x = omega * timeDelta;
        float exp = 1F / (1F + x + 0.48F * x * x + 0.235F * x * x * x);
        float change = time - targetTime;

        float temp = (timeVelocity + omega * change) * timeDelta;
        timeVelocity = (timeVelocity - omega * temp) * exp;
        long newTime = targetTime + (long) ((change + temp) * exp);

        // Disallow overstepping
        if (change < 0.0F == newTime > targetTime) {
            newTime = targetTime;
            timeVelocity = 0.0F;
        }

        setTime(newTime);
    }

    /**
     * When the server updates the client time, it does so by directly changing the current day
     * time. To interpolate changes received from the server over multiple frames (instead of
     * accepting the jump in time), this method detects time updates from the server, resets time
     * to where it was originally, and then updates the interpolation target time instead.
     *
     * To prevent interpolation distances larger than a single day (which could be jarring) this
     * method jumps to same day as the interpolation target and interpolates from there.
     */
    private void updateTargetTime() {
        long time = level.getDayTime();

        // Packet received, update interpolation target and reset current time.
        if (time != lastTime) {
            targetTime = time;

            // Prevent large interpolation distances
            long discrepancy = lastTime - time;
            if (Math.abs(discrepancy) > TimeUtils.DAY_LENGTH) {
                long newTimeOfDay = TimeUtils.getTimeOfDay(time);
                long oldTimeOfDay = TimeUtils.getTimeOfDay(lastTime);
                lastTime = time - newTimeOfDay + oldTimeOfDay;
            }

            level.setDayTime(lastTime);
        }
    }

    /**
     * Updates the time of day in {@link #level}, while keeping track of the last time set using
     * this method.
     *
     * @param time  the time of day to set
     */
    private void setTime(long time) {
        level.setDayTime(time);
        lastTime = time;
    }

    /**
     * The vanilla client increments time every tick, which messes with our time interpolation. Call
     * this method at the end of every tick to undo this.
     */
    private void undoVanillaTimeTicks() {
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            level.setDayTime(level.getDayTime() - 1);
        }
    }

}
