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

import java.util.HashMap;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;

/**
 * Message builder for Hourglass notifications, which allow for customizable targets and variable
 * substitution.
 */
public class TemplateMessage {

    /** A variable map used for variable string substitution. */
    public HashMap<String, String> variables;
    /** The {@code StrSubstitutor} object used to perform variable string substitution. */
    public StrSubstitutor substitutor;

    private boolean overlay;
    private TextWrapper message;
    private String template;

    /** Instantiates a new message builder. */
    public TemplateMessage() {
        variables = new HashMap<>();
        substitutor = new StrSubstitutor();
        template = "";
    }

    /** {@return true if this message is an overlay message, false otherwise} */
    public boolean isOverlay() {
        return overlay;
    }

    /**
     * Sets whether or not the message is displayed as an overlay message.
     *
     * @param overlay  true if this message should be displayed as an overlay, false otherwise
     * @return this, for chaining
     */
    public TemplateMessage setOverlay(boolean overlay) {
        this.overlay = overlay;
        return this;
    }

    /** {@return the message template} */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template of this message to use when baking the message.
     *
     * @param template  the message template
     * @return this, for chaining
     */
    public TemplateMessage setTemplate(String template) {
        this.template = template;
        return this;
    }

    /**
     * Sets a variable to be substituted in the template message.
     *
     * @param key  the variable name to search for in the template
     * @param value  the value to substitute the variable with
     * @return this, for chaining
     */
    public TemplateMessage setVariable(String key, String value) {
        this.variables.put(key, value);
        return this;
    }

    /** {@return the text component to be used as the message body} */
    public TextWrapper getMessage() {
        return this.message;
    }

    /**
     * Bake the variables a new message.
     * @return this, for chaining
     */
    public TemplateMessage bake() {
        this.substitutor.setVariableResolver(new MapLookup(this.variables));
        this.message = TextWrapper.literal(this.substitutor.replace(this.template));
        return this;
    }

    /**
     * Sends the message to the specified targets. This method is only allowed if the target argument
     * is MessageTarget.ALL. Otherwise, use {@link #send(MessageTarget, ServerLevelWrapper)}.
     *
     * @param target  the target of the message
     */
    public void send(MessageTarget target) {
        this.send(target, null);
    }

    /**
     * Sends the message to the specified targets. If {@code target} is MessageTarget.ALL,
     * {@code level} may be null.
     *
     * @param target  the target of the message
     * @param level  the level to send targeted message to, if applicable
     */
    public void send(MessageTarget target, @Nullable ServerLevelWrapper level) {
        if (target != MessageTarget.ALL && level == null) {
            throw new IllegalArgumentException("Level must be specified unless target is MessageTarget.ALL.");
        }

        if (target == MessageTarget.ALL) {
            level.get().getServer().getPlayerList().broadcastSystemMessage(this.message.get(), overlay);
        } else {
            Stream<ServerPlayerWrapper> playerStream = level.get().players().stream()
                    .map(player -> new ServerPlayerWrapper(player));

            if (target == MessageTarget.SLEEPING) {
                playerStream = playerStream.filter(ServerPlayerWrapper::isSleeping);
            }

            playerStream.forEach(player -> player.get().sendSystemMessage(this.message.get(), overlay));
        }
    }

    /** Target destination of a template message. */
    public enum MessageTarget {
        /** Targets all players on the server. */
        ALL,
        /** Targets all players in the associated dimension. */
        DIMENSION,
        /** Targets all sleeping players. */
        SLEEPING
    }

}
