package com.vidlus.conce;

import br.com.pointel.jarch.flow.MimeType;

public class UriMime {


    public static UriMime of(String fileUri) {
        if (fileUri == null) {
            return null;
        }
        return new UriMime(fileUri, MimeType.of(fileUri));
    }

    public static UriMime[] of(String... fileUri) {
        if (fileUri == null) {
            return null;
        }
        var uris = new UriMime[fileUri.length];
        for (int i = 0; i < fileUri.length; i++) {
            uris[i] = new UriMime(fileUri[i], MimeType.of(fileUri[i]));
        }
        return uris;
    }


    public final String fileUri;
    public final MimeType mimeType;

    public UriMime(String fileUri, MimeType mimeType) {
        this.fileUri = fileUri;
        this.mimeType = mimeType;
    }

}
