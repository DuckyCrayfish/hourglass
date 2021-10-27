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

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/** This class consists of general {@code static} utility functions used in Hourglass. */
public final class Utils {

    /**
     * Parses {@code path} into a {@code ResourceLocation} object using {@code namespace} as the
     * default namespace if {@code path} does not contain one.
     *
     * @param namespace  the default namespace to use if {@code path} does not contain one
     * @param path  the string to parse
     * @return the parsed {@link ResourceLocation}
     */
    public static ResourceLocation defaultNamespaceResourceLocation(String namespace, String path) {
        int i = path.indexOf(':');
        if (i >= 0) {
            path = path.substring(i + 1, path.length());
            if (i >= 1) {
                namespace = path.substring(0, i);
            }
        }

        return new ResourceLocation(namespace, path);
    }

    /**
     * Parses {@code key} into a {@code ResourceLocation} and uses it to return the corresponding
     * entry from {@code registry}.
     *
     * <p>The registry's namespace will be used by default if {@code key} does not contain one.
     *
     * @param <T>  the type of the registry entry
     * @param registry  the registry to search
     * @param key  the string to parse into a registry entry key
     * @return the registry entry corresponding to {@code key}
     */
    public static <T extends IForgeRegistryEntry<T>> T parseRegistryKey(IForgeRegistry<T> registry,
            String key) {

        String registryNamespace = registry.getRegistryName().getNamespace();
        return parseRegistryKey(registry, registryNamespace, key);
    }

    /**
     * Parses {@code key} into a {@code ResourceLocation} and uses it to return the corresponding
     * entry from {@code registry}.
     *
     * <p>{@code defaultNamespace} will be used as the default namespace if {@code key} does not
     * contain one.
     *
     * @param <T>  the type of the registry entry
     * @param registry  the registry to search
     * @param defaultNamespace  the namespace to use if {@code key} does not contain one
     * @param key  the string to parse into a registry entry key
     * @return the registry entry corresponding to {@code key}
     */
    public static <T extends IForgeRegistryEntry<T>> T parseRegistryKey(IForgeRegistry<T> registry,
            String defaultNamespace, String key) {
        ResourceLocation location = defaultNamespaceResourceLocation(defaultNamespace, key);
        return registry.getValue(location);
    }

    /**
     * Parses {@code key} into a {@code ResourceLocation} and uses it to test if it corresponds to
     * a valid entry in {@code registry}.
     *
     * <p>The registry's namespace will be used by default if {@code key} does not contain one.
     *
     * @param registry  the registry to search
     * @param key  the string to parse into a registry entry key
     * @return true if {@code key} corresponds to a valid registry entry, false otherwise
     */
    public static boolean isValidRegistryKey(IForgeRegistry<?> registry, String key) {
        String registryNamespace = registry.getRegistryName().getNamespace();
        return isValidRegistryKey(registry, registryNamespace, key);
    }

    /**
     * Parses {@code key} into a {@code ResourceLocation} and uses it to test if it corresponds to
     * a valid entry in {@code registry}.
     *
     * <p>{@code defaultNamespace} will be used as the default namespace if {@code key} does not
     * contain one.
     *
     * @param registry  the registry to search
     * @param defaultNamespace  the namespace to use if {@code key} does not contain one
     * @param key  the string to parse into a registry entry key
     * @return true if {@code key} corresponds to a valid registry entry, false otherwise
     */
    public static boolean isValidRegistryKey(IForgeRegistry<?> registry, String defaultNamespace,
            String key) {
        try {
            ResourceLocation rl = Utils.defaultNamespaceResourceLocation(defaultNamespace, key);
            return registry.containsKey(rl);
        } catch (Exception e) {
            return false;
        }
    }

    // Private constructor to prohibit instantiation.
    private Utils() {}

}
