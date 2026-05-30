package com.vidlus.conce;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnkiCsvHelper {

    private static final Logger logger = LoggerFactory.getLogger(AnkiCsvHelper.class);
    private static final URI ankiUri = URI.create("http://localhost:8765");
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void setupDeckFromCsv(String deckName, File csvFile) throws Exception {
        logger.info("Checking if deck exists: " + deckName);
        String deckNamesJson = "{\"action\": \"deckNames\", \"version\": 6}";
        String responseNames = sendRequest(deckNamesJson);
        if (responseNames.contains("\"" + escapeJson(deckName) + "\"")) {
            String deleteDeckJson = String.format(
                "{\"action\": \"deleteDecks\", \"version\": 6, \"params\": {\"decks\": [\"%s\"], \"cardsToo\": true}}", 
                escapeJson(deckName)
            );
            sendRequest(deleteDeckJson);
        }
        
        logger.info("Creating deck: " + deckName);
        String createDeckJson = String.format(
            "{\"action\": \"createDeck\", \"version\": 6, \"params\": {\"deck\": \"%s\"}}", 
            escapeJson(deckName)
        );
        sendRequest(createDeckJson);
        
        logger.info("Reading CSV and importing cards...");
        
        List<String> lines = Files.readAllLines(csvFile.toPath());
        List<String> notes = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(";", 2); 
            if (parts.length < 2) continue;
            if (parts[0] == null || parts[0].isBlank() || parts[1] == null || parts[1].isBlank()) {
                continue;
            }

            String front = escapeJson(removeQuotes(parts[0])).trim();
            String back = escapeJson(removeQuotes(parts[1])).trim();

            String noteJson = String.format(
                "{\"deckName\": \"%s\", \"modelName\": \"Base\", \"fields\": {\"Front\": \"%s\", \"Back\": \"%s\"}}",
                escapeJson(deckName), front, back
            );
            notes.add(noteJson);
        }

        if (notes.isEmpty()) {
            logger.info("No valid cards to import from {}.", csvFile.getName());
            return;
        }

        String notesArray = "[" + String.join(", ", notes) + "]";

        String addNotesJson = String.format(
            "{\"action\": \"addNotes\", \"version\": 6, \"params\": {\"notes\": %s}}", 
            notesArray
        );
        
        String response = sendRequest(addNotesJson);
        logger.info("Success! Anki response: " + response);
    }

    private static String sendRequest(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(ankiUri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        if (responseBody.contains("\"error\"") && !responseBody.contains("\"error\": null")) {
            throw new Exception("Anki error: " + responseBody);
        }
        return decodeUnicode(responseBody);
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static String removeQuotes(String text) {
        if (text == null || text.length() < 2) {
            return text;
        }
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static String decodeUnicode(String input) {
        if (input == null) return null;
        StringBuilder result = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 5 < len && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    result.append((char) code);
                    i += 5;
                    continue;
                } catch (NumberFormatException e) {}
            }
            result.append(c);
        }
        return result.toString();
    }

}