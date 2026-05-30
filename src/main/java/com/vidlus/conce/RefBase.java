package com.vidlus.conce;

import java.io.File;

public interface RefBase {

    public static RefBase get() throws Exception {
        return Setup.getRefBaseKind().getRefBaseClass().getDeclaredConstructor().newInstance();
    }

    public void upload(File origin) throws Exception;

    public void upload(File origin, String refWithExtension) throws Exception;

    public void download(String refWithExtension, File destiny) throws Exception;

    public String getURIRefs(String refWithExtension);

}
