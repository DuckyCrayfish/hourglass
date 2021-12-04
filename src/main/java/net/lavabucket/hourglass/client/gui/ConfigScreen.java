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

import com.mojang.blaze3d.vertex.PoseStack;

import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.client.CycleOption;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
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
        context.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen)));
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

        optionsList.addBig(CycleOption.create(KEY_CLOCK_ALIGNMENT,
                Arrays.asList(ScreenAlignment.values()),
                value -> translation(value.getKey()).get(),
                options -> clockAlignment,
                (options, option, value) -> clockAlignment = value));

        optionsList.addBig(new ProgressOption(KEY_CLOCK_SCALE, 0.0, 128, 4.0F,
                settings -> (double) clockScale,
                (settings, value) -> clockScale = value.intValue(),
                (settings, option) -> pixelOptionText(KEY_CLOCK_SCALE, option.get(settings)).get()));

        optionsList.addBig(new ProgressOption(KEY_CLOCK_MARGIN, 0.0, 128, 4.0F,
                settings -> (double) clockMargin,
                (settings, value) -> clockMargin = value.intValue(),
                (settings, option) -> pixelOptionText(KEY_CLOCK_MARGIN, option.get(settings)).get()));

        optionsList.addBig(CycleOption.createOnOff(
                KEY_PREVENT_CLOCK_WOBBLE,
                settings -> preventClockWobble,
                (options, option, value) -> preventClockWobble = value));

        addWidget(optionsList);

        int doneX = (width - BUTTON_WIDTH) / 2;
        int doneY = height - BUTTON_HEIGHT - DONE_BUTTON_BOTTOM_MARGIN;
        TextWrapper doneText = translation(KEY_DONE);
        doneButton = new Button(doneX, doneY, BUTTON_WIDTH, BUTTON_HEIGHT, doneText.get(),
                button -> onClose());

        addRenderableWidget(doneButton);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        optionsList.render(stack, mouseX, mouseY, partialTicks);
        drawCenteredString(stack, font, title.getString(), width / 2, TITLE_MARGIN, 0xFFFFFF);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        saveSettings();
        minecraft.setScreen(lastScreen);
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
