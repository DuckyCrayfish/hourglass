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
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ScreenEvent;
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
                && minecraft.level != null
                && !minecraft.isPaused()
                && clockEnabled()) {

            // Render a clock every tick to prevent clock wobble after getting in bed.
            minecraft.getItemRenderer().getModel(clock, minecraft.level, minecraft.player, 0);
        }
    }

    /**
     * Event listener that is called during GUI rendering. Renders additional GUI elements.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onGuiEvent(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InBedChatScreen && clockEnabled()) {
            renderSleepInterface(event.getScreen(), event.getGuiGraphics());
        }
    }

    /**
     * Renders the interface that displays extra information over the sleep screen.
     *
     * @param screen  the current Screen instance
     * @param guiGraphics graphics renderer
     */
    public static void renderSleepInterface(Screen screen, GuiGraphics guiGraphics) {
        Minecraft minecraft = screen.getMinecraft();

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

        renderClock(guiGraphics, x, y, scale);
    }

    /**
     * Renders a clock on the screen.
     *  @param guiGraphics graphics renderer
     * @param x  the x coordinate of the center of the clock
     * @param y  the y coordinate of the center of the clock
     * @param scale  the size of the clock
     */
    public static void renderClock(GuiGraphics guiGraphics, float x, float y, float scale) {
        scale /= 16F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 0);
        guiGraphics.renderItem(clock, 0, 0);
        guiGraphics.pose().popPose();
    }

    /** {@return true if the bed clock is enabled.} */
    public static boolean clockEnabled() {
        return SERVER_CONFIG.enableSleepFeature.get() && SERVER_CONFIG.displayBedClock.get();
    }

}
