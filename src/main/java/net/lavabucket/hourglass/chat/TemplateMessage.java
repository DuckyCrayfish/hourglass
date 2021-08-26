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

package net.lavabucket.hourglass.chat;

import java.util.HashMap;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TemplateMessage {

    private ChatType type = ChatType.SYSTEM;
    private Component message;
    private String template;
    public HashMap<String, String> variables;
    public StrSubstitutor substitutor;

    public TemplateMessage() {
        variables = new HashMap<>();
        substitutor = new StrSubstitutor();
        template = "";
    }

    /**
     * @return the ChatType of this message
     */
    public ChatType getType() {
        return type;
    }

    /**
     * @param type the ChatType of this message
     * @return this, for chaining
     */
    public TemplateMessage setType(ChatType type) {
        this.type = type;
        return this;
    }

    /**
     * @return the message template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the message template use during bake
     * @return this, for chaining
     */
    public TemplateMessage setTemplate(String template) {
        this.template = template;
        return this;
    }

    /**
     * Sets a variable to be substituted in the template message.
     *
     * @param key the variable name to search for in the template
     * @param value the value to substitute the variable with
     * @return this, for chaining
     */
    public TemplateMessage setVariable(String key, String value) {
        this.variables.put(key, value);
        return this;
    }

    /**
     * Gets the ITextComponent to be used as the message body.
     *
     * @return the message
     */
    public Component getMessage() {
        return this.message;
    }

    /**
     * Bake the variables into the message template to create an ITextComponent message.
     *
     * @return this, for chaining
     */
    public TemplateMessage bake() {
        this.substitutor.setVariableResolver(new MapLookup(this.variables));
        this.message = new TextComponent(this.substitutor.replace(this.template));
        return this;
    }

    /**
     * Sends the message to the specified targets. This method is only allowed if the target argument
     * is MessageTarget.ALL. Otherwise, use {@link #send(MessageTarget, ServerLevel)}.
     *
     * @param target the target of the message
     */
    public void send(MessageTarget target) {
        this.send(target, null);
    }

    /**
     * Sends the message to the specified targets. If {@code target} is MessageTarget.ALL,
     * {@code level} may be null.
     *
     * @param target the target of the message
     * @param level the level to send targeted message to, if applicable
     */
    public void send(MessageTarget target, @Nullable ServerLevel level) {
        if (target != MessageTarget.ALL && level == null) {
            throw new IllegalArgumentException("Level must be specified unless target is MessageTarget.ALL.");
        }

        if (target == MessageTarget.ALL) {
            level.getServer().getPlayerList().broadcastMessage(this.message, type, Util.NIL_UUID);
        } else {
            Stream<ServerPlayer> players = level.players().stream();
            if (target == MessageTarget.SLEEPING) {
                players = players.filter(ServerPlayer::isSleeping);
            }
            players.forEach(player -> player.sendMessage(this.message, type, Util.NIL_UUID));
        }
    }

    /**
     * Target destination of a template message.
     */
    public enum MessageTarget {
        // Targets all players on the server.
        ALL,
        // Targets all players in the associated dimension.
        DIMENSION,
        // Targets all sleeping players.
        SLEEPING
    }

}
