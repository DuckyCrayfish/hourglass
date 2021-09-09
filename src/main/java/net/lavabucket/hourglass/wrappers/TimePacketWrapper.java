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

package net.lavabucket.hourglass.wrappers;

import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

/**
 * Time packet wrapper used to hide class or package changes from classes who use time-packets.
 */
public class TimePacketWrapper {

    /** The wrapped time-packet. */
    public final ClientboundSetTimePacket packet;

    /**
     * Creates a new instance.
     * @param packet  the time-packet to wrap
     */
    public TimePacketWrapper(ClientboundSetTimePacket packet) {
        this.packet = packet;
    }

    /**
     * Creates a wrapped time-packet for a level.
     * @param levelWrapper  the wrapped level for which to create a time-packet
     */
    public static TimePacketWrapper create(ServerLevelWrapper levelWrapper) {
        long gameTime = levelWrapper.level.getGameTime();
        long dayTime = levelWrapper.level.getDayTime();
        boolean ruleDaylight = levelWrapper.daylightRuleEnabled();
        ClientboundSetTimePacket packet = new ClientboundSetTimePacket(gameTime, dayTime, ruleDaylight);
        return new TimePacketWrapper(packet);
    }

}
