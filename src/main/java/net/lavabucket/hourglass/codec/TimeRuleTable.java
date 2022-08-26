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

import net.lavabucket.hourglass.time.Time;
import net.lavabucket.hourglass.time.TimeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class TimeRuleTable implements TimeProvider, LootContextUser {

    public static final Codec<List<TimeRule>> CODEC = TimeRule.CODEC.listOf().fieldOf("rules").codec();

    public static final ResourceLocation VANILLA_TABLE_KEY = new ResourceLocation("vanilla");
    public static final TimeRuleTable VANILLA_TABLE = new TimeRuleTable(List.of());

    public static final TimeRule DEFAULT = new DefaultTimeRule(List.of());

    private ResourceLocation id;
    private final List<TimeRule> rules;

    public TimeRuleTable(List<TimeRule> rules) {
        this.rules = rules;
    }

    @Override
    public Time updateTime(Time oldTime, LootContext context) {
        for (TimeRule rule : this.rules) {
            Optional<TimeRule> result = rule.collapse(context);
            if (result.isPresent()) {
                return result.get().updateTime(oldTime, context);
            }
        }

        return DEFAULT.updateTime(oldTime, context);
    }

    // TODO: Attempt to check for unreachable entries here and in TimeRuleGroup.
    @Override
    public void validate(ValidationContext context) {
        context.enterTable("{" + this.getId() + "}", this.getId());

        for (int i = 0; i < this.rules.size(); i++) {
            this.rules.get(i).validate(context.forChild(".rules[" + i + "]"));
        }
    }

    public List<TimeRule> getRules() {
        return rules;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        if (this.id != null) {
            throw new IllegalStateException("Cannot rename time rule tables.");
        }
        this.id = id;
    }

}
