package com.vidlus.conce;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.vidlus.conce.talkers.gemini.Content;
import com.vidlus.conce.talkers.gemini.ErrorResponse;
import com.vidlus.conce.talkers.gemini.Part;
import com.vidlus.conce.talkers.gemini.Request;
import com.vidlus.conce.talkers.gemini.Response;

import br.com.pointel.jarch.mage.WizEnv;

public class TalkerGCloud implements Talker {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public String talk(String command, UriMime... attachs) throws Exception {
        var parts = new ArrayList<Part>();
        parts.add(new Part(command));
        if (attachs != null) {
            for (var attach : attachs) {
                // The Google Cloud API supports file uploads, but this is not yet implemented.
            }
        }
        var contents = new ArrayList<Content>();
        contents.add(new Content(parts, "user"));
        var requestBody = new Request(contents);

        // TODO: Replace "YOUR_PROJECT_ID" with your Google Cloud project ID.
        String projectId = "project-14a4c4d2-ed7b-4aec-a05"; 
        String url = "https://aiplatform.googleapis.com/v1/projects/" + projectId + "/locations/global/publishers/google/models/" + Setup.getGenaiModel().code() + ":generateContent";

        // To get the access token, run the following command in your terminal:
        // gcloud auth print-access-token
        String accessToken = WizEnv.get("CHARVS_KNOW_GCLOUD_API_KEY", "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
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
                throw new RuntimeException("GCloud API Error: " + errorResponse.error.message);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            // Not valid JSON, continue to normal response parsing
        }
        
        Response response = gson.fromJson(responseBody, Response.class);
        if (response == null || response.candidates == null || response.candidates.isEmpty()) {
            throw new RuntimeException("Invalid response from GCloud API. Response: " + responseBody);
        }
        return response.candidates.get(0).content.parts.get(0).text;
    }

}
