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

import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.TimeContext;

/**
 * Time effect that increases the random tick speed while players are sleeping, proportionate to
 * the current speed of time.
 */
public class RandomTickSleepEffect extends AbstractTimeEffect {

    @Override
    public void onTimeTick(TimeContext context) {
        updateRandomTickSpeed(context);
    }

    /**
     * Updates the random tick speed based on configuration values.
     * @param context  the {@link TimeContext} of the current tick
     */
    private void updateRandomTickSpeed(TimeContext context) {
        EffectCondition condition = SERVER_CONFIG.randomTickEffect.get();

        if (condition == EffectCondition.NEVER) {
            return;
        }

        int speed = SERVER_CONFIG.baseRandomTickSpeed.get();
        SleepStatus sleepStatus = context.getTimeService().sleepStatus;
        if (condition == ALWAYS || (condition == SLEEPING && !sleepStatus.allAwake())) {
            speed *= context.getTimeDelta();
        }

        context.getTimeService().levelWrapper.setRandomTickSpeed(speed);
    }

}
