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

import java.util.function.Supplier;

import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.notifications.target.TargetContext;
import net.lavabucket.hourglass.notifications.target.TargetParam;
import net.lavabucket.hourglass.notifications.textbuilder.TextBuilder;
import net.lavabucket.hourglass.time.SleepStatus;
import net.minecraft.network.chat.ChatType;

/** Factory for {@code TimeService} event notifications. */
public class TimeServiceNotificationFactory extends ConfigurableNotificationFactory {

    /**
     * Creates a new instance.
     * @param targetSupplier  supplier for the notification target
     * @param translationKeySupplier  supplier for the message translation key
     * @param templateSupplier  supplier for the message template
     * @param typeSupplier  supplier for the chat message type
     * @param translationModeSupplier  supplier for the translation mode switch
     * @see ConfigurableNotificationFactory#ConfigurableNotificationFactory(Supplier, Supplier, Supplier, Supplier, Supplier)
     */
    public TimeServiceNotificationFactory(Supplier<NotificationTarget> targetSupplier,
            Supplier<String> translationKeySupplier,
            Supplier<String> templateSupplier,
            Supplier<ChatType> typeSupplier,
            Supplier<Boolean> translationModeSupplier) {

        super(targetSupplier, translationKeySupplier, templateSupplier, typeSupplier,
                translationModeSupplier);
    }

    /**
     * Returns a {@code TextBuilder} to use for the notification.
     *
     * <p>The builder includes the following variables:
     * <ul>
     *   <li>"sleepingPlayers" -> the number of sleeping players in the level</li>
     *   <li>"totalPlayers" -> the number of active players in the level</li>
     *   <li>"sleepingPercentage" -> an integer percentage of players who are sleeping</li>
     * </ul>
     *
     * @param context  the {@code TargetContext} for the notification
     * @return the {@code TextBuilder} to use for the notification
     */
    @Override
    protected TextBuilder getContentBuilder(TargetContext context) {
        TextBuilder builder = super.getContentBuilder(context);
        SleepStatus sleepStatus = context.getParam(TargetParam.TIME_SERVICE).sleepStatus;
        builder.setVariable("sleepingPlayers", sleepStatus.amountSleeping())
                .setVariable("totalPlayers", sleepStatus.amountActive())
                .setVariable("sleepingPercentage", sleepStatus.percentage());
        return builder;
    }

    /** Builder class for {@code TimeServiceNotificationFactory} objects. */
    public static class Builder extends ConfigurableNotificationFactory.Builder {

        /** {@return a new {@code TimeServiceNotificationFactory} using the supplied parameters} */
        public ConfigurableNotificationFactory create() {
            return new TimeServiceNotificationFactory(targetSupplier,
                    translationKeySupplier,
                    templateSupplier,
                    typeSupplier,
                    translationModeSupplier);
        }

    }

}
