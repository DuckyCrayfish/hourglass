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

/**
 * Alignment of a GUI item on the screen.
 */
public enum ScreenAlignment {
    /** Top left of screen. */
    TOP_LEFT("hourglass.configgui.screenAlignment.topLeft"),
    /** Top center of screen. */
    TOP_CENTER("hourglass.configgui.screenAlignment.topCenter"),
    /** Top right of screen. */
    TOP_RIGHT("hourglass.configgui.screenAlignment.topRight"),
    /** Left center of screen. */
    CENTER_LEFT("hourglass.configgui.screenAlignment.centerLeft"),
    /** Center of screen. */
    CENTER_CENTER("hourglass.configgui.screenAlignment.centerCenter"),
    /** Right center of screen. */
    CENTER_RIGHT("hourglass.configgui.screenAlignment.centerRight"),
    /** Bottom left of screen. */
    BOTTOM_LEFT("hourglass.configgui.screenAlignment.bottomLeft"),
    /** Bottom center of screen. */
    BOTTOM_CENTER("hourglass.configgui.screenAlignment.bottomCenter"),
    /** Bottom right of screen. */
    BOTTOM_RIGHT("hourglass.configgui.screenAlignment.bottomRight");

    private final String translationKey;

    private ScreenAlignment(String translationKey) {
        this.translationKey = translationKey;
    }

    /** {@return the translation key used to display this enum's value} */
    public String getKey() {
        return translationKey;
    }
}
