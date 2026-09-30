package com.anuvadation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class AnuvadationBot {

    public static void main(String[] args) throws InterruptedException {

        // Read the Discord bot token from IntelliJ environment variables.
        String token = System.getenv("ANUVADATION_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "ANUVADATION_TOKEN is not configured."
            );
        }

        // Connect to Discord.
        JDA jda = JDABuilder.createLight(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(new MessageListener())
                .build();

        // Wait until the bot is fully connected.
        jda.awaitReady();

        /*
         * Register /setup.
         *
         * Only administrators can use this command.
         */
        jda.updateCommands()
                .addCommands(
                        Commands.slash(
                                        "setup",
                                        "Configure a language channel"
                                )
                                .setDefaultPermissions(
                                        DefaultMemberPermissions.enabledFor(
                                                Permission.ADMINISTRATOR
                                        )
                                )
                                .addOptions(

                                        // Language option
                                        new OptionData(
                                                OptionType.STRING,
                                                "language",
                                                "Language for this channel",
                                                true
                                        ),

                                        // Channel option restricted to text channels
                                        new OptionData(
                                                OptionType.CHANNEL,
                                                "channel",
                                                "Text channel to use",
                                                true
                                        ).setChannelTypes(
                                                ChannelType.TEXT
                                        )
                                )
                )
                .queue();

        System.out.println("Anuvadation is online!");
        System.out.println(
                "Logged in as: "
                        + jda.getSelfUser().getName()
        );
    }
}