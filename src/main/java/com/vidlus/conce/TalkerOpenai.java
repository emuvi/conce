package com.vidlus.conce;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.vidlus.conce.talkers.openai.ErrorResponse;
import com.vidlus.conce.talkers.openai.Message;
import com.vidlus.conce.talkers.openai.Request;
import com.vidlus.conce.talkers.openai.Response;

import br.com.pointel.jarch.mage.WizEnv;

public class TalkerOpenai implements Talker {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public String talk(String command, UriMime... attachs) throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", command));
        Request requestBody = new Request(Setup.getOpenaiModel().code(), messages);

        String apiKey = WizEnv.get("CONCE_KNOW_OPENAI_API_KEY", "");
        String url = "https://api.openai.com/v1/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return parseResponse(response.body());
    }

    private String parseResponse(String responseBody) {
        // Try to parse as error response first
        try {
            ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
            if (errorResponse != null && errorResponse.error != null && errorResponse.error.message != null) {
                throw new RuntimeException("OpenAI API Error: " + errorResponse.error.message);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            // Not valid JSON, continue to normal response parsing
        }
        
        Response response = gson.fromJson(responseBody, Response.class);
        if (response == null || response.choices == null || response.choices.isEmpty()) {
            throw new RuntimeException("Invalid response from OpenAI API. Response: " + responseBody);
        }
        return response.choices.get(0).message.content;
    }

}