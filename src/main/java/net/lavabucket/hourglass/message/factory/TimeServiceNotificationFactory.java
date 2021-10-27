package net.lavabucket.hourglass.message.factory;

import java.util.function.Supplier;

import net.lavabucket.hourglass.message.target.NotificationTarget;
import net.lavabucket.hourglass.message.target.TargetContext;
import net.lavabucket.hourglass.message.target.TargetParam;
import net.lavabucket.hourglass.message.textbuilder.TextBuilder;
import net.lavabucket.hourglass.time.SleepStatus;
import net.minecraft.network.chat.ChatType;

public class TimeServiceNotificationFactory extends ConfigurableNotificationFactory {

    public TimeServiceNotificationFactory(Supplier<NotificationTarget> targetSupplier,
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
        SleepStatus sleepStatus = context.getParam(TargetParam.TIME_SERVICE).sleepStatus;
        builder.setVariable("sleepingPlayers", sleepStatus.amountSleeping())
                .setVariable("totalPlayers", sleepStatus.amountActive())
                .setVariable("sleepingPercentage", sleepStatus.percentage());
        return builder;
    }

    public static class Builder extends ConfigurableNotificationFactory.Builder {

        public ConfigurableNotificationFactory create() {
            return new TimeServiceNotificationFactory(targetSupplier,
                    translationKeySupplier,
                    templateSupplier,
                    typeSupplier,
                    translationModeSupplier);
        }

    }

}
