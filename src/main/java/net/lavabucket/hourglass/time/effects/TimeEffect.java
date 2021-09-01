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

import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.TimeContext;
import net.lavabucket.hourglass.time.TimeService;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Base interface used for time effects.
 *
 * A time effect is anything that uses the speed of time to affect something in the game.
 *
 * Implementations of this class should be registered to the {@link HourglassRegistry#TIME_EFFECT}
 * registry.
 */
public interface TimeEffect extends IForgeRegistryEntry<TimeEffect> {

    /**
     * Method that is called by {@link TimeService} every tick after time has been adjusted.
     *
     * @param context  the context of the time adjustment
     */
    public void onTimeTick(TimeContext context);

}
