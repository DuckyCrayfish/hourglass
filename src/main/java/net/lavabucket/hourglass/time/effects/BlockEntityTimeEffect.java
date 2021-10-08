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

import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.TimeContext;

/**
 * Represents a {@code TimeEffect} that progresses block entities to match the rate of the current
 * time-speed.
 */
public class BlockEntityTimeEffect extends AbstractTimeEffect {

    @Override
    public void onTimeTick(TimeContext context) {
        EffectCondition condition = SERVER_CONFIG.blockEntityEffect.get();
        long extraTicks = context.getTimeDelta().longValue() - 1;
        SleepStatus sleepStatus = context.getTimeService().sleepStatus;

        if (extraTicks <= 0
                || condition == EffectCondition.NEVER
                || (condition == EffectCondition.SLEEPING && sleepStatus.allAwake())) {
            return;
        }

        for (int i = 0; i < extraTicks; i++) {
            context.getLevel().tickBlockEntities();
        }
    }

}
