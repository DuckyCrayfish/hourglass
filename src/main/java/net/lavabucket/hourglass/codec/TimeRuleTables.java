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

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TimeRuleTables extends SimpleJsonResourceReloadListener {

    public static TimeRuleTables INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    private final PredicateManager predicateManager;
    private ImmutableMap<ResourceLocation, TimeRuleTable> tables = ImmutableMap.of();

    public TimeRuleTables(PredicateManager predicateManager) {
        super(GSON, "time_rules");
        this.predicateManager = predicateManager;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager resourceManager,
            ProfilerFiller profiler) {

        ImmutableMap.Builder<ResourceLocation, TimeRuleTable> mapBuilder = ImmutableMap.builder();

        if (input.remove(TimeRuleTable.VANILLA_TABLE_KEY) != null) {
            LOGGER.warn("Datapack tried to redefine {} time rule table, ignoring", TimeRuleTable.VANILLA_TABLE_KEY);
        }

        input.forEach((key, jsonElement) -> {
            TimeRuleTable.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> LOGGER.error("Could not parse time rule table {}: {}", key, error))
                    .ifPresent(rules -> {
                        TimeRuleTable ruleTable = new TimeRuleTable(rules);
                        ruleTable.setId(key);
                        mapBuilder.put(key, ruleTable);
                    });
        });

        tables = mapBuilder.build();

        ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, key -> null);
        validationContext.setParams(LootContextParamSets.EMPTY);
        tables.forEach((key, ruleTable) -> ruleTable.validate(validationContext));
        validationContext.getProblems().forEach((key, errorMessage) -> {
            LOGGER.warn("Found validation problem in {}: {}", key, errorMessage);
        });
    }

    public TimeRuleTable get(ResourceLocation key) {
        return this.tables.getOrDefault(key, TimeRuleTable.VANILLA_TABLE);
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {
        ReloadableServerResources serverResources = event.getServerResources();
        INSTANCE = new TimeRuleTables(serverResources.getPredicateManager());
        event.addListener(INSTANCE);
    }

}
