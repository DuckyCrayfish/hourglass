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

package net.lavabucket.hourglass.wrappers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * This class acts as a wrapper for {@link Component} to increase consistency between Minecraft
 * versions.
 *
 * <p>Since the {@link Component} class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link Component}. This class consolidates these variations into itself,
 * allowing other classes to depend on it instead.
 */
public class TextWrapper extends Wrapper<Component> {

    /**
     * Instantiates a new object.
     * @param object  the text component to wrap
     */
    public TextWrapper(Component object) {
        super(object);
    }

    /**
     * Creates a new {@code TextWrapper} for a translatable text component.
     * @param key  the translation key for the component text
     * @return wrapped translatable text component for the provided key
     */
    public static TextWrapper translation(String key) {
        return new TextWrapper(new TranslatableComponent(key));
    }

    /**
     * Creates a new {@code TextWrapper} for a translatable text component with arguments.
     * @param key  the translation key for the component text
     * @param args  the arguments used for text substitution in the translated text
     * @return wrapped translatable text component for the provided key
     */
   public static TextWrapper translation(String key, Object... args) {
       return new TextWrapper(new TranslatableComponent(key, args));
   }

    /**
     * Creates a new {@code TextWrapper} for a text component with the specified message.
     * @param message  the component's literal message
     * @return wrapped text component with the provided message
     */
    public static TextWrapper literal(String message) {
        return new TextWrapper(new TextComponent(message));
    }

}
