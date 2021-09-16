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

import static net.lavabucket.hourglass.config.HourglassConfig.CLIENT_CONFIG;
import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This class handles modifications to the sleep interface.
 */
public class SleepGui {

    private static ItemStack clock = new ItemStack(Items.CLOCK);

    /**
     * Event listener that is called once per client tick. Updates the clock texture to prevent
     * clock wobble when getting in bed.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (event.phase == Phase.START
                && CLIENT_CONFIG.preventClockWobble.get()
                && clockEnabled()
                && minecraft.level != null
                && !minecraft.isPaused()) {

            // Render a clock every tick to prevent clock wobble after getting in bed.
            minecraft.getItemRenderer().getModel(clock, minecraft.level, minecraft.player);
        }
    }

    /**
     * Event listener that is called during GUI rendering. Renders additional GUI elements.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onGuiEvent(DrawScreenEvent.Post event) {
        if (event.getGui() instanceof SleepInMultiplayerScreen && clockEnabled()) {

            renderSleepInterface(event.getGui().getMinecraft());
        }
    }

    /**
     * Renders the interface that displays extra information over the sleep screen.
     *
     * @param minecraft  the current Minecraft instance
     */
    public static void renderSleepInterface(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        if (!(screen instanceof SleepInMultiplayerScreen)) {
            return;
        }

        float x, y;
        int scale = CLIENT_CONFIG.clockScale.get();
        int margin = CLIENT_CONFIG.clockMargin.get();
        ScreenAlignment alignment = CLIENT_CONFIG.clockAlignment.get();

        if (alignment == ScreenAlignment.TOP_LEFT
                || alignment == ScreenAlignment.CENTER_LEFT
                || alignment == ScreenAlignment.BOTTOM_LEFT) {
            x = margin;
        } else if (alignment == ScreenAlignment.TOP_CENTER
                || alignment == ScreenAlignment.CENTER_CENTER
                || alignment == ScreenAlignment.BOTTOM_CENTER) {
            x = screen.width / 2 - scale / 2;
        } else {
            x = screen.width - scale - margin;
        }

        if (alignment == ScreenAlignment.TOP_LEFT
                || alignment == ScreenAlignment.TOP_CENTER
                || alignment == ScreenAlignment.TOP_RIGHT) {
            y = margin;
        } else if (alignment == ScreenAlignment.CENTER_LEFT
                || alignment == ScreenAlignment.CENTER_CENTER
                || alignment == ScreenAlignment.CENTER_RIGHT) {
            y = screen.height / 2 - scale / 2;
        } else {
            y = screen.height - scale - margin;
        }

        renderClock(minecraft, x, y, scale);
    }

    /**
     * Renders a clock on the screen.
     *
     * @param minecraft  the current Minecraft instance
     * @param x  the x coordinate of the center of the clock
     * @param y  the y coordinate of the center of the clock
     * @param scale  the size of the clock
     */
    @SuppressWarnings("deprecation")
    public static void renderClock(Minecraft minecraft, float x, float y, float scale) {
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        scale /= 16F;

        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(scale, scale, 0);
        itemRenderer.renderAndDecorateItem(clock, 0, 0);
        RenderSystem.popMatrix();
    }

    public static boolean clockEnabled() {
        return SERVER_CONFIG.enableSleepFeature.get() && SERVER_CONFIG.displayBedClock.get();
    }

}
