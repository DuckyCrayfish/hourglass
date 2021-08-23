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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.IteratableOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

public final class ConfigScreen extends Screen {
    private static final int TITLE_MARGIN = 8;
    private static final int OPTIONS_LIST_MARGIN = 24;
    private static final int OPTIONS_LIST_BOTTOM_MARGIN = 32;
    private static final int OPTION_HEIGHT = 25;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 6;

    private static final TranslationTextComponent TEXT_TITLE = new TranslationTextComponent("hourglass.configgui.title");
    private static final String KEY_DONE = "gui.done";
    private static final String KEY_CLOCK_ALIGNMENT = "hourglass.configgui.clockAlignment";
    private static final String KEY_CLOCK_SCALE = "hourglass.configgui.clockScale";
    private static final String KEY_CLOCK_MARGIN = "hourglass.configgui.clockMargin";
    private static final String KEY_PREVENT_CLOCK_WOBBLE = "hourglass.configgui.preventClockWobble";
    private static final String KEY_PIXELS = "hourglass.configgui.pixels";

    protected Screen lastScreen;
    protected OptionsRowList optionsList;
    protected Button doneButton;

    private ScreenAlignment clockAlignment;
    private int clockScale;
    private int clockMargin;
    private boolean preventClockWobble;

    public static void register(ModLoadingContext context) {
        context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, screen) -> new ConfigScreen(screen));
    }

    public ConfigScreen(Screen lastScreen) {
        super(TEXT_TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        fetchSettings();

        optionsList = new OptionsRowList(minecraft, width, height, OPTIONS_LIST_MARGIN,
                height - OPTIONS_LIST_BOTTOM_MARGIN, OPTION_HEIGHT);

        optionsList.func_214333_a(new IteratableOption(KEY_CLOCK_ALIGNMENT,
                (settings, value) -> clockAlignment = ScreenAlignment.values()[
                        (clockAlignment.ordinal() + value) % ScreenAlignment.values().length],
                (settings, option) -> option.getDisplayString() + I18n.format(clockAlignment.getKey())));

        optionsList.func_214333_a(new SliderPercentageOption(KEY_CLOCK_SCALE, 0.0, 128, 4.0F,
                settings -> (double) clockScale,
                (settings, value) -> clockScale = value.intValue(),
                (settings, option) -> option.getDisplayString() + I18n.format(KEY_PIXELS, (int) option.get(settings))));

        optionsList.func_214333_a(new SliderPercentageOption(KEY_CLOCK_MARGIN, 0.0, 128, 4.0F,
                settings -> (double) clockMargin,
                (settings, value) -> clockMargin = value.intValue(),
                (settings, option) -> option.getDisplayString() + I18n.format(KEY_PIXELS, (int) option.get(settings))));

        optionsList.func_214333_a(new BooleanOption(KEY_PREVENT_CLOCK_WOBBLE,
                settings -> preventClockWobble,
                (settings, value) -> preventClockWobble = value));

        children.add(optionsList);

        doneButton = new Button((width - BUTTON_WIDTH) / 2,
                height - BUTTON_HEIGHT - DONE_BUTTON_BOTTOM_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format(KEY_DONE), button -> onClose());

        addButton(doneButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        optionsList.render(mouseX, mouseY, partialTicks);
        drawCenteredString(font, title.getString(), width / 2, TITLE_MARGIN, 0xFFFFFF);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        saveSettings();
        minecraft.displayGuiScreen(lastScreen);
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

}
