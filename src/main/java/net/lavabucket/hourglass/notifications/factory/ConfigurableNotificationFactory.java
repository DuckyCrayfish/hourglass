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

package net.lavabucket.hourglass.notifications.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.lavabucket.hourglass.notifications.GenericNotification;
import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.notifications.target.TargetContext;
import net.lavabucket.hourglass.notifications.target.TargetParam;
import net.lavabucket.hourglass.notifications.textbuilder.TemplateTextBuilder;
import net.lavabucket.hourglass.notifications.textbuilder.TextBuilder;
import net.lavabucket.hourglass.notifications.textbuilder.TranslatableTextBuilder;
import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.utils.Utils;
import net.minecraft.network.chat.ChatType;

/**
 * A factory class for {@link GenericNotification} objects that fetches the required arguments using
 * {@code Supplier}s at notification creation time.
 *
 * <p>The method in which the notification's content is generated depends on whether or not translation
 * mode is used.
 *
 * <p>If translation mode is enabled, the content is generated using a
 * {@link TranslatableTextBuilder} object that is passed a translation key supplied by the
 * {@code translationKeySupplier} from the object's constructor.
 *
 * <p>If translation mode is disabled, the content is generated using a {@link TemplateTextBuilder}
 * object that is passed a template {@code String} supplied by the {@code templateSupplier} from the
 * object's constructor.
 */
public class ConfigurableNotificationFactory {

    /** The {@code Supplier} for the notification's target. */
    protected final Supplier<NotificationTarget> targetSupplier;
    /** The {@code Supplier} for the notification's {@link TranslatableTextBuilder} translation key. */
    protected final Supplier<String> translationKeySupplier;
    /** The {@code Supplier} for the notification's {@link TemplateTextBuilder} template string. */
    protected final Supplier<String> templateSupplier;
    /** The {@code Supplier} for the notification's message {@code ChatType}. */
    protected final Supplier<ChatType> typeSupplier;
    /** The {@code Supplier} for the translation mode flag. */
    protected final Supplier<Boolean> translationModeSupplier;
    /** The required {@code TargetParam} parameters of the notification. */
    protected final ImmutableSet<TargetParam<?>> requiredParams;

    /**
     * Creates a new instance.
     * @param targetSupplier  {@code Supplier} for the notification's target
     * @param translationKeySupplier  {@code Supplier} for the notification translation key
     * @param templateSupplier  {@code Supplier} for the notification template
     * @param typeSupplier  {@code Supplier} for the notification's message {@code ChatType}
     * @param translationModeSupplier  {@code Supplier} for the translation mode flag
     */
    public ConfigurableNotificationFactory(Supplier<NotificationTarget> targetSupplier,
            Supplier<String> translationKeySupplier,
            Supplier<String> templateSupplier,
            Supplier<ChatType> typeSupplier,
            Supplier<Boolean> translationModeSupplier,
            Set<TargetParam<?>> requiredParams) {
        this.targetSupplier = targetSupplier;
        this.translationKeySupplier = translationKeySupplier;
        this.templateSupplier = templateSupplier;
        this.typeSupplier = typeSupplier;
        this.translationModeSupplier = translationModeSupplier;
        this.requiredParams = ImmutableSet.copyOf(requiredParams);
    }

    /**
     * Returns true if this notification factory is compatible with {@code target}, false otherwise.
     * @param target  the target to check
     * @return true if this notification factory is compatible with {@code target}, false otherwise
     */
    public boolean targetCompatible(NotificationTarget target) {
        return requiredParams.containsAll(target.getRequiredParams());
    }

    /**
     * {@return the set of required parameters not contained in {@code params}}
     * @param context  the parameters to check
     */
    public Set<TargetParam<?>> getMissingParams(Set<TargetParam<?>> params) {
        return Sets.difference(requiredParams, params);
    }

    public GenericNotification create(TargetContext context) {
        TextBuilder builder = getContentBuilder(context);
        return create(context, builder);
    }

    /**
     * Creates a new notification using the provided {@code TextBuilder}.
     * @param context  the {@code TargetContext} for the notification
     * @param builder  the {@code TextBuilder} for the notification message content
     * @return  the new notification
     */
    protected GenericNotification create(TargetContext context, TextBuilder builder) {
        NotificationTarget target = targetSupplier.get();
        if (!targetCompatible(target)) {
            throw new IllegalArgumentException("Target " + target.getRegistryName().toString()
                    + " is not compatible with this notification factory do to required "
                    + "parameters.");
        }
        return new GenericNotification(targetSupplier.get(), context, typeSupplier.get(), builder);
    }

    /**
     * Creates and returns the {@code TextBuilder} for the notification message content.
     * @param context  the notification {@code TargetContext}
     * @return  the new {@code TextBuilder}
     */
    protected TextBuilder getContentBuilder(TargetContext context) {
        if (translationModeSupplier.get()) {
            return new TranslatableTextBuilder(translationKeySupplier.get());
        } else {
            return new TemplateTextBuilder(templateSupplier.get());
        }
    }

    /** Builder class for {@code ConfigurableNotificationFactory} objects. */
    public static class Builder {

        /** The {@code Supplier} for the notification's target. */
        protected Supplier<NotificationTarget> targetSupplier;
        /** The {@code Supplier} for the notification's {@link TranslatableTextBuilder} translation key. */
        protected Supplier<String> translationKeySupplier;
        /** The {@code Supplier} for the notification's {@link TemplateTextBuilder} template string. */
        protected Supplier<String> templateSupplier;
        /** The {@code Supplier} for the notification's message {@code ChatType}. */
        protected Supplier<ChatType> typeSupplier;
        /** The {@code Supplier} for the translation mode flag. */
        protected Supplier<Boolean> translationModeSupplier;
        /** The required {@code TargetParam} parameters of the notification. */
        protected HashSet<TargetParam<?>> requiredParams = new HashSet<>();

        /**
         * Sets the {@code Supplier} for the notification's target.
         * @param targetSupplier  the target {@code Supplier} to set
         * @return this {@code Builder} object
         */
        public Builder target(Supplier<NotificationTarget> targetSupplier) {
            this.targetSupplier = targetSupplier;
            return this;
        }

        /**
         * Sets the notification target {@code Supplier} to one that parses the {@code String}
         * provided by {@code keySupplier} into a registry key and returns the corresponding
         * registry entry from the {@link HourglassRegistry#NOTIFICATION_TARGET} registry.
         *
         * @param keySupplier  {@code Supplier} of the un-parsed registry key
         * @return this {@code Builder} object
         */
        public Builder stringTarget(Supplier<String> keySupplier) {
            this.targetSupplier = () -> {
                return Utils.parseRegistryKey(HourglassRegistry.NOTIFICATION_TARGET, keySupplier.get());
            };
            return this;
        }

        /**
         * Sets the text translation key {@code Supplier} for the {@link TranslationTextBuilder}.
         * @param translationKeySupplier  the text translation key {@code Supplier}
         * @return this {@code Builder} object
         */
        public Builder translationKey(Supplier<String> translationKeySupplier) {
            this.translationKeySupplier = translationKeySupplier;
            return this;
        }

        /**
         * Sets the text template {@code Supplier} for the {@link TemplateTextBuilder}.
         * @param translationKeySupplier  the text template {@code Supplier}
         * @return this {@code Builder} object
         */
        public Builder template(Supplier<String> templateSupplier) {
            this.templateSupplier = templateSupplier;
            return this;
        }

        /**
         * Sets the {@code Supplier} for the notification's message {@code ChatType}.
         * @param typeSupplier  the {@code ChatType} {@code Supplier}
         * @return this {@code Builder} object
         */
        public Builder type(Supplier<ChatType> typeSupplier) {
            this.typeSupplier = typeSupplier;
            return this;
        }

        /**
         * Sets the translation mode {@code Supplier} that determines which text builder to use.
         * @param translationModeSupplier  the translation mode {@code Supplier}
         * @return this {@code Builder} object
         */
        public Builder translationMode(Supplier<Boolean> translationModeSupplier) {
            this.translationModeSupplier = translationModeSupplier;
            return this;
        }

        /**
         * Adds {@code param} to the set of context parameters required by the factory.
         *
         * @param param  the {@code TargetParam} to add
         * @return this {@code Builder} object
         */
        public Builder requires(TargetParam<?> param) {
            requiredParams.add(param);
            return this;
        }

        /**
         * Returns a newly-created {@code ConfigurableNotificationFactory} object using the provided
         * parameters.
         * @return a newly-created {@code ConfigurableNotificationFactory} object
         */
        public ConfigurableNotificationFactory create() {
            return new ConfigurableNotificationFactory(targetSupplier,
                    translationKeySupplier,
                    templateSupplier,
                    typeSupplier,
                    translationModeSupplier,
                    requiredParams);
        }

    }

}
