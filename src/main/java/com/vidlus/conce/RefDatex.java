package com.vidlus.conce;

import java.io.File;
import java.util.ArrayList;

import br.com.pointel.jarch.flow.Datex;
import br.com.pointel.jarch.flow.DatexNode;
import br.com.pointel.jarch.flow.DatexToken;
import br.com.pointel.jarch.mage.WizProps;
import br.com.pointel.jarch.mage.WizString;
import br.com.pointel.jarch.mage.WizText;

public class RefDatex {

    
    private static final String propsName = "props";
    private static final String propsStart = "---";
    private static final String propsEnd = "---";
    private static final String propsSeparator = ": ";

    private static final String memoaName = "memoa";
    private static final String memoaStart = "## Memoa";
    private static final String memoaEnd = "^^^";

    private static final String groupName = "group";
    private static final String groupStart = "## Grupo ";
    private static final String groupEnd = "^^^";

    private static final DatexNode nodeProps = DatexNode.must(propsName, DatexToken.literal(propsStart), DatexToken.literal(propsEnd));
    private static final DatexNode nodeMemoa = DatexNode.may(memoaName, DatexToken.literal(memoaStart), DatexToken.literal(memoaEnd));
    
    
    private static final String organizationName = "organization";
    private static final String organizationStart = "### Organização";
    private static final String organizationEnd = "~--";

    private static final String topicsName = "topics";
    private static final String topicsStart = "### Tópicos";
    private static final String topicsEnd = "~--";

    private static final String realizationName = "realization";
    private static final String realizationStart = "### Realização";
    private static final String realizationEnd = "~--";

    private static final DatexNode nodeOrganization = DatexNode.must(organizationName, DatexToken.literal(organizationStart), DatexToken.literal(organizationEnd));
    private static final DatexNode nodeTopics = DatexNode.must(topicsName, DatexToken.literal(topicsStart), DatexToken.literal(topicsEnd));
    private static final DatexNode nodeRealization = DatexNode.must(realizationName, DatexToken.literal(realizationStart), DatexToken.literal(realizationEnd));

    private static final Datex datexGroup = new Datex(DatexNode.of(nodeOrganization, nodeTopics, nodeRealization));
    

    public static final synchronized Ref read(File file) throws Exception {
        var ref = new Ref();
        read(ref, file);
        return ref;
    }

    public static final synchronized void read(Ref ref, File file) throws Exception {
        var source = WizText.read(file);
        var nodes = new ArrayList<DatexNode>();
        nodes.add(nodeProps);
        nodes.add(nodeMemoa);
        int index = 1;
        var groupNodes = new ArrayList<DatexNode>();
        while (sourceHasGroup(source, index)) {
            var nodeGroup = DatexNode.must(getGroupName(index), DatexToken.literal(getGroupStart(index)), DatexToken.literal(groupEnd));
            nodes.add(nodeGroup);
            groupNodes.add(nodeGroup);
            index++;
        }
        var datexRoot = new Datex(nodes);
        datexRoot.parse(source);
        parseProps(ref);
        parseMemoa(ref);
        for (var nodeGroup : groupNodes) {
            var refGroup = new RefGroup();
            parseGroup(nodeGroup, refGroup);
            ref.groups.add(refGroup);
        }
    }

    private static void parseProps(Ref ref) {
        var props = WizProps.fromSource(nodeProps.getValue(), propsSeparator);
        ref.props.hashMD5 = dressHash(props.getOrDefault("hash-md5", ""));
        ref.props.createdAt = props.getOrDefault("created-at", "");
        ref.props.memoedAt = props.getOrDefault("memoed-at", "");
        ref.props.uploadedAt = props.getOrDefault("uploaded-at", "");
        ref.props.identifiedAt = props.getOrDefault("identified-at", "");
        ref.props.organizedAt = props.getOrDefault("organized-at", "");
        ref.props.classifiedAt = props.getOrDefault("classified-at", "");
        ref.props.doneAt = props.getOrDefault("done-at", "");
        ref.props.revisedAt = props.getOrDefault("revised-at", "");
        ref.props.revisedCount = props.getOrDefault("revised-count", "");
    }

    private static void parseMemoa(Ref ref) {
        if (nodeMemoa.isPresent()) {
            ref.memoa.text = nodeMemoa.getValue().trim();
        }
    }

    private static void parseGroup(DatexNode node, RefGroup group) throws Exception {
        if (node.isPresent()) {
            datexGroup.parse(node.getValue());
            var propsOrganization = WizProps.fromSource(nodeOrganization.getValue(), propsSeparator);
            group.order = propsOrganization.getOrDefault("Ordem", "");
            group.classification = propsOrganization.getOrDefault("Classificação", "");
            group.hierarchy = propsOrganization.getOrDefault("Hierarquia", "");
            group.titration = propsOrganization.getOrDefault("Titulação", "");
            group.topics = nodeTopics.getValue().trim();
            var propsRealization = WizProps.fromSource(nodeRealization.getValue(), propsSeparator);
            group.cardsAt = propsRealization.getOrDefault("Cartões em", "");
            group.questsAt = propsRealization.getOrDefault("Questões em", "");
            group.explainsAt = propsRealization.getOrDefault("Redações em", "");
            group.didacticAt = propsRealization.getOrDefault("Didática em", "");
        }
    }

    public static final void write(Ref ref, File file) throws Exception {
        WizText.write(file, getRefSource(ref));
    }

    public static String getRefSource(Ref ref) {
        return getRefSource(ref, true);
    }

    public static String getRefSource(Ref ref, boolean withMemoa) {
        var builder = new StringBuilder();
        builder.append(propsStart).append("\n");
        builder.append("hash-md5").append(propsSeparator).append(stripHash(ref.props.hashMD5)).append("\n");
        builder.append("created-at").append(propsSeparator).append(ref.props.createdAt).append("\n");
        builder.append("memoed-at").append(propsSeparator).append(ref.props.memoedAt).append("\n");
        builder.append("uploaded-at").append(propsSeparator).append(ref.props.uploadedAt).append("\n");
        builder.append("identified-at").append(propsSeparator).append(ref.props.identifiedAt).append("\n");
        builder.append("organized-at").append(propsSeparator).append(ref.props.organizedAt).append("\n");
        builder.append("classified-at").append(propsSeparator).append(ref.props.classifiedAt).append("\n");
        builder.append("done-at").append(propsSeparator).append(ref.props.doneAt).append("\n");
        builder.append("revised-at").append(propsSeparator).append(ref.props.revisedAt).append("\n");
        builder.append("revised-count").append(propsSeparator).append(ref.props.revisedCount).append("\n");
        builder.append(propsEnd).append("\n");
        if (withMemoa && ref.memoa.isPresent()) {
            builder.append(memoaStart).append("\n");
            builder.append(ref.memoa.text).append("\n");
            builder.append(memoaEnd).append("\n");
        }
        for (var index = 0; index < ref.groups.size(); index++) {
            var group = ref.groups.get(index);
            if (group.isPresent()) {
                builder.append(getGroupStart(index + 1)).append("\n");
                writeGroup(group, builder);
                builder.append(groupEnd).append("\n");
            }
        }
        return builder.toString();
    }

    private static String stripHash(String ref) {
        if (ref == null) {
            return "";
        }
        if (ref.startsWith("&")) {
            ref = ref.substring(1);
        }
        return ref.trim();
    }

    private static String dressHash(String ref) {
        if (ref == null || ref.isBlank()) {
            return "";
        }
        ref = ref.trim();
        if (!ref.startsWith("&")) {
            ref = "& " + ref;
        }
        return ref;
    }

    private static void writeGroup(RefGroup group, StringBuilder builder) {
        builder.append(organizationStart).append("\n");
        builder.append("Ordem: ").append(group.order).append("\n");
        builder.append("Classificação: ").append(group.classification).append("\n");
        builder.append("Hierarquia: ").append(group.hierarchy).append("\n");
        builder.append("Titulação: ").append(group.titration).append("\n");
        builder.append(organizationEnd).append("\n");
        builder.append(topicsStart).append("\n");
        builder.append(group.topics).append("\n");
        builder.append(topicsEnd).append("\n");
        builder.append(realizationStart).append("\n");
        builder.append("Cartões em: ").append(group.cardsAt).append("\n");
        builder.append("Questões em: ").append(group.questsAt).append("\n");
        builder.append("Redações em: ").append(group.explainsAt).append("\n");
        builder.append("Didática em: ").append(group.didacticAt).append("\n");
        builder.append(realizationEnd).append("\n");
    }

    private static boolean sourceHasMemoa(String source) {
        int posStart = source.indexOf(memoaStart);
        int posEnd = source.indexOf(memoaEnd, posStart);
        if (posStart < 0 || posEnd < 0) {
            return false;
        } else if (posStart > posEnd) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean sourceHasGroup(String source, Integer index) {
        int posStart = source.indexOf(getGroupStart(index));
        int posEnd = source.indexOf(groupEnd, posStart);
        if (posStart < 0 || posEnd < 0) {
            return false;
        } else if (posStart > posEnd) {
            return false;
        } else {
            return true;
        }
    }

    private static String getGroupName(Integer index) {
        return  groupName + WizString.fillAtStart(index.toString(), '0', 2);
    }

    private static String getGroupStart(Integer index) {
        return  groupStart + WizString.fillAtStart(index.toString(), '0', 2);
    }
    
}
