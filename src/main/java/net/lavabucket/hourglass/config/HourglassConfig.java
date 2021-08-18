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

import net.lavabucket.hourglass.chat.TemplateMessage.MessageTarget;
import net.lavabucket.hourglass.client.gui.ScreenAlignment;
import net.lavabucket.hourglass.time.TimeUtils;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;

public class HourglassConfig {

    public static final Builder SERVER_BUILDER = new Builder();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig(SERVER_BUILDER);
    public static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static final Builder CLIENT_BUILDER = new Builder();
    public static final ClientConfig CLIENT_CONFIG = new ClientConfig(CLIENT_BUILDER);
    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

	/**
	 * Register this class's configs with the mod context provided. Should be called during mod
	 * initialization.
	 *
	 * @param context the mod loading context to register the configs with.
	 */
	public static void register(ModLoadingContext context) {
		context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
		context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
	}

    public static class ServerConfig {

        public final DoubleValue daySpeed;
        public final DoubleValue nightSpeed;
        public final BooleanValue displayBedClock;
        public final BooleanValue accelerateWeather;

        public final BooleanValue enableSleepFeature;
        public final DoubleValue sleepSpeedMin;
        public final DoubleValue sleepSpeedMax;
        public final DoubleValue sleepSpeedAll;
        public final BooleanValue clearWeatherOnWake;
        public final BooleanValue accelerateRandomTickSpeed;
        public final IntValue baseRandomTickSpeed;

        public final ConfigValue<String> morningMessage;
        public final EnumValue<ChatType> morningMessageType;
        public final EnumValue<MessageTarget> morningMessageTarget;
        public final ConfigValue<String> inBedMessage;
        public final ConfigValue<String> outOfBedMessage;
        public final EnumValue<ChatType> bedMessageType;
        public final EnumValue<MessageTarget> bedMessageTarget;

        /**
         * Constructs an instance of an Hourglass server config.
         *
         * @param builder  a Forge config builder instance
         */
        public ServerConfig(final ForgeConfigSpec.Builder builder) {
            // general
            builder.push("general");

            daySpeed = builder
                    .comment("The speed at which time passes during the day.\n"
                            + "Day is defined as any time between 23500 (middle of dawn) and 12500 (middle of dusk) the next day.\n"
                            + "Vanilla speed: 1.0")
                    .defineInRange("daySpeed", 1D, 0D, (double) TimeUtils.DAY_LENGTH);

            nightSpeed = builder
                    .comment("The speed at which time passes during the night.\n"
                            + "Night is defined as any time between 12500 (middle of dusk) and 23500 (middle of dawn).\n"
                            + "Vanilla speed: 1.0")
                    .defineInRange("nightSpeed", 1D, 0D, (double) TimeUtils.DAY_LENGTH);

            displayBedClock = builder
                    .comment("When true, displays a clock in the sleep interface.")
                    .define("displayBedClock", true);

            accelerateWeather = builder
                    .comment("Accelerate the passage of weather at the same rate as the passage of time, making weather events\n"
                            + "elapse faster while the passage of time is accelerated. Clear weather is not accelerated.\n"
                            + "Note: This setting is not applicable if game rule doWeatherCycle is false.")
                    .define("accelerateWeather", true);

            builder.pop();

            // sleep
            builder.push("sleep");

            enableSleepFeature = builder
                    .comment("Enables or disables the sleep feature of this mod. Enabling this setting will modify the vanilla\n"
                            + "sleep functionality and may conflict with other sleep mods. If disabled, the remaining settings\n"
                            + "in this section will not apply.")
                    .define("enableSleepFeature", true);

            sleepSpeedMin = builder
                    .comment("The minimum speed at which time passes when only 1 player is sleeping in a full server.")
                    .defineInRange("sleepSpeedMin", 1D, 0D, (double) TimeUtils.DAY_LENGTH);

            sleepSpeedMax = builder
                    .comment("The maximum speed at which time passes when all players are sleeping. A value of 120\n"
                            + "is approximately equal to the time it takes to sleep in vanilla.")
                    .defineInRange("sleepSpeedMax", 120D, 0D, (double) TimeUtils.DAY_LENGTH);

            sleepSpeedAll = builder
                    .comment("The speed at which time passes when all players are sleeping.\n"
                            + "Set to -1 to disable this feature (sleepSpeedMax will be used when all players are sleeping).")
                    .defineInRange("sleepSpeedAll", -1.0D, -1.0D, (double) TimeUtils.DAY_LENGTH);

            clearWeatherOnWake = builder
                    .comment("Set to 'true' for the weather to clear when players wake up in the morning as it does in vanilla.\n"
                            + "Set to 'false' to allow weather to pass realistically overnight if accelerateWeather is enabled.\n"
                            + "Note: This setting is ignored if game rule doWeatherCycle is false.")
                    .define("clearWeatherOnWake", true);

            accelerateRandomTickSpeed = builder
                    .comment("When true, accelerates the random tick speed while sleeping. This allows things like crops and\n"
                            + "grass to grow at the same rate as time is passing overnight. The modified random tick speed is the\n"
                            + "sleep.baseRandomTickSpeed value times the current time speed. This means that as time moves faster, crops grow faster.\n"
                            + "More information on the effects of random tick speed can be found here:\n"
                            + "https://minecraft.fandom.com/wiki/Tick#Random_tick\n"
                            + "WARNING: This setting manipulates the randomTickSpeed game rule. To modify the base random tick speed,\n"
                            + "use the sleep.baseRandomTickSpeed config setting instead of changing the game rule directly.")
                    .define("accelerateRandomTicking", false);

            baseRandomTickSpeed = builder
                    .comment("The base random tick speed to use when sleep.accelerateRandomTickSpeed config is enabled.")
                    .defineInRange("baseRandomTickSpeed", 3, 0, Integer.MAX_VALUE);

            builder.pop();

            // messages
            builder.comment("This section defines settings for notification messages.\n"
                            + "All messages in this section support Minecraft formatting codes (https://minecraft.fandom.com/wiki/Formatting_codes).\n"
                            + "All messages in this section support variable substitution in the following format: ${variableName}\n"
                            + "Supported variables differ per message.")
                    .push("messages");

            morningMessage = builder
                    .comment("This message is sent to morningMessageTarget after a sleep cycle has completed in it.\n"
                            + "Available variables:\n"
                            + "sleepingPlayers -> the number of players in the current dimension who were sleeping.\n"
                            + "totalPlayers -> the number of players in the current dimension (spectators are not counted).\n"
                            + "sleepingPercentage -> the percentage of players in the current dimension who were sleeping (does not include % symbol).")
                    .define("morningMessage", "\u00A7e\u00A7oTempus fugit!");

            morningMessageType = builder
                    .comment("Sets the message type for the morning message.\n"
                            + "SYSTEM: Appears as a message in the chat. (e.g., \"Respawn point set\")\n"
                            + "GAME_INFO: Game information that appears above the hotbar (e.g., \"You may not rest now, the bed is too far away\").")
                    .defineEnum("morningMessageType", ChatType.GAME_INFO, ChatType.SYSTEM, ChatType.GAME_INFO);

            morningMessageTarget = builder
                    .comment("Sets the target for the morning message.\n"
                            + "ALL: Send the message to all players on the server.\n"
                            + "DIMENSION: Send the message to all players in the current dimension.\n"
                            + "SLEEPING: Only send the message to those who just woke up.")
                    .defineEnum("morningMessageTarget", MessageTarget.DIMENSION);

            inBedMessage = builder
                    .comment("This message is sent to bedMessageTarget when a player starts sleeping.\n"
                            + "Available variables:\n"
                            + "player -> the player who started sleeping.\n"
                            + "sleepingPlayers -> the number of players in the current dimension who are sleeping.\n"
                            + "totalPlayers -> the number of players in the current dimension (spectators are not counted).\n"
                            + "sleepingPercentage -> the percentage of players in the current dimension who are sleeping (does not include % symbol).")
                    .define("inBedMessage", "${player} is now sleeping. [${sleepingPlayers}/${totalPlayers}]");

            outOfBedMessage = builder
                    .comment("This message is sent to bedMessageTarget when a player gets out of bed (without being woken up naturally at morning).\n"
                            + "Available variables:\n"
                            + "player -> the player who left their bed.\n"
                            + "sleepingPlayers -> the number of players in the current dimension who are sleeping.\n"
                            + "totalPlayers -> the number of players in the current dimension (spectators are not counted).\n"
                            + "sleepingPercentage -> the percentage of players in the current dimension who are sleeping (does not include % symbol).")
                    .define("outOfBedMessage", "${player} has left their bed. [${sleepingPlayers}/${totalPlayers}]");

            bedMessageType = builder
                    .comment("Sets the message type for inBedMessage and outOfBedMessage.\n"
                            + "SYSTEM: Appears as a message in the chat (e.g., \"Respawn point set\").\n"
                            + "GAME_INFO: Game information that appears above the hotbar (e.g., \"You may not rest now, the bed is too far away\").")
                    .defineEnum("bedMessageType", ChatType.GAME_INFO, ChatType.SYSTEM, ChatType.GAME_INFO);

            bedMessageTarget = builder
                    .comment("Sets the target for inBedMessage and outOfBedMessage.\n"
                            + "ALL: Send the message to all players on the server.\n"
                            + "DIMENSION: Send the message to all players in the current dimension.\n"
                            + "SLEEPING: Only send the message to players who are currently sleeping.")
                    .defineEnum("bedMessageTarget", MessageTarget.DIMENSION);

            builder.pop();
        }

    }

    public static class ClientConfig {

        public final EnumValue<ScreenAlignment> clockAlignment;
        public final IntValue clockScale;
        public final IntValue clockMargin;
        public final BooleanValue preventClockWobble;

        /**
         * Constructs an instance of an Hourglass client config.
         *
         * @param builder  a Forge config builder instance
         */
        public ClientConfig(final ForgeConfigSpec.Builder builder) {
            // gui
            builder.push("gui");

            clockAlignment = builder
                    .comment("Sets the screen alignment of the bed clock.")
                    .defineEnum("clockAlignment", ScreenAlignment.TOP_RIGHT);

            clockScale = builder
                    .comment("Sets the scale of the bed clock.")
                    .defineInRange("clockScale", 64, 1, Integer.MAX_VALUE);

            clockMargin = builder
                    .comment("Sets the distance between the clock and the edge of the screen.\n"
                            + "Unused if clockAlignment is CENTER_CENTER.")
                    .defineInRange("clockMargin", 16, 0, Integer.MAX_VALUE);

            preventClockWobble = builder
                    .comment("This setting prevents clock wobble when getting in bed by updating the clock's position every tick.\n"
                            + "As a side-effect, the clock won't wobble when first viewed as it does in vanilla. This setting is\n"
                            + "unused if displayBedClock is false.")
                    .define("preventClockWobble", true);

            builder.pop();
        }

    }

}
