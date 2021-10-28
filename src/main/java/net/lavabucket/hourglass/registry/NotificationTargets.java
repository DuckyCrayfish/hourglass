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

package net.lavabucket.hourglass.registry;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.notifications.target.TargetContext;
import net.lavabucket.hourglass.notifications.target.TargetParam;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.registries.DeferredRegister;

/** This class contains a collection of default {@code NotificationTarget} registry objects. */
public final class NotificationTargets {

    private static final DeferredRegister<NotificationTarget> NOTIFICATION_TARGETS = DeferredRegister.create(NotificationTarget.class, Hourglass.MOD_ID);

    /** Uses {@link TargetParam#LEVEL} to supply all players in a level. */
    private static final Function<TargetContext, Stream<ServerPlayerWrapper>> GET_IN_LEVEL =
            context -> context.getParam(TargetParam.LEVEL).get().players().stream()
                .map(ServerPlayerWrapper::new);

    /** Fetches all players on the server. */
    private static final Function<TargetContext, Stream<ServerPlayerWrapper>> GET_ONLINE =
            context -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream()
                .map(ServerPlayerWrapper::new);

    /** Targets no players. */
    public static final RegistryObject<NotificationTarget> NONE = NOTIFICATION_TARGETS.register("none", () ->
            new NotificationTarget.Builder()
                .function(context -> Stream.empty())
                .create());

    /** Targets all players on the server. */
    public static final RegistryObject<NotificationTarget> ALL = NOTIFICATION_TARGETS.register("all", () ->
            new NotificationTarget.Builder()
                .function(GET_ONLINE)
                .create());

    /** Targets all operators on the server. */
    public static final RegistryObject<NotificationTarget> OPERATORS = NOTIFICATION_TARGETS.register("operators", () ->
            new NotificationTarget.Builder()
                .function(context -> {
                    PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
                    return GET_ONLINE.apply(context)
                            .filter(player -> playerList.isOp(player.get().getGameProfile()));
                })
                .create());

    /** Targets all players in the dimension. */
    public static final RegistryObject<NotificationTarget> DIMENSION = NOTIFICATION_TARGETS.register("dimension", () ->
            new NotificationTarget.Builder()
                .requires(TargetParam.LEVEL)
                .function(GET_IN_LEVEL)
                .create());

    /** Targets all sleeping players in the dimension. */
    public static final RegistryObject<NotificationTarget> ASLEEP = NOTIFICATION_TARGETS.register("asleep", () ->
            new NotificationTarget.Builder()
                .requires(TargetParam.LEVEL)
                .function(context -> GET_IN_LEVEL.apply(context)
                        .filter(ServerPlayerWrapper::isSleeping))
                .create());

    /** Targets all awake players in the dimension. */
    public static final RegistryObject<NotificationTarget> AWAKE = NOTIFICATION_TARGETS.register("awake", () ->
            new NotificationTarget.Builder()
                .requires(TargetParam.LEVEL)
                .function(context -> GET_IN_LEVEL.apply(context)
                        .filter(Predicate.not(ServerPlayerWrapper::isSleeping)))
                .create());

    /** Targets only the player that triggered this notification. */
    public static final RegistryObject<NotificationTarget> SELF = NOTIFICATION_TARGETS.register("self", () ->
            new NotificationTarget.Builder()
                .requires(TargetParam.PLAYER)
                .function(context -> Stream.of(context.getParam(TargetParam.PLAYER)))
                .create());

    /**
     * Registers all {@code NotificationTarget} objects created in this class to the registry.
     * @param event  the event, provided by the mod event bus
     */
    @SubscribeEvent
    public static void onConstructModEvent(FMLConstructModEvent event) {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        NOTIFICATION_TARGETS.register(modBus);
    }

}
