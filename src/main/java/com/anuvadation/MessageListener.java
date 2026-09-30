package com.anuvadation;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

public class MessageListener extends ListenerAdapter {

    private final DatabaseManager databaseManager =
            new DatabaseManager();

    private final TranslationService translationService =
            new TranslationService();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // Ignore bot messages.
        if (event.getAuthor().isBot()) {
            return;
        }

        // Only process messages from servers.
        if (!event.isFromGuild()) {
            return;
        }

        System.out.println(
                "MESSAGE RECEIVED: "
                        + event.getMessage().getContentDisplay()
        );

        long guildId = event.getGuild().getIdLong();

        // Load this server's saved language/channel configuration.
        Map<String, Long> configuredChannels =
                databaseManager.getLanguageChannels(guildId);

        // Debug: show what was loaded from SQLite.
        System.out.println(
                "Loaded channels for server: "
                        + configuredChannels
        );

        // Nothing has been configured.
        if (configuredChannels.isEmpty()) {
            System.out.println(
                    "No language channels configured for this server."
            );
            return;
        }

        long sourceChannelId =
                event.getChannel().getIdLong();

        // Only translate messages sent in configured channels.
        if (!configuredChannels.containsValue(sourceChannelId)) {
            return;
        }

        String message =
                event.getMessage().getContentDisplay();

        if (message.isBlank()) {
            return;
        }

        // Translate to every other configured language.
        for (Map.Entry<String, Long> entry
                : configuredChannels.entrySet()) {

            String targetLanguage = entry.getKey();
            long targetChannelId = entry.getValue();

            // Don't send back to the original channel.
            if (targetChannelId == sourceChannelId) {
                continue;
            }

            String targetLanguageCode =
                    getLanguageCode(targetLanguage);

            if (targetLanguageCode == null) {
                continue;
            }

            TextChannel targetChannel =
                    event.getJDA().getTextChannelById(
                            targetChannelId
                    );

            if (targetChannel == null) {
                System.out.println(
                        "Could not find channel: "
                                + targetChannelId
                );
                continue;
            }

            try {

                String translatedText =
                        translationService.translate(
                                message,
                                targetLanguageCode
                        );

                String output =
                        "**"
                                + event.getAuthor().getName()
                                + ":** "
                                + translatedText;

                targetChannel
                        .sendMessage(output)
                        .queue();

            } catch (Exception e) {

                System.err.println(
                        "Translation failed for "
                                + targetLanguage
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event) {

        if (!event.getName().equals("setup")) {
            return;
        }

        if (!event.isFromGuild()) {
            event.reply(
                    "This command can only be used inside a server."
            ).queue();
            return;
        }

        String language =
                event.getOption("language")
                        .getAsString()
                        .trim()
                        .toLowerCase();

        long channelId =
                event.getOption("channel")
                        .getAsChannel()
                        .getIdLong();

        long guildId =
                event.getGuild().getIdLong();

        databaseManager.saveChannel(
                guildId,
                language,
                channelId
        );

        System.out.println(
                "Saved configuration: "
                        + language
                        + " -> "
                        + channelId
        );

        event.reply(
                "Saved!\n"
                        + "Language: "
                        + language
                        + "\n"
                        + "Channel: <#"
                        + channelId
                        + ">"
        ).queue();
    }

    private String getLanguageCode(String language) {

        return switch (language) {

            case "english" -> "en";
            case "hindi" -> "hi";
            case "marathi" -> "mr";

            default -> null;
        };
    }
}