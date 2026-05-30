package com.vidlus.conce;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.pointel.jarch.mage.WizText;

public class AtomicNote {

    public Map<String, String> props = new LinkedHashMap<>();
    public String note = "";
    public String tags = "";
    public String refs = "";

    public AtomicNote() {}

    public static AtomicNote read(File file) throws Exception {
        var source = WizText.read(file).trim();
        var result = new AtomicNote();
        result.props = extractProps(source);
        result.note = extractNote(source);
        result.tags = extractTags(source);
        result.refs = extractRefs(source);
        return result;
    }

    public static Map<String, String> extractProps(String source) {
        var result = new LinkedHashMap<String, String>();
        if (source.startsWith("---")) {
            int endProps = source.indexOf("---", 3);
            if (endProps > -1) {
                var propsStr = source.substring(3, endProps).trim();
                var lines = propsStr.split("\\r?\\n");
                for (var line : lines) {
                    var parts = line.split(":", 2);
                    if (parts.length == 2) {
                        result.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        return result;
    }

    public static String extractNote(String source) {
        if (source.startsWith("---")) {
            int endProps = source.indexOf("---", 3);
            if (endProps > -1) {
                source = source.substring(endProps + 3).trim();
            }
        }
        int indexTags = source.lastIndexOf("Tags:");
        int indexRefs = source.lastIndexOf("Refs:");
        int end = source.length();
        if (indexTags > -1) {
            int start = indexTags;
            if (start > 0 && source.charAt(start - 1) == '*') {
                start--;
            }
            end = Math.min(end, start);
        }
        if (indexRefs > -1) {
            int start = indexRefs;
            if (start > 0 && source.charAt(start - 1) == '*') {
                start--;
            }
            end = Math.min(end, start);
        }
        return source.substring(0, end).trim();
    }
    
    public static String extractTags(String source) {
        int indexTags = source.lastIndexOf("Tags:");
        if (indexTags == -1) {
            return "";
        }
        int indexRefs = source.lastIndexOf("Refs:");
        String raw;
        if (indexTags > indexRefs) {
            raw = source.substring(indexTags + 5);
        } else {
            int end = indexRefs;
            if (end > 0 && source.charAt(end - 1) == '*') {
                end--;
            }
            raw = source.substring(indexTags + 5, end);
        }
        return cleanRaw(raw);
    }

    public static String extractRefs(String source) {
        int indexRefs = source.lastIndexOf("Refs:");
        if (indexRefs == -1) {
            return "";
        }
        int indexTags = source.lastIndexOf("Tags:");
        String raw;
        if (indexRefs > indexTags) {
            raw = source.substring(indexRefs + 5);
        } else {
            int end = indexTags;
            if (end > 0 && source.charAt(end - 1) == '*') {
                end--;
            }
            raw = source.substring(indexRefs + 5, end);
        }
        return cleanRaw(raw);
    }

    private static String cleanRaw(String raw) {
        raw = raw.trim();
        if (raw.startsWith("*")) {
            raw = raw.substring(1).trim();
        }
        return raw;
    }

    public static void write(AtomicNote atomicNote, File file) throws Exception {
        var builder = new StringBuilder();
        if (!atomicNote.props.isEmpty()) {
            builder.append("---\n");
            for (var entry : atomicNote.props.entrySet()) {
                builder.append(entry.getKey().trim())
                        .append(": ").append(entry.getValue().trim())
                        .append("\n");
            }
            builder.append("---\n");
        }
        builder.append(atomicNote.note.trim());
        if (atomicNote.tags != null && !atomicNote.tags.isBlank()) {
            builder.append("\n\n*Tags:* ").append(atomicNote.tags.trim());
        }
        if (atomicNote.refs != null && !atomicNote.refs.isBlank()) {
            builder.append("\n\n*Refs:* ").append(atomicNote.refs.trim());
        }
        WizText.write(file, builder.toString().trim());
    }

}
