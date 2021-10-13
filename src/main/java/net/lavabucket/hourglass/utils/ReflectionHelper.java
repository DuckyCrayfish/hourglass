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

package net.lavabucket.hourglass.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * This class contains all reflection methods and fields used in Hourglass.
 *
 * <p>Encapsulating all reflected members in a single class ensures they can all be accounted for
 * simultaneously when porting to different Minecraft versions.
 */
public final class ReflectionHelper {

    /** Reflection reference to {@code ServerLevel#sleepStatus} field. */
    public static final Field FIELD_SLEEP_STATUS = ObfuscationReflectionHelper.findField(ServerLevel.class, "f_143245_");
    /** Reflection reference to {@code ServerLevel#tickBlockEntities()} method. */
    public static final Method METHOD_TICK_BLOCK_ENTITIES = ObfuscationReflectionHelper.findMethod(Level.class, "m_46463_");
    /** Reflection reference to {@code LivingEntity#tickEffects()} method. */
    public static final Method METHOD_TICK_EFFECTS = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_21217_");

    // Private constructor to forbid object instantiation.
    private ReflectionHelper() {}

}