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

package net.lavabucket.hourglass.message;

import java.util.stream.Stream;

import net.lavabucket.hourglass.message.target.NotificationTarget;
import net.lavabucket.hourglass.message.target.TargetContext;
import net.lavabucket.hourglass.message.textbuilder.TextBuilder;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;

public class Notification {

    protected final NotificationTarget target;
    protected final ChatType type;
    protected final TextBuilder messageBuilder;
    protected final TargetContext context;

    public Notification(NotificationTarget target, TargetContext context, ChatType type,
            TextBuilder messageBuilder) {
        this.target = target;
        this.context = context;
        this.type = type;
        this.messageBuilder = messageBuilder;
    }

    public void send() {
        getRecipients().forEach(this::sendToPlayer);
    }

    protected TextWrapper getMessage() {
        return messageBuilder.build();
    }

    protected Stream<ServerPlayerWrapper> getRecipients() {
        return target.findMatches(context);
    }

    protected void sendToPlayer(ServerPlayerWrapper player) {
        player.get().sendMessage(getMessage().get(), type, Util.NIL_UUID);
    }

}
