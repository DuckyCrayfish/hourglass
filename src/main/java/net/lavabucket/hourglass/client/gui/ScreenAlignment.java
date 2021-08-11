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
    TOP_LEFT("hourglass.configgui.screenAlignment.topLeft"),
    TOP_CENTER("hourglass.configgui.screenAlignment.topCenter"),
    TOP_RIGHT("hourglass.configgui.screenAlignment.topRight"),
    CENTER_LEFT("hourglass.configgui.screenAlignment.centerLeft"),
    CENTER_CENTER("hourglass.configgui.screenAlignment.centerCenter"),
    CENTER_RIGHT("hourglass.configgui.screenAlignment.centerRight"),
    BOTTOM_LEFT("hourglass.configgui.screenAlignment.bottomLeft"),
    BOTTOM_CENTER("hourglass.configgui.screenAlignment.bottomCenter"),
    BOTTOM_RIGHT("hourglass.configgui.screenAlignment.bottomRight");

    private final String translationKey;

    private ScreenAlignment(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getKey() {
        return translationKey;
    }
}
