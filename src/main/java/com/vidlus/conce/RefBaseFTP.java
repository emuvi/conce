package com.vidlus.conce;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import br.com.pointel.jarch.flow.FTP;
import br.com.pointel.jarch.mage.WizBytes;
import br.com.pointel.jarch.mage.WizEnv;

public class RefBaseFTP implements RefBase {


    public final String BASE_FTP_URI = WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_URI", "https://urvs.com.br/");
    public final String BASE_FTP_REFS = WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_FOLDER", "Conhecer/+ Refs/");
    public final String BASE_FTP_INSIDE = WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_INSIDE", "public_html/");
    public final String BASE_FTP_URI_REFS = BASE_FTP_URI + BASE_FTP_REFS;


    private final FTP ftp = new FTP(
        WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_HOST", "ftp.urvs.com.br"),
        WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_PORT", 21),
        WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_USER", "urvs1"),
        WizEnv.get("CONCE_KNOW_REFS_BASE_FTP_PASS", ""));

    @Override
    public void upload(File origin) throws Exception {
        if (!ftp.isConnected()) {
            ftp.connect();
        }
        var hashMD5 = "& " + WizBytes.getMD5(origin);
        var ext = FilenameUtils.getExtension(origin.getName());
        var refWithExtension = hashMD5 + (ext.isEmpty() ? "" : "." + ext);
        ftp.openWithMakeDir(getInsideRefsFolder(refWithExtension));
        ftp.upload(origin, refWithExtension);
    }

    @Override
    public void upload(File origin, String refWithExtension) throws Exception {
        if (!ftp.isConnected()) {
            ftp.connect();
        }
        ftp.openWithMakeDir(getInsideRefsFolder(refWithExtension));
        ftp.upload(origin, refWithExtension);
    }

    @Override
    public void download(String refWithExtension, File destiny) throws Exception {
        if (!ftp.isConnected()) {
            ftp.connect();
        }
        var remote = getInsideRefsFolder(refWithExtension) + "/" + refWithExtension;
        ftp.download(remote, destiny);
    }

    @Override
    public String getURIRefs(String refWithExtension) {
        return getURIRefsFolder(refWithExtension) + "/" + refWithExtension;
    }

    private String getInsideRefsFolder(String refWithExtension) {
        return BASE_FTP_INSIDE + BASE_FTP_REFS + refWithExtension.substring(0, 4);
    }

    private String getURIRefsFolder(String refWithExtension) {
        return BASE_FTP_URI + BASE_FTP_REFS + refWithExtension.substring(0, 4);
    }

}
