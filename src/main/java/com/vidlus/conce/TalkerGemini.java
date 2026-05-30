package com.vidlus.conce;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.vidlus.conce.talkers.gemini.Content;
import com.vidlus.conce.talkers.gemini.ErrorResponse;
import com.vidlus.conce.talkers.gemini.Part;
import com.vidlus.conce.talkers.gemini.Request;
import com.vidlus.conce.talkers.gemini.Response;

import br.com.pointel.jarch.mage.WizEnv;

public class TalkerGemini implements Talker {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public String talk(String command, UriMime... attachs) throws Exception {
        var parts = new ArrayList<Part>();
        parts.add(new Part(command));
        if (attachs != null) {
            for (var attach : attachs) {
                // The free version of the Gemini API does not support file uploads.
                // This part is left for future implementation.
            }
        }
        var contents = new ArrayList<Content>();
        contents.add(new Content(parts, "user"));
        var requestBody = new Request(contents);

        String apiKey = WizEnv.get("CHARVS_KNOW_GENAI_API_KEY", "");
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + Setup.getGenaiModel().code()
                + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return sendMessage(response.body());
    }

    private String sendMessage(String responseBody) {
        // Try to parse as error response first
        try {
            ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
            if (errorResponse != null && errorResponse.error != null && errorResponse.error.message != null) {
                throw new RuntimeException("Gemini API Error: " + errorResponse.error.message);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            // Not valid JSON, continue to normal response parsing
        }
        
        Response response = gson.fromJson(responseBody, Response.class);
        if (response == null || response.candidates == null || response.candidates.isEmpty()) {
            throw new RuntimeException("Invalid response from Gemini API. Response: " + responseBody);
        }
        return response.candidates.get(0).content.parts.get(0).text;
    }

}
