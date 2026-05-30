package com.vidlus.conce;

public interface Talker {

    public static Talker get() throws Exception {
        return Setup.getTalkerKind().getTalkerClass().getDeclaredConstructor().newInstance();
    }

    public String talk(String command, UriMime... attachs) throws Exception;

}
