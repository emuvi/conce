package com.vidlus.conce;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkRef {


    private static final Logger logger = LoggerFactory.getLogger(WorkRef.class);


    public final File baseFolder;
    public final Ref ref;
    public final File refFile;
    public final File sourceFile;
    public final String refWithExtension;
    public final Runnable updateStatus;
    public final Talker talker;
    public final Sounder sounder;
    public final RefBase refBase;

    private volatile File workFile;

    public WorkRef(File baseFolder, Ref ref, File refFile, File sourceFile, String refWithExtension, Runnable updateStatus) throws Exception {
        this.baseFolder = baseFolder;
        this.ref = ref;
        this.refFile = refFile;
        this.sourceFile = sourceFile;
        this.refWithExtension = refWithExtension;
        this.updateStatus = updateStatus;
        this.talker = Talker.get();
        this.sounder = Sounder.get();
        this.refBase = RefBase.get();
    }

    public String talk(String command) throws Exception {
        logger.info("Talk Command:\n{}", command);
        return talker.talk(command);
    }

    public String talkWithBase(String command) throws Exception {
        logger.info("Talk Command:\n{}", command);
        var baseURI = refBase.getURIRefs(refWithExtension);
        logger.info("Talk Base URI:\n{}", baseURI);
        return talker.talk(command, UriMime.of(baseURI));
    }

    public void read() throws Exception {
        RefDatex.read(ref, refFile);
        updateStatus.run();
    }

    public void write() throws Exception {
        RefDatex.write(ref, refFile);
        updateStatus.run();
    }


    public boolean hasWorkFile() {
        return workFile != null;
    }

    public File workFile() {
        return workFile;
    }

    public void workFile(File workFile) {
        this.workFile = workFile;
    }


}
