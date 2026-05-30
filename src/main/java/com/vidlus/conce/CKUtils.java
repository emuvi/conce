package com.vidlus.conce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.pointel.jarch.data.Pair;
import br.com.pointel.jarch.mage.WizString;

public class CKUtils {

    public static List<String> getAllClassifications(File onBaseFolder) throws Exception {
        var result = new ArrayList<String>();
        for (var inside : onBaseFolder.listFiles()) {
            if (inside.isDirectory() && inside.getName().startsWith("- ")) {
                getAllClassifications(result, inside, 1, onBaseFolder.getAbsolutePath().length());
            }
        }
        return result;
    }

    private static void getAllClassifications(List<String> result, File folder, int level, int baseFolderLength) throws Exception {
        if (level == 4) {
            var classification = folder.getAbsolutePath().substring(baseFolderLength);
            classification = classification.replace(File.separator, " ");
            result.add(classification.trim());
            return;
        }
        for (var inside : folder.listFiles()) {
            if (inside.isDirectory() && inside.getName().startsWith("- ")) {
                getAllClassifications(result, inside, level + 1, baseFolderLength);
            }
        }
    }

    public static void putMarkDownLink(File classFile, String link) throws Exception {
        if (link == null || link.isBlank()) {
            return;
        }
        if (!classFile.exists()) {
            var folder = classFile.getParentFile();
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    throw new Exception("Failed to create class folder: " + folder.getAbsolutePath());
                }
            }
        }
        var classData = ClassDatex.read(classFile);
        link = delBrackets(link);
        if (link.startsWith("♦")) {
            if (!classData.explainsLinks.contains(link)) {
                classData.explainsLinks.add(link);
            }
        } else if (link.startsWith("♣")) {
            if (!classData.didacticLinks.contains(link)) {
                classData.didacticLinks.add(link);
            }
        } else {
            if (!classData.cardsLinks.contains(link)) {
                classData.cardsLinks.add(link);
            }
        }
        ClassDatex.write(classData, classFile);
    }

    public static void delMarkDownLink(File classFile, String link) throws Exception {
        if (link == null || link.isBlank()) {
            return;
        }
        if (!classFile.exists()) {
            return;
        }
        var classData = ClassDatex.read(classFile);
        link = delBrackets(link);
        var updated = false;
        if (link.startsWith("♦")) {
            updated = classData.explainsLinks.remove(link);
        } else if (link.startsWith("♣")) {
            updated = classData.didacticLinks.remove(link);
        } else  {
            updated = classData.cardsLinks.remove(link);
        }
        if (updated) {
            ClassDatex.write(classData, classFile);
        }
    }

    public static List<String> filterMarkDownLinks(List<String> onListOfLinks) {
        var result = new ArrayList<String>();
        for (var link : onListOfLinks) {
            if (link == null || link.isBlank()) {
                continue;
            }
            var testLink = delBrackets(link);
            if (testLink.toLowerCase().endsWith(".md")) {
                result.add(link);
            } else {
                var lastDot = testLink.lastIndexOf('.');
                if (lastDot == -1 || lastDot < testLink.length() - 7){
                    result.add(link);
                }
            }
        }
        return result;
    }

    public static String cleanFileName(String title) {
        title = title.trim();
        if (title.equals(title.toUpperCase())) {
            title = WizString.capitalizeWords(title.toLowerCase());
        }
        title = title
                .replace("\"", "”")
                .replace("'", "”")
                .replace("/", "-")
                .replace("|", "-")
                .replace("\\", "-")
                .replace("?", "")
                .replace("!", "")
                .replace("<", "")
                .replace(">", "")
                .replace("*", "")
                .replace("#", "")
                .replace(": ", " - ")
                .replace(":", ",")
                .replace(";", ",");
        return title.replaceAll("\\s+", " ").trim();
    }

    public static String cleanBracketsLinks(String source) {
        if (source == null || source.isBlank()) {
            return source;
        }
        var links = getBracketsLinks(source);
        if (links.isEmpty()) {
            return source;
        }
        var builder = new StringBuilder();
        int lastEnd = 0;
        for (var link : links) {
            builder.append(source, lastEnd, link.key());
            builder.append("[[");
            var content = source.substring(link.key() + 2, link.val() - 2);
            var cleaned = cleanFileName(content);
            builder.append(cleaned);
            builder.append("]]");
            lastEnd = link.val();
        }
        if (lastEnd < source.length()) {
            builder.append(source, lastEnd, source.length());
        }
        return builder.toString();
    }

    public static List<String> putBrackets(List<String> links) {
        if (links == null) {
            return null;
        }
        for (int i = 0; i < links.size(); i++) {
            links.set(i, putBrackets(links.get(i)));
        }
        return links;
    }

    public static String putBrackets(String link) {
        if (link == null || link.isBlank()) {
            return "";
        }
        if (!link.startsWith("[[")) {
            link = "[[" + link;
        }
        if (!link.endsWith("]]")) {
            link = link + "]]";
        }
        return link;
    }

    public static String delBrackets(String link) {
        if (link == null || link.isBlank()) {
            return link;
        }
        if (link.startsWith("[[")) {
            link = link.substring(2);
        }
        if (link.endsWith("]]")) {
            link = link.substring(0, link.length() - 2);
        }
        return link;
    }

    public static List<Pair<Integer, Integer>> getBracketsLinks(String source) {
        var result = new ArrayList<Pair<Integer, Integer>>();
        if (source == null || source.isBlank()) {
            return result;
        }
        var start = source.indexOf("[[");
        while (start > -1) {
            var end = source.indexOf("]]", start);
            if (end > -1) {
                result.add(new Pair<>(start, end + 2));
                start = source.indexOf("[[", end + 2);
            } else {
                break;
            }
        }
        return result;
    }

}
