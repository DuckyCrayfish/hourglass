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

public class SpeedTimeRule extends TimeRule {

    public static final Codec<SpeedTimeRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(SpeedTimeRule::getSpeed),
                TimeRule.conditionsCodec()
            ).apply(instance, SpeedTimeRule::new));

    private final Time speed;

    protected SpeedTimeRule(double speed, List<LootItemCondition> conditions) {
        super(conditions);
        this.speed = new Time(speed);
    }

    @Override
    public Time updateTime(Time oldTime, LootContext context) {
        return oldTime.add(speed);
    }

    @Override
    protected TimeRuleType<?> getType() {
        return TimeRuleTypes.SPEED.get();
    }

    public double getSpeed() {
        return speed.doubleValue();
    }

}
