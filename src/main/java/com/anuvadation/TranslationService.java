package com.anuvadation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String translate(String text, String targetLanguage) {

        /*
         * First, try translating directly from the automatically
         * detected source language to the target language.
         *
         * This handles:
         * English  -> Hindi
         * English  -> Marathi
         * Hindi    -> English
         * Marathi  -> English
         * and any other pair supported directly by the provider.
         */
        try {
            return translateDirect(
                    text,
                    "autodetect",
                    targetLanguage
            );
        } catch (RuntimeException directError) {

            /*
             * If direct translation fails, use English as a bridge.
             *
             * Example:
             * Marathi -> English -> Hindi
             *
             * This avoids the previous problem where we accidentally
             * tried:
             * English -> English -> Hindi
             */
            if (!targetLanguage.equals("en")) {

                String englishText = translateDirect(
                        text,
                        "autodetect",
                        "en"
                );

                return translateDirect(
                        englishText,
                        "en",
                        targetLanguage
                );
            }

            // If English was the target and direct translation failed,
            // there is no bridge we need to use.
            throw directError;
        }
    }

    private String translateDirect(
            String text,
            String sourceLanguage,
            String targetLanguage) {

        String encodedText = URLEncoder.encode(
                text,
                StandardCharsets.UTF_8
        );

        String encodedLanguagePair = URLEncoder.encode(
                sourceLanguage + "|" + targetLanguage,
                StandardCharsets.UTF_8
        );

        String url =
                "https://api.mymemory.translated.net/get" +
                        "?q=" + encodedText +
                        "&langpair=" + encodedLanguagePair;

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "MyMemory HTTP error: "
                                + response.statusCode()
                );
            }

            JsonObject json = JsonParser
                    .parseString(response.body())
                    .getAsJsonObject();

            int responseStatus = json
                    .get("responseStatus")
                    .getAsInt();

            if (responseStatus != 200) {

                String details = json.has("responseDetails")
                        ? json.get("responseDetails").getAsString()
                        : "Unknown error";

                throw new RuntimeException(
                        "MyMemory translation error: "
                                + details
                );
            }

            return json
                    .getAsJsonObject("responseData")
                    .get("translatedText")
                    .getAsString();

        } catch (Exception e) {

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new RuntimeException(
                    "Translation request failed: "
                            + e.getMessage(),
                    e
            );
        }
    }
}