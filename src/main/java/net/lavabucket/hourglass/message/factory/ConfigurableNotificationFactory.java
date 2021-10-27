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

package net.lavabucket.hourglass.message.factory;

import java.util.function.Supplier;

import net.lavabucket.hourglass.message.Notification;
import net.lavabucket.hourglass.message.target.NotificationTarget;
import net.lavabucket.hourglass.message.target.TargetContext;
import net.lavabucket.hourglass.message.textbuilder.TemplateTextBuilder;
import net.lavabucket.hourglass.message.textbuilder.TextBuilder;
import net.lavabucket.hourglass.message.textbuilder.TranslatableTextBuilder;
import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.utils.Utils;
import net.minecraft.network.chat.ChatType;

public class ConfigurableNotificationFactory {

    protected final Supplier<NotificationTarget> targetSupplier;
    protected final Supplier<String> translationKeySupplier;
    protected final Supplier<String> templateSupplier;
    protected final Supplier<ChatType> typeSupplier;
    protected final Supplier<Boolean> translationModeSupplier;

    public ConfigurableNotificationFactory(Supplier<NotificationTarget> targetSupplier,
            Supplier<String> translationKeySupplier,
            Supplier<String> templateSupplier,
            Supplier<ChatType> typeSupplier,
            Supplier<Boolean> translationModeSupplier) {
        this.targetSupplier = targetSupplier;
        this.translationKeySupplier = translationKeySupplier;
        this.templateSupplier = templateSupplier;
        this.typeSupplier = typeSupplier;
        this.translationModeSupplier = translationModeSupplier;
    }

    public Notification create(TargetContext context) {
        TextBuilder builder = getMessageBuilder(context);
        return create(context, builder);
    }

    public Notification create(TargetContext context, TextBuilder builder) {
        return new Notification(targetSupplier.get(), context, typeSupplier.get(), builder);
    }

    protected TextBuilder getMessageBuilder(TargetContext context) {
        if (translationModeSupplier.get()) {
            return new TranslatableTextBuilder(translationKeySupplier.get());
        } else {
            return new TemplateTextBuilder(templateSupplier.get());
        }
    }

    public static class Builder {

        protected Supplier<NotificationTarget> targetSupplier;
        protected Supplier<String> translationKeySupplier;
        protected Supplier<String> templateSupplier;
        protected Supplier<ChatType> typeSupplier;
        protected Supplier<Boolean> translationModeSupplier;

        public Builder target(Supplier<NotificationTarget> targetSupplier) {
            this.targetSupplier = targetSupplier;
            return this;
        }

        public Builder stringTarget(Supplier<String> keySupplier) {
            this.targetSupplier = () -> {
                return Utils.parseRegistryKey(HourglassRegistry.MESSAGE_TARGET, keySupplier.get());
            };
            return this;
        }

        public Builder translationKey(Supplier<String> translationKeySupplier) {
            this.translationKeySupplier = translationKeySupplier;
            return this;
        }

        public Builder template(Supplier<String> templateSupplier) {
            this.templateSupplier = templateSupplier;
            return this;
        }

        public Builder type(Supplier<ChatType> typeSupplier) {
            this.typeSupplier = typeSupplier;
            return this;
        }

        public Builder translationMode(Supplier<Boolean> translationModeSupplier) {
            this.translationModeSupplier = translationModeSupplier;
            return this;
        }

        public ConfigurableNotificationFactory create() {
            return new ConfigurableNotificationFactory(targetSupplier,
                    translationKeySupplier,
                    templateSupplier,
                    typeSupplier,
                    translationModeSupplier);
        }

    }

}
