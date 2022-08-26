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
import java.util.Optional;
import java.util.function.Predicate;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.lavabucket.hourglass.codec.TimeRuleTypes.TimeRuleType;
import net.lavabucket.hourglass.time.TimeProvider;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class TimeRule implements TimeProvider, LootContextUser {

    public static Codec<TimeRule> CODEC = TimeRuleTypes.REGISTRY.get().getCodec().dispatch(TimeRule::getType, TimeRuleType::codec);

    public static <T extends TimeRule> RecordCodecBuilder<T, List<LootItemCondition>> conditionsCodec() {
        return ConditionsCodec.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(TimeRule::getConditions);
    }

    protected final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;

    protected TimeRule(List<LootItemCondition> conditions) {
        this.conditions = conditions;
        this.compositeCondition = LootItemConditions.andConditions(conditions.toArray(new LootItemCondition[0]));
    }

    protected abstract TimeRuleType<?> getType();

    public List<LootItemCondition> getConditions() {
        return this.conditions;
    }

    public Optional<TimeRule> collapse(LootContext context) {
        if (this.conditionsSatisfied(context)) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public boolean conditionsSatisfied(LootContext context) {
        return this.compositeCondition.test(context);
    }

    @Override
    public void validate(ValidationContext context) {
        for (int i = 0; i < this.conditions.size(); i++) {
            this.conditions.get(i).validate(context.forChild(".conditions[" + i + "]"));
        }
    }

}
