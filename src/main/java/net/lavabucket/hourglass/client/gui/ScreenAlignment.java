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

package net.lavabucket.hourglass.client.gui;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

/**
 * Alignment of a GUI item on the screen.
 */
public enum ScreenAlignment implements TranslatableEnum {
    /** Top left of screen. */
    TOP_LEFT("hourglass.configuration.screenAlignment.topLeft"),
    /** Top center of screen. */
    TOP_CENTER("hourglass.configuration.screenAlignment.topCenter"),
    /** Top right of screen. */
    TOP_RIGHT("hourglass.configuration.screenAlignment.topRight"),
    /** Left center of screen. */
    CENTER_LEFT("hourglass.configuration.screenAlignment.centerLeft"),
    /** Center of screen. */
    CENTER_CENTER("hourglass.configuration.screenAlignment.centerCenter"),
    /** Right center of screen. */
    CENTER_RIGHT("hourglass.configuration.screenAlignment.centerRight"),
    /** Bottom left of screen. */
    BOTTOM_LEFT("hourglass.configuration.screenAlignment.bottomLeft"),
    /** Bottom center of screen. */
    BOTTOM_CENTER("hourglass.configuration.screenAlignment.bottomCenter"),
    /** Bottom right of screen. */
    BOTTOM_RIGHT("hourglass.configuration.screenAlignment.bottomRight");

    private final String translationKey;

    private ScreenAlignment(String translationKey) {
        this.translationKey = translationKey;
    }

    /** {@return the message component used to display this enum's value} */
    public Component getTranslatedName() {
        return Component.translatable(translationKey);
    }
}
