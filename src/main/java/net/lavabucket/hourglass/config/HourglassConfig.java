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

package net.lavabucket.hourglass.config;

import java.util.stream.Collectors;

import net.lavabucket.hourglass.client.gui.ScreenAlignment;
import net.lavabucket.hourglass.notifications.NotificationService;
import net.lavabucket.hourglass.notifications.factory.ConfigurableNotificationFactory;
import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.Time;
import net.lavabucket.hourglass.time.effects.EffectCondition;
import net.lavabucket.hourglass.utils.Utils;
import net.minecraft.network.chat.ChatType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Configuration class for Hourglass.
 */
public final class HourglassConfig {

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    /**
     * Register this class's configs with the mod loading context.
     * @param event  the event, provided by the mod event bus
     */
    @SubscribeEvent
    public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
        final ModLoadingContext context = ModLoadingContext.get();

        SERVER_CONFIG = new ServerConfig(new Builder());
        CLIENT_CONFIG = new ClientConfig(new Builder());

        context.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.spec);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG.spec);
    }

    /** Server-specific configuration file. */
    public static class ServerConfig {

        public final ForgeConfigSpec spec;

        public final DoubleValue daySpeed;
        public final DoubleValue nightSpeed;

        public final EnumValue<EffectCondition> weatherEffect;
        public final EnumValue<EffectCondition> randomTickEffect;
        public final IntValue baseRandomTickSpeed;
        public final EnumValue<EffectCondition> potionEffect;
        public final EnumValue<EffectCondition> hungerEffect;
        public final EnumValue<EffectCondition> blockEntityEffect;

        public final BooleanValue enableSleepFeature;
        public final DoubleValue sleepSpeedMin;
        public final DoubleValue sleepSpeedMax;
        public final DoubleValue sleepSpeedAll;
        public final DoubleValue sleepSpeedCurve;
        public final BooleanValue clearWeatherOnWake;
        public final BooleanValue allowBedClock;
        public final BooleanValue allowDaySleep;

        public final BooleanValue internationalMode;
        public final ConfigValue<String> morningNotificationContent;
        public final EnumValue<ChatType> morningNotificationType;
        public final ConfigValue<String> morningNotificationTarget;

        public final ConfigValue<String> enterBedNotificationContent;
        public final EnumValue<ChatType> enterBedNotificationType;
        public final ConfigValue<String> enterBedNotificationTarget;

        public final ConfigValue<String> leaveBedNotificationContent;
        public final EnumValue<ChatType> leaveBedNotificationType;
        public final ConfigValue<String> leaveBedNotificationTarget;

        /**
         * Constructs an instance of an Hourglass server config.
         * @param builder  a Forge config builder instance
         */
        public ServerConfig(final ForgeConfigSpec.Builder builder) {

            builder.push("time"); // time

                daySpeed = builder.comment(
                    "The speed at which time passes during the day.",
                    "Day is defined as any time between 23500 (middle of dawn) and 12500 (middle of dusk) the next day.",
                    "Vanilla speed: 1.0")
                    .defineInRange("daySpeed", 1D, 0D, Time.DAY_LENGTH.doubleValue());

                nightSpeed = builder.comment(
                    "The speed at which time passes during the night.",
                    "Night is defined as any time between 12500 (middle of dusk) and 23500 (middle of dawn).",
                    "Vanilla speed: 1.0")
                    .defineInRange("nightSpeed", 1D, 0D, Time.DAY_LENGTH.doubleValue());

                builder.push("effects"); // time.effects

                    weatherEffect = builder.comment(
                        "When applied, this effect syncs the passage of weather with the current speed of time.",
                        "I.e., as time moves faster, rain stops faster. Clear weather is not affected.",
                        "When set to SLEEPING, this effect only applies when at least one player is sleeping in a dimension.",
                        "Note: This setting is not applicable if game rule doWeatherCycle is false.")
                        .defineEnum("weatherEffect", EffectCondition.SLEEPING);

                    randomTickEffect = builder.comment(
                        "When applied, this effect syncs the random tick speed with the current speed of time, forcing",
                        "crop, tree, and grass growth to occur at baseRandomTickSpeed multiplied by the current time-speed.",
                        "When set to SLEEPING, randomTickSpeed is set to baseRandomTickSpeed unless at least one player is sleeping in a dimension.",
                        "More information on the effects of random tick speed can be found here: https://minecraft.fandom.com/wiki/Tick#Random_tick",
                        "WARNING: This setting overwrites the randomTickSpeed game rule. To modify the base random tick speed,",
                        "use the baseRandomTickSpeed setting instead of changing the game rule directly.")
                        .defineEnum("randomTickEffect", EffectCondition.NEVER);

                    baseRandomTickSpeed = builder
                        .comment("The base random tick speed used by the randomTickEffect time effect.")
                        .defineInRange("baseRandomTickSpeed", 3, 0, Integer.MAX_VALUE);

                    potionEffect = builder.comment(
                        "When applied, this effect progresses potion effects to match the rate of the current time-speed.",
                        "This effect does not apply if time speed is 1.0 or less.",
                        "THIS MAY HAVE A NEGATIVE IMPACT ON PERFORMANCE IN SERVERS WITH MANY PLAYERS.",
                        "When set to ALWAYS, this effect applies to all players in the dimension, day or night.",
                        "When set to SLEEPING, this effect only applies to players who are sleeping.")
                        .defineEnum("potionEffect", EffectCondition.NEVER);

                    hungerEffect = builder.comment(
                        "When applied, this effect progresses player hunger effects to match the rate of the current time-speed.",
                        "This results in faster healing when food level is full, and faster harm when food level is too low.",
                        "This effect does not apply if time speed is 1.0 or less.",
                        "When set to ALWAYS, this effect applies to all players in the dimension, day or night. Not recommended on higher difficulty settings",
                        "When set to SLEEPING, this effect only applies to players who are sleeping.")
                        .defineEnum("hungerEffect", EffectCondition.NEVER);

                    blockEntityEffect = builder.comment(
                        "When applied, this effect progresses block entities like furnaces, hoppers, and spawners to match the rate of the current time-speed.",
                        "WARNING: This time-effect has a significant impact on performance.",
                        "This effect does not apply if time speed is 1.0 or less.",
                        "When set to SLEEPING, this effect only applies when at least one player is sleeping in a dimension.")
                        .defineEnum("blockEntityEffect", EffectCondition.NEVER);

                builder.pop(); // time.effects
            builder.pop(); // time

            builder.push("sleep"); // sleep

                enableSleepFeature = builder.comment(
                    "Enables or disables the sleep feature of this mod. Enabling this setting will modify the vanilla sleep functionality",
                    "and may conflict with other sleep mods. If disabled, all settings in the sleep section will not apply.")
                    .define("enableSleepFeature", true);

                sleepSpeedMax = builder.comment(
                    "## THIS SETTING DEFINES THE SLEEP TIME-SPEED IN SINGLE-PLAYER GAMES ###",
                    "The maximum speed at which time passes when all players are sleeping.",
                    "A value of 110 is nearly equal to the time it takes to sleep in vanilla.")
                    .defineInRange("sleepSpeedMax", 110D, 0D, Time.DAY_LENGTH.doubleValue());

                sleepSpeedMin = builder
                    .comment("The minimum speed at which time passes when only 1 player is sleeping in a full server.")
                    .defineInRange("sleepSpeedMin", 1D, 0D, Time.DAY_LENGTH.doubleValue());

                sleepSpeedAll = builder.comment(
                    "The speed at which time passes when all players are sleeping.",
                    "Set to -1 to disable this feature (sleepSpeedMax will be used when all players are sleeping).")
                    .defineInRange("sleepSpeedAll", -1.0D, -1.0D, Time.DAY_LENGTH.doubleValue());

                sleepSpeedCurve = builder.comment(
                    "This parameter defines the curvature of the interpolation function that translates the sleeping player percentage into time-speed.",
                    "The function used is a Normalized Tunable Sigmoid Function.",
                    "A value of 0.5 represents a linear relationship.",
                    "Smaller values bend the curve toward the X axis, while greater values bend it toward the Y axis.",
                    "This graph may be used as a reference for tuning the curve: https://www.desmos.com/calculator/w8gntxzfow",
                    "Credit to Dino Dini for the function: https://dinodini.wordpress.com/2010/04/05/normalized-tunable-sigmoid-functions/",
                    "Credit to SmoothSleep for the idea: https://www.spigotmc.org/resources/smoothsleep.32043/")
                    .defineInRange("sleepSpeedCurve", 0.3D, 0D, 1D);

                clearWeatherOnWake = builder.comment(
                    "Set to 'true' for the weather to clear when players wake up in the morning as it does in vanilla.",
                    "Set to 'false' to force weather to pass naturally. Adds realism when accelerateWeather is enabled.",
                    "Note: This setting is ignored if game rule doWeatherCycle is false.")
                    .define("clearWeatherOnWake", true);

                allowDaySleep = builder.comment(
                    "When true, players are allowed to sleep at all times of day in dimensions controlled by Hourglass.",
                    "Note: Other mods may override this ability.")
                    .define("allowDaySleep", false);

                allowBedClock = builder.comment(
                    "When true, a clock will appear in the bed interface.",
                    "This clock may be disabled by players via hideBedClock config setting.")
                    .define("allowBedClock", true);

            builder.pop(); // sleep

            // notifications
            builder.comment(
                "This section defines settings for notification messages.",
                "Minecraft formatting codes are supported (https://minecraft.fandom.com/wiki/Formatting_codes).",
                "All notifications have variables that can be inserted using the following format: ${variableName}",
                "Not all notifications support the same variables. The list of variables includes:",
                "\tplayer -> the name of the subject player of the notification.",
                "\tsleepingPlayers -> the number of players in the dimension who are sleeping (spectators are not counted).",
                "\ttotalPlayers -> the total number of players in the dimension (spectators are not counted).",
                "\tsleepingPercentage -> a 0-100 ratio of sleeping players to total players (100 * sleepingPlayers / totalPlayers).",
                "\tself -> each player who receives the notification will have their own name inserted wherever this variable is used.",
                "The type option controls where the notification appears:",
                "\tSYSTEM: Appears as a message in the chat. (e.g., \"Respawn point set\")",
                "\tGAME_INFO: Game information that appears above the hotbar (e.g., \"You may not rest now, the bed is too far away\").",
                "The target option controls to whom the notification is sent:",
                "\tnone: Disables the notification.",
                "\tall: Send to all players on the server.",
                "\toperators: Send to all operators on the server.",
                "\tdimension: Send to all players in the current dimension.",
                "\tasleep: Send to all asleep players in the current dimension.",
                "\tawake: Send to all awake players in the current dimension.",
                "\tplayer: Send only to the player who is the subject of the notification.")
                .push("notifications");

                internationalMode = builder.comment(
                    "When true, sleep notifications are sent using language files instead of the text content defined in this config file.",
                    "This allows for the ability to support multiple languages at a time.",
                    "When true, resource packs are required for notification text customization.",
                    "Enabling this setting is recommended for modpacks.")
                    .define("internationalMode", false);

                // notifications.morning
                builder.comment(
                    "This notification is sent as an Hourglass-assisted sleep cycle completes.",
                    "The target and variables of this notification are calculated prior to players waking up.",
                    "For example, a target of 'asleep' will target all players who were sleeping just before morning.",
                    "Not sent if sleep feature is disabled.")
                    .push("morning");
                    morningNotificationContent = builder.comment(
                        "Allowed Variables: sleepingPlayers, totalPlayers, sleepingPercentage, self")
                        .define("message", "\u00A7e\u00A7oTempus fugit!");
                    morningNotificationType = builder.comment("Sets where this notification appears.")
                        .defineEnum("type", ChatType.GAME_INFO, ChatType.SYSTEM, ChatType.GAME_INFO);
                    morningNotificationTarget = builder.comment(
                        "Sets to whom this notification is sent.",
                        "Allowed Values: " + generateAllowedTargets(NotificationService.MORNING_MESSAGE))
                        .define("target", () -> "dimension", value -> isValidTargetKey(NotificationService.MORNING_MESSAGE, value));
                builder.pop(); // notifications.morning

                // notifications.enterBed
                builder.comment(
                    "This notification is sent when a player enters their bed in a dimension controlled by Hourglass.",
                    "Not sent if sleep feature is disabled.")
                    .push("enterBed");
                    enterBedNotificationContent = builder.comment(
                        "Allowed Variables: player, sleepingPlayers, totalPlayers, sleepingPercentage, self")
                        .define("message", "${player} is now sleeping. [${sleepingPlayers}/${totalPlayers}]");
                    enterBedNotificationType = builder.comment("Sets where this notification appears.")
                        .defineEnum("type", ChatType.GAME_INFO, ChatType.SYSTEM, ChatType.GAME_INFO);
                    enterBedNotificationTarget = builder.comment(
                        "Sets to whom this notification is sent.",
                        "Allowed Values: " + generateAllowedTargets(NotificationService.ENTER_BED_MESSAGE))
                        .define("target", () -> "dimension", value -> isValidTargetKey(NotificationService.ENTER_BED_MESSAGE, value));
                builder.pop(); // notifications.enterBed

                // notifications.leaveBed
                builder.comment(
                    "This notification is sent when a player leaves their bed in a dimension controlled by Hourglass.",
                    "Not sent when woken up by morning.",
                    "Not sent if sleep feature is disabled.")
                    .push("leaveBed");
                    leaveBedNotificationContent = builder.comment(
                        "Allowed Variables: player, sleepingPlayers, totalPlayers, sleepingPercentage, self")
                        .define("message", "${player} has left their bed. [${sleepingPlayers}/${totalPlayers}]");
                    leaveBedNotificationType = builder.comment("Sets where this notification appears.")
                        .defineEnum("type", ChatType.GAME_INFO, ChatType.SYSTEM, ChatType.GAME_INFO);
                    leaveBedNotificationTarget = builder.comment(
                        "Sets to whom this notification is sent.",
                        "Allowed Values: " + generateAllowedTargets(NotificationService.LEAVE_BED_MESSAGE))
                        .define("target", () -> "dimension", value -> isValidTargetKey(NotificationService.LEAVE_BED_MESSAGE, value));
                builder.pop(); // notifications.leaveBed

            builder.pop(); // notifications

            spec = builder.build();
        }

        private static String generateAllowedTargets(ConfigurableNotificationFactory notification) {
            IForgeRegistry<NotificationTarget> registry = HourglassRegistry.NOTIFICATION_TARGET;
            String namespace = registry.getRegistryName().getNamespace();
            return registry.getValues().stream()
                    .filter(notification::targetCompatible)
                    .map(NotificationTarget::getRegistryName)
                    .map(rl -> Utils.resourceLocationToShortString(rl, namespace))
                    .collect(Collectors.joining(", "));
        }

        private static boolean isValidTargetKey(ConfigurableNotificationFactory notification, Object value) {
            if (!(value instanceof String)) {
                return false;
            }

            IForgeRegistry<NotificationTarget> registry = HourglassRegistry.NOTIFICATION_TARGET;
            String key = (String) value;
            if (!Utils.isValidRegistryKey(registry, key)) {
                return false;
            }

            NotificationTarget target = Utils.parseRegistryKey(registry, key);
            if (!notification.targetCompatible(target)) {
                return false;
            }

            return true;
        }

    }

    /** Client-specific configuration file. */
    public static class ClientConfig {

        public final ForgeConfigSpec spec;

        public final BooleanValue hideBedClock;
        public final EnumValue<ScreenAlignment> clockAlignment;
        public final IntValue clockScale;
        public final IntValue clockMargin;
        public final BooleanValue preventClockWobble;

        /**
         * Constructs an instance of an Hourglass client config.
         * @param builder  a Forge config builder instance
         */
        public ClientConfig(final ForgeConfigSpec.Builder builder) {

            builder.push("gui"); // gui

                hideBedClock = builder.comment(
                    "When false, a clock is displayed in the bed interface, if server settings allow it.",
                    "When true, the clock is hidden.")
                    .define("hideBedClock", false);

                clockAlignment = builder.comment("Sets the screen alignment of the bed clock.")
                    .defineEnum("clockAlignment", ScreenAlignment.TOP_RIGHT);

                clockScale = builder.comment("Sets the scale of the bed clock.")
                    .defineInRange("clockScale", 64, 1, Integer.MAX_VALUE);

                clockMargin = builder.comment(
                    "Sets the distance between the clock and the edge of the screen.",
                    "Unused if clockAlignment is CENTER_CENTER.")
                    .defineInRange("clockMargin", 16, 0, Integer.MAX_VALUE);

                preventClockWobble = builder.comment(
                    "This setting prevents clock wobble when getting in bed by updating the clock's position every tick.",
                    "As a side-effect, the clock won't wobble when first viewed as it does in vanilla. This setting is",
                    "unused if displayBedClock is false.")
                    .define("preventClockWobble", true);

            builder.pop(); // gui

            spec = builder.build();
        }

    }

}
