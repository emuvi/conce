package com.vidlus.conce;

import java.io.File;

public class RefGroup {

    public String order = "";
    public String classification = "";
    public String hierarchy = "";
    public String titration = "";
    public String topics = "";
    public String cardsAt = "";
    public String questsAt = "";
    public String explainsAt = "";
    public String didacticAt = "";

    public RefGroup() {}

    public boolean isPresent() {
        return topics != null && !topics.isBlank();
    }

    public void clearIdentified() {
        order = "";
        classification = "";
        titration = "";
        topics = "";
    }

    public void clearOrganized() {
        order = "";
        classification = "";
        titration = "";
    }

    public void clearClassified() {
        order = "";
        classification = "";
    }

    public void writeClassification(File onBaseFolder) throws Exception {
        if (classification == null || classification.isBlank()) {
            return;
        }
        File actualFolder = null;
        var hierarchy = classification.split("\\-");
        for (int i = 0; i < hierarchy.length; i++) {
            var level = hierarchy[i].trim();
            if (level.isBlank()) {
                continue;
            }
            level = "- " + level;
            File markDownFile;
            if (actualFolder == null) {
                actualFolder = onBaseFolder;
                markDownFile = new File(actualFolder, "index.md");
            } else {
                markDownFile = new File(actualFolder, actualFolder.getName() + ".md");
            }
            CKUtils.putMarkDownLink(markDownFile, level);
            actualFolder = new File(actualFolder, level);
        }
        if (actualFolder != null) {
            if (!actualFolder.exists()) {
                actualFolder.mkdirs();
            }
            var classificationFile = new File(actualFolder, actualFolder.getName() + ".md");
            if (!classificationFile.exists()) {
                ClassDatex.create(classificationFile);
            }
        }
    }

    public File getClassificationFolder(File onBaseFolder) throws Exception {
        if (classification == null || classification.isBlank()) {
            return null;
        }
        File actualFolder = onBaseFolder;
        var hierarchy = classification.split("\\-");
        for (int i = 0; i < hierarchy.length; i++) {
            var level = hierarchy[i].trim();
            if (level.isBlank()) {
                continue;
            }
            level = "- " + level;
            actualFolder = new File(actualFolder, level);
        }
        if (!actualFolder.exists()) {
            actualFolder.mkdirs();
        }
        return actualFolder;
    }

    public File getClassificationFile(File onBaseFolder) throws Exception {
        if (classification == null || classification.isBlank()) {
            return null;
        }
        File folder = getClassificationFolder(onBaseFolder);
        return new File(folder, folder.getName() + ".md");
    }

    public File getTitrationFile(File onBaseFolder) throws Exception {
        if (classification == null || classification.isBlank()) {
            return null;
        }
        if (titration == null || titration.isBlank()) {
            return null;
        }
        var folder = getClassificationFolder(onBaseFolder);
        if (folder == null) {
            return null;
        }
        return new File(folder, CKUtils.delBrackets(titration) + ".md");
    }

    public File getQuestsFile(File onBaseFolder) throws Exception {
        if (classification == null || classification.isBlank()) {
            return null;
        }
        if (titration == null || titration.isBlank()) {
            return null;
        }
        var folder = getClassificationFolder(onBaseFolder);
        if (folder == null) {
            return null;
        }
        return new File(folder, CKUtils.delBrackets(titration) + ".csv");
    }

}
