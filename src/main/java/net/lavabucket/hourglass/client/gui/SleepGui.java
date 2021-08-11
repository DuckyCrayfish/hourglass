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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.BooleanUtils;

import net.lavabucket.hourglass.config.HourglassConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
                && BooleanUtils.isTrue(HourglassConfig.SERVER_CONFIG.displayBedClock.get())
                && BooleanUtils.isTrue(HourglassConfig.CLIENT_CONFIG.preventClockWobble.get())
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
        if (event.getGui() instanceof SleepInMultiplayerScreen
                && BooleanUtils.isTrue(HourglassConfig.SERVER_CONFIG.displayBedClock.get())) {

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
        int scale = HourglassConfig.CLIENT_CONFIG.clockScale.get();
        int margin = HourglassConfig.CLIENT_CONFIG.clockMargin.get();
        ScreenAlignment alignment = HourglassConfig.CLIENT_CONFIG.clockAlignment.get();

        if (alignment == ScreenAlignment.TOP_LEFT
                || alignment == ScreenAlignment.CENTER_LEFT
                || alignment == ScreenAlignment.BOTTOM_LEFT) {
            x = scale / 2 + margin;
        } else if (alignment == ScreenAlignment.TOP_CENTER
                || alignment == ScreenAlignment.CENTER_CENTER
                || alignment == ScreenAlignment.BOTTOM_CENTER) {
            x = screen.width / 2;
        } else {
            x = screen.width - scale / 2 - margin;
        }

        if (alignment == ScreenAlignment.TOP_LEFT
                || alignment == ScreenAlignment.TOP_CENTER
                || alignment == ScreenAlignment.TOP_RIGHT) {
            y = scale / 2 + margin;
        } else if (alignment == ScreenAlignment.CENTER_LEFT
                || alignment == ScreenAlignment.CENTER_CENTER
                || alignment == ScreenAlignment.CENTER_RIGHT) {
            y = screen.height / 2;
        } else {
            y = screen.height - scale / 2 - margin;
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
        IBakedModel model = itemRenderer.getItemModelShaper().getItemModel(Items.CLOCK);
        model = model.getOverrides().resolve(model, clock, minecraft.level, minecraft.player);

        // Replicate ItemRenderer#renderAndDecorateItem(ItemStack, int, int);
        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(scale, -scale, scale);
        MatrixStack matrixStack = new MatrixStack();
        IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
        RenderHelper.setupForFlatItems();

        itemRenderer.render(clock, ItemCameraTransforms.TransformType.GUI, false, matrixStack,
                buffer, 15728880, OverlayTexture.NO_OVERLAY, model);

        buffer.endBatch();
        RenderHelper.setupFor3DItems();
        RenderSystem.popMatrix();
    }

}
