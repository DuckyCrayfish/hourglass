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
import static net.lavabucket.hourglass.wrappers.TextWrapper.translation;

import java.util.Arrays;

import com.mojang.serialization.Codec;

import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

/**
 * Mod configuration screen, accessed from the mod list in the main menu.
 */
public final class ConfigScreen extends Screen {
    private static final int TITLE_MARGIN = 8;
    private static final int OPTIONS_LIST_MARGIN = 24;
    private static final int OPTIONS_LIST_BOTTOM_MARGIN = 32;
    private static final int OPTION_HEIGHT = 25;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 6;

    private static final String KEY_TITLE = "hourglass.configgui.title";
    private static final String KEY_CLOCK_ALIGNMENT = "hourglass.configgui.clockAlignment";
    private static final String KEY_CLOCK_SCALE = "hourglass.configgui.clockScale";
    private static final String KEY_CLOCK_MARGIN = "hourglass.configgui.clockMargin";
    private static final String KEY_PREVENT_CLOCK_WOBBLE = "hourglass.configgui.preventClockWobble";
    private static final String KEY_PIXELS = "hourglass.configgui.pixels";
    private static final String KEY_DONE = "gui.done";
    private static final String KEY_GENERIC_OPTION = "options.generic_value";

    /** The screen that was active prior to this screen opening. */
    protected Screen lastScreen;
    /** The options list used for the settings in this screen. */
    protected OptionsList optionsList;
    /** This screen's "done" button. */
    protected Button doneButton;

    private ScreenAlignment clockAlignment;
    private int clockScale;
    private int clockMargin;
    private boolean preventClockWobble;

    /**
     * Registers this screen as the mod's config screen.
     * @param event  the event, provided by the mod event bus
     */
    @SubscribeEvent
    public static void onConstructModEvent(FMLConstructModEvent event) {
        final ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((mc, screen) -> new ConfigScreen(screen)));
    }

    /**
     * Instantiates a new {@code ConfigScreen} object.
     * @param lastScreen  the screen that was active prior to this screen opening
     */
    public ConfigScreen(Screen lastScreen) {
        super(translation(KEY_TITLE).get());
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        fetchSettings();

        optionsList = new OptionsList(minecraft, width, height, OPTIONS_LIST_MARGIN,
                height - OPTIONS_LIST_BOTTOM_MARGIN, OPTION_HEIGHT);

        optionsList.addBig(new OptionInstance<>(
                KEY_CLOCK_ALIGNMENT,
                OptionInstance.noTooltip(),
                (i, value) -> translation(value.getKey()).get(),
                new OptionInstance.Enum<>(Arrays.asList(ScreenAlignment.values()), Codec.STRING.xmap(ScreenAlignment::valueOf, Enum::name)),
                clockAlignment,
                value -> clockAlignment = value));

        optionsList.addBig(new OptionInstance<Double>(
                KEY_CLOCK_SCALE,
                OptionInstance.noTooltip(),
                (i, value) -> pixelOptionText(KEY_CLOCK_SCALE, value).get(),
                (new OptionInstance.IntRange(0, 128)).xmap((value) -> (double) value, (value) -> value.intValue()),
                Codec.doubleRange(0.0D, 128.0D),
                Double.valueOf(clockScale),
                value -> clockScale = value.intValue()));

        optionsList.addBig(new OptionInstance<Double>(
                KEY_CLOCK_MARGIN,
                OptionInstance.noTooltip(),
                (i, value) -> pixelOptionText(KEY_CLOCK_MARGIN, value).get(),
                (new OptionInstance.IntRange(0, 128)).xmap((value) -> (double) value, (value) -> value.intValue()),
                Codec.doubleRange(0.0D, 128.0D),
                Double.valueOf(clockMargin),
                value -> clockMargin = value.intValue()));

        optionsList.addBig(OptionInstance.createBoolean(
                KEY_PREVENT_CLOCK_WOBBLE,
                preventClockWobble,
                value -> preventClockWobble = value));

        addWidget(optionsList);

        int doneX = (width - BUTTON_WIDTH) / 2;
        int doneY = height - BUTTON_HEIGHT - DONE_BUTTON_BOTTOM_MARGIN;
        TextWrapper doneText = translation(KEY_DONE);
        doneButton = Button.builder(doneText.get(), button -> onClose())
                .pos(doneX, doneY)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        addRenderableWidget(doneButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        optionsList.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(font, title.getString(), width / 2, TITLE_MARGIN, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        saveSettings();
        if (minecraft != null) {
            minecraft.setScreen(lastScreen);
        }
    }

    private void fetchSettings() {
        clockAlignment = CLIENT_CONFIG.clockAlignment.get();
        clockScale = CLIENT_CONFIG.clockScale.get();
        clockMargin = CLIENT_CONFIG.clockMargin.get();
        preventClockWobble = CLIENT_CONFIG.preventClockWobble.get();
    }

    private void saveSettings() {
        CLIENT_CONFIG.clockAlignment.set(clockAlignment);
        CLIENT_CONFIG.clockScale.set(clockScale);
        CLIENT_CONFIG.clockMargin.set(clockMargin);
        CLIENT_CONFIG.preventClockWobble.set(preventClockWobble);
    }

    /**
     * Returns a wrapped translatable text component for a generic option that includes a pixel
     * count.
     *
     * @param key  the translation key for the option
     * @param pixelCount  the pixel count to display
     * @return the new wrapped text component
     * @deprecated Do not use, will be removed.
     */
    public static TextWrapper pixelOptionText(String key, double pixelCount) {
        return translation(KEY_GENERIC_OPTION,
                translation(key).get(),
                translation(KEY_PIXELS, (int) pixelCount).get());
    }

}
