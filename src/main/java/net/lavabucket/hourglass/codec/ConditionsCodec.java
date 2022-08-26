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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ConditionsCodec implements Codec<LootItemCondition> {

    private static final Gson GSON = Deserializers.createConditionSerializer().create();
    public static final ConditionsCodec CODEC = new ConditionsCodec();

    @Override
    public <T> DataResult<T> encode(LootItemCondition input, DynamicOps<T> ops, T prefix) {
        JsonElement json = GSON.toJsonTree(input, LootItemCondition.class);
        Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, json);
        T result = dynamic.convert(ops).getValue();
        return DataResult.success(result);
    }

    @Override
    public <T> DataResult<Pair<LootItemCondition, T>> decode(DynamicOps<T> ops, T input) {
        Dynamic<T> dynamic = new Dynamic<>(ops, input);
        JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
        LootItemCondition output = GSON.fromJson(json, LootItemCondition.class);
        return DataResult.success(Pair.of(output, ops.empty()));
    }

}
