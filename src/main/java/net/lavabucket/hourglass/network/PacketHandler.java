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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.lavabucket.hourglass.HourglassMod;
import net.lavabucket.hourglass.config.ConfigSynchronizer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

/**
 * Network packet registration and management for Hourglass.
 */
public class PacketHandler {

    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(HourglassMod.ID, "channel");
    public static final String PROTOCOL_VERSION = "1.0";
    public static final byte TIME_MESSAGE_ID = 1;
    public static final byte CONFIG_MESSAGE_ID = 2;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    /**
     * Register all network messages needed for the mod. Called during the mod common setup event.
     *
     * @param event the mod event from the event bus
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

    /**
     * Packet handler wrapper that ensures the handler is only called on the client distribution.
     */
    public static class ClientOnlyMessageHandler<MSG> implements BiConsumer<MSG, Supplier<Context>> {
        private BiConsumer<MSG, Supplier<Context>> handler;
        public ClientOnlyMessageHandler(BiConsumer<MSG, Supplier<Context>> handler) {
            this.handler = handler;
        }
        @Override
        public void accept(MSG msg, Supplier<Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(msg, context)));
            context.get().setPacketHandled(true);
        }
    }

}
