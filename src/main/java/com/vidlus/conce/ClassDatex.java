package com.vidlus.conce;

import java.io.File;
import java.util.Date;

import br.com.pointel.jarch.mage.WizProps;
import br.com.pointel.jarch.mage.WizString;
import br.com.pointel.jarch.mage.WizText;
import br.com.pointel.jarch.mage.WizUtilDate;

public class ClassDatex {

    public static ClassData create(File file) throws Exception {
        var classData = new ClassData();
        classData.createdAt = WizUtilDate.formatDateMach(new Date());
        write(classData, file);
        return classData;
    }

    public static ClassData read(File file) throws Exception {
        var result = new ClassData();
        if (!file.exists()) {
            result.createdAt = WizUtilDate.formatDateMach(new Date());
            return result;
        }
        var source = WizText.read(file).trim();
        var lines = WizString.getLines(source);
        if (lines.length == 0) {
            result.createdAt = WizUtilDate.formatDateMach(new Date());
            return result;
        }
        var propsSource = new StringBuilder();
        var insideProps = false;
        var doneProps = false;
        for (var line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            line = line.trim();
            if (!doneProps && line.equals("---")) {
                if (!insideProps) {
                    insideProps = true;
                } else {
                    insideProps = false;
                    doneProps = true;
                }
                continue;
            }
            if (insideProps) {
                propsSource.append(line).append("\n");
            } else {
                if (line.startsWith("[[") && line.endsWith("]]")) {
                    var link = CKUtils.delBrackets(line);
                    if (link.startsWith("♦")) {
                        result.explainsLinks.add(link);
                    } else if (link.startsWith("♣")) {
                        result.didacticLinks.add(link);
                    } else {
                        result.cardsLinks.add(link);
                    }
                }
            }
        }
        var mapProps = WizProps.fromSource(propsSource.toString(), ": ");
        result.createdAt = mapProps.getOrDefault("created-at", "");
        result.updatedAt = mapProps.getOrDefault("updated-at", "");
        return result;
    }

    public static void write(ClassData classData, File file) throws Exception {
        classData.updatedAt = WizUtilDate.formatDateMach(new Date());
        var builder = new StringBuilder();
        builder.append("---\n");
        builder.append("nature: class\n");
        if (classData.createdAt != null && !classData.createdAt.isBlank()) {
            builder.append("created-at: ").append(classData.createdAt).append("\n");
        }
        if (classData.updatedAt != null && !classData.updatedAt.isBlank()) {
            builder.append("updated-at: ").append(classData.updatedAt).append("\n");
        }
        builder.append("---\n");
        for (var link : classData.explainsLinks) {
            builder.append(CKUtils.putBrackets(link)).append("\n\n");
        }
        for (var link : classData.didacticLinks) {
            builder.append(CKUtils.putBrackets(link)).append("\n\n");
        }
        for (var link : classData.cardsLinks) {
            builder.append(CKUtils.putBrackets(link)).append("\n\n");
        }
        WizText.write(file, builder.toString().trim());
    }


}
