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

package net.lavabucket.hourglass.time.effects;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;
import static net.lavabucket.hourglass.time.effects.EffectCondition.ALWAYS;
import static net.lavabucket.hourglass.time.effects.EffectCondition.SLEEPING;

import net.lavabucket.hourglass.time.TimeContext;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;

/**
 * Time effect that increases the speed that weather passes at the same rate as the current speed of
 * time.
 */
public class WeatherSleepEffect extends AbstractTimeEffect {

    @Override
    public void onTimeTick(TimeContext context) {
        ServerLevelWrapper levelWrapper = context.getTimeService().levelWrapper;
        EffectCondition condition = SERVER_CONFIG.weatherEffect.get();
        boolean allAwake = context.getTimeService().sleepStatus.allAwake();
        if (levelWrapper.weatherCycleEnabled()
                && (condition == ALWAYS || (condition == SLEEPING && !allAwake))) {
            progressWeather(context);
        }
    }

    /**
     * Progress the weather cycle in the level of {@code context} by its time delta.
     *
     * @param context  the {@link TimeContext} of the current tick
     */
    private void progressWeather(TimeContext context) {
        ServerLevelWrapper levelWrapper = context.getTimeService().levelWrapper;
        int clearWeatherTime = levelWrapper.levelData.getClearWeatherTime();
        int thunderTime = levelWrapper.levelData.getThunderTime();
        int rainTime = levelWrapper.levelData.getRainTime();

        // Subtract 1 from weather speed to account for vanilla's weather progression of 1 per tick.
        int weatherSpeed = (int) Math.min(Integer.MAX_VALUE, context.getTimeDelta()) - 1;

        if (clearWeatherTime <= 0) {
            if (thunderTime > 0) {
                levelWrapper.levelData.setThunderTime(Math.max(1, thunderTime - weatherSpeed));
            }
            if (rainTime > 0) {
                levelWrapper.levelData.setRainTime(Math.max(1, rainTime - weatherSpeed));
            }
        }
    }

}
