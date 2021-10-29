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
    /** The {@code TextBuilder} for this notification's content. */
    protected final TextBuilder textBuilder;
    /** The {@code TargetContext} for {@link #target}. */
    protected final TargetContext context;

    /**
     * Creates a new instance.
     * @param target  this notification's target
     * @param context  the {@code TargetContext} for {@code target}
     * @param type  this notification's message {@code ChatType}
     * @param textBuilder  the {@code TextBuilder} for this notification's content
     */
    public GenericNotification(NotificationTarget target, TargetContext context, ChatType type,
            TextBuilder textBuilder) {
        this.target = target;
        this.context = context;
        this.type = type;
        this.textBuilder = textBuilder;
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
    @Override
    public void send() {
        getTarget().findMatches(context).forEach(player -> {
            textBuilder.setVariable("self", player.get().getDisplayName());
            TextWrapper content = textBuilder.build();
            player.get().sendMessage(content.get(), type, Util.NIL_UUID);
        });
    }

}
