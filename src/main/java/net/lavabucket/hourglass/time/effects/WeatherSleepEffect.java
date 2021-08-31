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

import net.lavabucket.hourglass.time.TimeContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * Time effect that increases the speed that weather passes at the same rate as the current speed of
 * time.
 */
public class WeatherSleepEffect extends AbstractTimeEffect {

    @Override
    public void onTimeTick(TimeContext context) {
        ServerLevel level = context.getTimeService().level;
        if (weatherCycleEnabled(level) && effectEnabled()) {
            progressWeather(context);
        }
    }

    /**
     * Accelerate the weather cycle in a level.
     *
     * @param context  the {@link TimeContext} of the current tick
     */
    private void progressWeather(TimeContext context) {
        ServerLevelData levelData = context.getTimeService().levelData;
        int clearWeatherTime = levelData.getClearWeatherTime();
        int thunderTime = levelData.getThunderTime();
        int rainTime = levelData.getRainTime();

        // Subtract 1 from weather speed to account for vanilla's weather progression of 1 per tick.
        int weatherSpeed = (int) Math.min(Integer.MAX_VALUE, context.getTimeDelta()) - 1;

        if (clearWeatherTime <= 0) {
            if (thunderTime > 0) {
                levelData.setThunderTime(Math.max(1, thunderTime - weatherSpeed));
            }
            if (rainTime > 0) {
                levelData.setRainTime(Math.max(1, rainTime - weatherSpeed));
            }
        }
    }

    /**
     * {@return true if the weather cycle is running on {@code level}}
     *
     * @param level  the level to check
     */
    private boolean weatherCycleEnabled(ServerLevel level) {
        return level.dimensionType().hasSkyLight() &&
                level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
    }

    /** {@return true if this effect is enabled in configs} */
    private boolean effectEnabled() {
        return SERVER_CONFIG.accelerateWeather.get() && SERVER_CONFIG.enableSleepFeature.get();
    }

}
