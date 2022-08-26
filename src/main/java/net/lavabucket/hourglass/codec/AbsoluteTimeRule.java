/*
 * Copyright (C) 2022 Nick Iacullo
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

package net.lavabucket.hourglass.codec;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.lavabucket.hourglass.codec.TimeRuleTypes.TimeRuleType;
import net.lavabucket.hourglass.time.Time;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AbsoluteTimeRule extends TimeRule {

    public static final Codec<AbsoluteTimeRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("time").forGetter(AbsoluteTimeRule::getTime),
                TimeRule.conditionsCodec()
            ).apply(instance, AbsoluteTimeRule::new));

    private final Time time;

    protected AbsoluteTimeRule(double time, List<LootItemCondition> conditions) {
        super(conditions);
        this.time = new Time(time);
    }

    @Override
    public Time updateTime(Time oldTime, LootContext context) {
        return time;
    }

    @Override
    protected TimeRuleType<?> getType() {
        return TimeRuleTypes.ABSOLUTE.get();
    }

    public double getTime() {
        return time.doubleValue();
    }

}
