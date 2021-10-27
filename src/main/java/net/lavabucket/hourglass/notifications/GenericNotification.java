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

public class GenericNotification implements Notification {

    protected final NotificationTarget target;
    protected final ChatType type;
    protected final TextBuilder messageBuilder;
    protected final TargetContext context;

    public GenericNotification(NotificationTarget target, TargetContext context, ChatType type,
            TextBuilder messageBuilder) {
        this.target = target;
        this.context = context;
        this.type = type;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public NotificationTarget getTarget() {
        return target;
    }

    public void send() {
        getTarget().findMatches(context).forEach(this::sendToPlayer);
    }

    protected TextWrapper getMessage() {
        return messageBuilder.build();
    }

    protected void sendToPlayer(ServerPlayerWrapper player) {
        player.get().sendMessage(getMessage().get(), type, Util.NIL_UUID);
    }

}
