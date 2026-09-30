package com.anuvadation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:anuvadation.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {

        String sql = """
                CREATE TABLE IF NOT EXISTS language_channels (
                    guild_id INTEGER NOT NULL,
                    language TEXT NOT NULL,
                    channel_id INTEGER NOT NULL,
                    PRIMARY KEY (guild_id, language)
                )
                """;

        try (
                Connection connection = DriverManager.getConnection(DATABASE_URL);
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);

            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize database.",
                    e
            );
        }
    }

    public void saveChannel(
            long guildId,
            String language,
            long channelId
    ) {

        String sql = """
                INSERT INTO language_channels
                    (guild_id, language, channel_id)
                VALUES (?, ?, ?)
                ON CONFLICT(guild_id, language)
                DO UPDATE SET channel_id = excluded.channel_id
                """;

        try (
                Connection connection = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, guildId);
            statement.setString(2, language);
            statement.setLong(3, channelId);

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save channel configuration.",
                    e
            );
        }
    }

    public Map<String, Long> getLanguageChannels(long guildId) {

        Map<String, Long> channels = new HashMap<>();

        String sql = """
                SELECT language, channel_id
                FROM language_channels
                WHERE guild_id = ?
                """;

        try (
                Connection connection = DriverManager.getConnection(DATABASE_URL);
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, guildId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String language =
                            resultSet.getString("language");

                    long channelId =
                            resultSet.getLong("channel_id");

                    channels.put(language, channelId);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load channel configuration.",
                    e
            );
        }

        return channels;
    }
}