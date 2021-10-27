/*
 *   Copyright (c) 2021
 *   All rights reserved.
 */
package net.lavabucket.hourglass.message.factory;

import java.util.function.Supplier;

import net.lavabucket.hourglass.message.target.NotificationTarget;
import net.lavabucket.hourglass.message.target.TargetContext;
import net.lavabucket.hourglass.message.target.TargetParam;
import net.lavabucket.hourglass.message.textbuilder.TextBuilder;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.minecraft.network.chat.ChatType;

public class SleepNotificationFactory extends TimeServiceNotificationFactory {

    public SleepNotificationFactory(Supplier<NotificationTarget> targetSupplier,
            Supplier<String> translationKeySupplier,
            Supplier<String> templateSupplier,
            Supplier<ChatType> typeSupplier,
            Supplier<Boolean> translationModeSupplier) {

        super(targetSupplier, translationKeySupplier, templateSupplier, typeSupplier,
                translationModeSupplier);
    }

    @Override
    protected TextBuilder getMessageBuilder(TargetContext context) {
        TextBuilder builder = super.getMessageBuilder(context);
        ServerPlayerWrapper player = context.getParam(TargetParam.PLAYER);
        builder.setVariable("player", player.get().getDisplayName());
        return builder;
    }

    public static class Builder extends ConfigurableNotificationFactory.Builder {

        public ConfigurableNotificationFactory create() {
            return new SleepNotificationFactory(targetSupplier,
                    translationKeySupplier,
                    templateSupplier,
                    typeSupplier,
                    translationModeSupplier);
        }

    }

}
