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

package net.lavabucket.hourglass.notifications;

import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.notifications.target.TargetContext;
import net.lavabucket.hourglass.notifications.textbuilder.TextBuilder;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;

/**
 * A generic notification that is sent as a Minecraft chat message whose content is provided by a
 * {@link TextBuilder} instance.
 */
public class GenericNotification implements Notification {

    /** This notification's target. */
    protected final NotificationTarget target;
    /** This notification's message {@code ChatType}. */
    protected final ChatType type;
    /** The {@code TextBuilder} for this notification's message content. */
    protected final TextBuilder messageBuilder;
    /** The {@code TargetContext} for {@link #target}. */
    protected final TargetContext context;

    /**
     * Creates a new instance.
     * @param target  this notification's target
     * @param context  the {@code TargetContext} for {@code target}
     * @param type  this notification's message {@code ChatType}
     * @param messageBuilder  the {@code TextBuilder} for this notification's message content
     */
    public GenericNotification(NotificationTarget target, TargetContext context, ChatType type,
            TextBuilder messageBuilder) {
        this.target = target;
        this.context = context;
        this.type = type;
        this.messageBuilder = messageBuilder;
    }

    /** {@return this notification's target} */
    @Override
    public NotificationTarget getTarget() {
        return target;
    }

    /**
     * Sends this notification to all players matching this notification's target, using the target
     * context provided. The content of this notification is obtained by building this
     * notification's {@code TextBuilder} instance.
     */
    public void send() {
        getTarget().findMatches(context).forEach(this::sendToPlayer);
    }

    /**
     * Builds and returns this notification's message content.
     * @return this notification's message content
     */
    protected TextWrapper getMessage() {
        return messageBuilder.build();
    }

    /**
     * Sends this notification to {@code player} using the content provided by
     * {@link #getMessage()}.
     */
    protected void sendToPlayer(ServerPlayerWrapper player) {
        player.get().sendMessage(getMessage().get(), type, Util.NIL_UUID);
    }

}
