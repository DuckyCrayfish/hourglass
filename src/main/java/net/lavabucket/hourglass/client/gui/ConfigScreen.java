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

import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.CycleOption;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;

public final class ConfigScreen extends Screen {
    private static final int TITLE_MARGIN = 8;
    private static final int OPTIONS_LIST_MARGIN = 24;
    private static final int OPTIONS_LIST_BOTTOM_MARGIN = 32;
    private static final int OPTION_HEIGHT = 25;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 6;

    private static final TranslatableComponent TEXT_TITLE = new TranslatableComponent("hourglass.configgui.title");
    private static final TranslatableComponent TEXT_CLOCK_ALIGNMENT = new TranslatableComponent("hourglass.configgui.clockAlignment");
    private static final TranslatableComponent TEXT_CLOCK_SCALE = new TranslatableComponent("hourglass.configgui.clockScale");
    private static final TranslatableComponent TEXT_CLOCK_MARGIN = new TranslatableComponent("hourglass.configgui.clockMargin");
    private static final TranslatableComponent TEXT_PREVENT_CLOCK_WOBBLE = new TranslatableComponent("hourglass.configgui.preventClockWobble");

    protected Screen lastScreen;
    protected OptionsList optionsList;
    protected Button doneButton;

    private ScreenAlignment clockAlignment;
    private int clockScale;
    private int clockMargin;
    private boolean preventClockWobble;

    public static void register(ModLoadingContext context) {
        context.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen)));
    }

    public ConfigScreen(Screen lastScreen) {
        super(TEXT_TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        fetchSettings();

        optionsList = new OptionsList(minecraft, width, height, OPTIONS_LIST_MARGIN,
                height - OPTIONS_LIST_BOTTOM_MARGIN, OPTION_HEIGHT);

        optionsList.addBig(CycleOption.create(TEXT_CLOCK_ALIGNMENT.getKey(),
                Arrays.asList(ScreenAlignment.values()),
                value -> new TranslatableComponent(value.getKey()),
                options -> clockAlignment,
                (options, option, value) -> clockAlignment = value));

        optionsList.addBig(new ProgressOption(TEXT_CLOCK_SCALE.getKey(), 0.0, 128, 4.0F,
                settings -> (double) clockScale,
                (settings, value) -> clockScale = value.intValue(),
                (settings, option) -> pixelValueTextComponent(TEXT_CLOCK_SCALE, option.get(settings))));

        optionsList.addBig(new ProgressOption(TEXT_CLOCK_MARGIN.getKey(), 0.0, 128, 4.0F,
                settings -> (double) clockMargin,
                (settings, value) -> clockMargin = value.intValue(),
                (settings, option) -> pixelValueTextComponent(TEXT_CLOCK_MARGIN, option.get(settings))));

        optionsList.addBig(CycleOption.createOnOff(
                TEXT_PREVENT_CLOCK_WOBBLE.getKey(),
                settings -> preventClockWobble,
                (options, option, value) -> preventClockWobble = value));

        addWidget(optionsList);

        doneButton = new Button((width - BUTTON_WIDTH) / 2,
                height - BUTTON_HEIGHT - DONE_BUTTON_BOTTOM_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.GUI_DONE, button -> onClose());

        addRenderableWidget(doneButton);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        optionsList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, font, title.getString(), width / 2, TITLE_MARGIN, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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

    public static TranslatableComponent genericValueTextComponent(Object... args) {
        return new TranslatableComponent("options.generic_value", args);
    }

    public static TranslatableComponent pixelValueTextComponent(Component name, double count) {
        return genericValueTextComponent(name,
                new TranslatableComponent("hourglass.configgui.pixels", (int) count));
    }

}

