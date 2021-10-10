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

package net.lavabucket.hourglass.network;

import java.util.Optional;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.config.ConfigSynchronizer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

/**
 * Network packet registration and management for Hourglass.
 */
public final class NetworkHandler {

    /** Network channel name. */
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Hourglass.MOD_ID, "channel");
    /** Current protocol version used to determine compatibility with other versions of this mod. */
    public static final String PROTOCOL_VERSION = "1.0";
    /** The network ID for the {@code ConfigSynchronizer} message. */
    public static final byte CONFIG_MESSAGE_ID = 1;

    /** The Hourglass network channel. */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    /**
     * Register all network messages needed for the mod. Called during the mod common setup event.
     * @param event  the mod event from the event bus
     */
    @SubscribeEvent
    public static void registerNetworkMessages(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(CONFIG_MESSAGE_ID,
                ConfigSynchronizer.getMessageClass(),
                ConfigSynchronizer::encode,
                ConfigSynchronizer::decode,
                ConfigSynchronizer::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

}
