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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.lavabucket.hourglass.codec.TimeRuleTypes.TimeRuleType;
import net.lavabucket.hourglass.time.Time;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TimeRuleGroup extends TimeRule {

    public static Codec<TimeRuleGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TimeRule.CODEC.listOf().fieldOf("rules").forGetter(TimeRuleGroup::getChildren),
        TimeRule.conditionsCodec()
    ).apply(instance, TimeRuleGroup::new));

    private final List<TimeRule> children;

    protected TimeRuleGroup(List<TimeRule> children, List<LootItemCondition> conditions) {
        super(conditions);
        this.children = children;
    }

    @Override
    public Time updateTime(Time oldTime, LootContext context) {
        return null;
    }

    @Override
    protected TimeRuleType<?> getType() {
        return TimeRuleTypes.GROUP.get();
    }

    @Override
    public Optional<TimeRule> collapse(LootContext context) {
        return this.children.stream()
                .map(rule -> rule.collapse(context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);

        if (this.children.isEmpty()) {
            context.reportProblem("Empty rule group");
        }

        for(int i = 0; i < this.children.size(); i++) {
            this.children.get(i).validate(context.forChild(".rules[" + i + "]"));
        }
    }

    public List<TimeRule> getChildren() {
        return this.children;
    }

}
