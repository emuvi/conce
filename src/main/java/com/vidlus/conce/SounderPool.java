package com.vidlus.conce;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizString;


public class SounderPool implements Sounder {

    public void sound(WorkRef workRef, RefGroup group) throws Exception {
        var poolFolder = new File(workRef.baseFolder, "+ Pool");
        if (!poolFolder.exists()) {
            poolFolder.mkdirs();
        }
        var mp3Files = new ArrayList<File>();
        for (var file : poolFolder.listFiles()) {
            if (file.getName().endsWith(".mp3")) {
                mp3Files.add(file);
            }
        }
        var selectedName = FilenameUtils.getBaseName(workRef.workFile().getName());
        File mp3File = null;
        var minDiff = Integer.MAX_VALUE;
        for (var file : mp3Files) {
            var mp3Name = FilenameUtils.getBaseName(file.getName());
            var difference = WizString.getDistanceWords(selectedName, mp3Name);
            if (mp3Name.equals(selectedName)) {
                mp3File = file;
                break;
            } else if (difference < minDiff) {
                mp3File = file;
                minDiff = difference;
            }
        }
        if (mp3File == null) {
            throw new Exception("No mp3 file found in pool folder.");
        }
        var targetFile = new File(workRef.workFile().getParentFile(), selectedName + ".mp3");
        Files.move(mp3File.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        var titrationFile = group.getTitrationFile(workRef.baseFolder);
        CKUtils.putMarkDownLink(titrationFile, targetFile.getName());
        WizGUI.showNotify("Sound inserted.", 1);
    }

}
