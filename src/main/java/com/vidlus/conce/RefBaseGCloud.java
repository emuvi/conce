package com.vidlus.conce;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import br.com.pointel.jarch.mage.WizBytes;
import br.com.pointel.jarch.mage.WizEnv;

public class RefBaseGCloud implements RefBase {

    private final String BUCKET_NAME = WizEnv.get("CONCE_KNOW_REFS_BASE_GCLOUD_BUCKET", "");
    private final String ACCESS_TOKEN = WizEnv.get("CONCE_KNOW_REFS_BASE_GCLOUD_API_KEY", "");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void upload(File origin) throws Exception {
        var hashMD5 = "& " + WizBytes.getMD5(origin);
        var ext = FilenameUtils.getExtension(origin.getName());
        var refWithExtension = hashMD5 + (ext.isEmpty() ? "" : "." + ext);
        upload(origin, refWithExtension);
    }

    @Override
    public void upload(File origin, String refWithExtension) throws Exception {
        String objectName = getObjectPath(refWithExtension);
        String url = "https://storage.googleapis.com/upload/storage/v1/b/" + BUCKET_NAME + "/o?uploadType=media&name=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", Files.probeContentType(origin.toPath()))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofFile(origin.toPath()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Error uploading file to GCloud: " + response.body());
        }
    }

    @Override
    public void download(String refWithExtension, File destiny) throws Exception {
        String objectName = getObjectPath(refWithExtension);
        String url = "https://storage.googleapis.com/storage/v1/b/" + BUCKET_NAME + "/o/" + URLEncoder.encode(objectName, StandardCharsets.UTF_8) + "?alt=media";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .GET()
                .build();

        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destiny.toPath()));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Error downloading file from GCloud: " + response.body());
        }
    }

    @Override
    public String getURIRefs(String refWithExtension) {
         return "https://storage.googleapis.com/" + BUCKET_NAME + "/" + getObjectPath(refWithExtension);
    }
    
    private String getObjectPath(String refWithExtension) {
        return refWithExtension.substring(0, 4) + "/" + refWithExtension;
    }

}
