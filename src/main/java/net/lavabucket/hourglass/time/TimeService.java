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

import static net.lavabucket.hourglass.Hourglass.MARKER;
import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.lavabucket.hourglass.time.providers.SystemBasedTimeProvider;
import net.lavabucket.hourglass.time.providers.TimeProvider;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.TimePacketWrapper;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Handles the Hourglass time and sleep functionality for a level.
 */
public class TimeService {

    private static final Logger LOGGER = LogManager.getLogger();

    // The largest number of lunar cycles that can be stored in an int
    private static final int OVERFLOW_THRESHOLD = 11184 * Time.LUNAR_CYCLE_TICKS;

    /** The level managed by this {@code TimeService}. */
    public final ServerLevelWrapper level;
    /** The {@code SleepStatus} object for this level. */
    public final SleepStatus sleepStatus;

    public TimeProvider timeProvider;

    private double timeDecimalAccumulator = 0;

    /**
     * Creates a new instance.
     *
     * @param level  the wrapped level whose time this object should manage
     */
    public TimeService(ServerLevelWrapper level) {
        this.level = level;
        this.sleepStatus = new SleepStatus(() -> SERVER_CONFIG.enableSleepFeature.get());
        this.level.setSleepStatus(this.sleepStatus);
        this.timeProvider = new SystemBasedTimeProvider();
    }

    /**
     * Performs all time, sleep, and weather calculations. Should run once per tick.
     */
    public void tick() {
        if (!level.daylightRuleEnabled()) {
            return;
        }

        Time oldTime = getDayTime();
        Time time = timeProvider.updateTime();
        setDayTime(time);
        Time deltaTime = time.subtract(oldTime);

        timeProvider.updateTime();
        TimeContext context = new TimeContext(this, time, deltaTime);
        getActiveTimeEffects().forEach(effect -> effect.onTimeTick(context));

        boolean overrideSleep = SERVER_CONFIG.enableSleepFeature.get();
        if (overrideSleep && !sleepStatus.allAwake() && Time.crossedMorning(oldTime, time)) {
            handleMorning();
        }

        preventTimeOverflow();
        broadcastTime();
        vanillaTimeCompensation();
    }

    private void handleMorning() {
        long time = level.get().getDayTime();
        ForgeEventFactory.onSleepFinished(level.get(), time, time);
        sleepStatus.removeAllSleepers();
        level.wakeUpAllPlayers();

        if (level.weatherRuleEnabled() && SERVER_CONFIG.clearWeatherOnWake.get()) {
            level.stopWeather();
        }

        LOGGER.debug(MARKER, "Sleep cycle complete on dimension: {}.",
                level.get().dimension().location());
    }

    /**
     * This method compensates for time changes made by the vanilla server every tick.
     *
     * The vanilla server increments time at a rate of 1 every tick. Since this functionality
     * conflicts with this mod's time changes, and this functionality cannot be prevented, this
     * method should be called at the end of the {@code START} phase of every world tick to undo
     * this vanilla progression.
     */
    private void vanillaTimeCompensation() {
        level.get().setDayTime(level.get().getDayTime() - 1);
    }

    /**
     * Prevents time value from getting too large by essentially keeping it modulo a multiple of the
     * lunar cycle.
     */
    private void preventTimeOverflow() {
        long time = level.get().getDayTime();
        if (time > OVERFLOW_THRESHOLD) {
            level.get().setDayTime(time - OVERFLOW_THRESHOLD);
        }
    }

    /**
     * {@return this level's time as an instance of {@link Time}}
     */
    public Time getDayTime() {
        return new Time(level.get().getDayTime(), timeDecimalAccumulator);
    }

    /**
     * Sets this level's 'daytime' to the integer component of {@code time}.
     * @param time  the time to set
     * @return the new time
     */
    public Time setDayTime(Time time) {
        timeDecimalAccumulator = time.fractionalValue();
        level.get().setDayTime(time.longValue());
        return time;
    }

    /**
     * Broadcasts the current time to all players who observe it.
     */
    public void broadcastTime() {
        TimePacketWrapper timePacket = TimePacketWrapper.create(level);
        level.get().getServer().getPlayerList().getPlayers().stream()
                .filter(player -> managesLevel(new ServerLevelWrapper(player.level)))
                .forEach(player -> player.connection.send(timePacket.get()));
    }

    /**
     * Returns true if {@code levelToCheck} has its time managed by this object, or false otherwise.
     * If this object is managing the overworld, this method will return true for all derived
     * levels.
     *
     * @param levelToCheck  the level to check
     * @return true if {@code levelToCheck} has its time managed by this object, or false otherwise.
     */
    public boolean managesLevel(ServerLevelWrapper levelToCheck) {
        if (level.get().equals(levelToCheck.get())) {
            return true;
        } else if (level.get().equals(level.get().getServer().overworld())
                && ServerLevelWrapper.isDerived(levelToCheck.get())) {
            return true;
        } else {
            return false;
        }
    }

    private Collection<TimeEffect> getActiveTimeEffects() {
        return HourglassRegistry.TIME_EFFECT.getValues();
    }

}
