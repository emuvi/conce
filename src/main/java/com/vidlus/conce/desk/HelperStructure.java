package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import com.vidlus.conce.Steps;
import com.vidlus.conce.WorkRef;

import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DScroll;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.desk.DText;
import br.com.pointel.jarch.mage.WizFiles;
import br.com.pointel.jarch.mage.WizGUI;

public class HelperStructure extends DFrame {

    private final DButton buttonClear = new DButton("Clear")
            .onAction(this::buttonClearActionPerformed);
    private final DButton buttonClean = new DButton("_")
            .onAction(this::buttonCleanActionPerformed);
    private final DButton buttonAsk = new DButton("Ask")
            .onAction(this::buttonAskActionPerformed);
    private final DButton buttonPaste = new DButton("Ʇ")
            .onAction(this::buttonPasteActionPerformed);
    private final DButton buttonParse = new DButton("Parse")
            .onAction(this::buttonParseActionPerformed);
    
    private final DPane paneAskActs = new DRowPane().insets(2)
        .growNone().put(buttonClear)
        .growNone().put(buttonClean)
        .growHorizontal().put(buttonAsk)
        .growNone().put(buttonPaste)
        .growNone().put(buttonParse);

    private final TextEditor textAsk = new TextEditor();
     
    private final DPane paneAsk = new DColPane().insets(2)
            .growHorizontal().put(paneAskActs)
            .growBoth().put(textAsk);

    
    private final DButton buttonBack = new DButton("<")
            .onAction(this::buttonBackActionPerformed);
    private final DButton buttonBring = new DButton("Bring")
            .onAction(this::buttonBringActionPerformed);
    private final DButton buttonWrite = new DButton("Write")
            .onAction(this::buttonWriteActionPerformed);
    private final DPane paneGroupActs = new DRowPane().insets(2)
            .growNone().put(buttonBack)
            .growNone().put(buttonBring)
            .growNone().put(buttonWrite)
            .growHorizontal().put(new JPanel());

    private final DText textTopics = new DText()
            .editable(false);
    private final DScroll scrollTopics = new DScroll(textTopics);

    private final DPane paneGroup = new DColPane().insets(2)
            .growHorizontal().put(paneGroupActs)
            .growBoth().put(scrollTopics);

    private final DSplitter splitterBody = new DSplitter()
            .horizontal().left(paneAsk).right(paneGroup)
            .divider(0.5f)
            .name("splitterBody")
            .borderEmpty(7);


    private final WorkRef workRef;
    private final Node root = new Node("*");

    
    public HelperStructure(WorkRef workRef) {
        super("Helper Structure");
        this.workRef = workRef;
        body(splitterBody);
        onFirstActivated(e -> buttonBringActionPerformed(null));
    }

    private void buttonClearActionPerformed(ActionEvent e) {
        if (!WizGUI.showConfirm("Are you sure, do you wanna to clear the base?\nThis will delete all information in the base.")) {
            return;
        }
        if (!WizGUI.showConfirm("Are you REALLY sure, do you wanna to clear the base?\nThis will delete all information in the base.")) {
            return;
        }
        if (!WizGUI.showConfirm("Do you REALLY want to reset your base.\nThe references will be moved to the Pool, everything else will be deleted.")) {
            return;
        }
        try {
            var refsFolder = new File(workRef.baseFolder, "+ Refs");
            if (refsFolder.exists()) {
                var poolFolder = new File(workRef.baseFolder, "+ Pool");
                if (!poolFolder.exists()) {
                    poolFolder.mkdirs();
                }
                WizFiles.moveAll(refsFolder, poolFolder);
            }
            for (var inside : workRef.baseFolder.listFiles()) {
                if (inside.isDirectory() && inside.getName().startsWith("- ")) {
                    WizFiles.delete(inside);
                }
            }
            var index = new File(workRef.baseFolder, "index.md");
            index.delete();
            index.createNewFile();
            buttonBringActionPerformed(e);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonCleanActionPerformed(ActionEvent e) {
        textAsk.setValue("");
    }

    private volatile AskThread askThread = null;

    private void buttonAskActionPerformed(ActionEvent e) {
        if (askThread != null) {
            askThread.stop = true;
            askThread = null;
            buttonAsk.setText("Ask");
        } else {
            askThread = new AskThread();
            askThread.start();
            buttonAsk.setText("Asking...");
        }
    }

    private void buttonPasteActionPerformed(ActionEvent e) {
        textAsk.edit().clear();
        textAsk.edit().paste();
    }

    private void buttonParseActionPerformed(ActionEvent e) {
        try {
            var source = textAsk.getValue();
            if (source == null || source.isBlank()) {
                return;
            }
            root.children.clear();
            var lines = source.split("\\n");
            for (var line : lines) {
                line = line.trim();
                if (!line.startsWith("[[") || !line.endsWith("]]")) {
                    continue;
                }
                var content = line.substring(2, line.length() - 2).trim();
                var parts = content.split("\\-");
                var current = root;
                for (var part : parts) {
                    var partName = part.trim();
                    if (partName.isBlank()) {
                        continue;
                    }
                    var nodeName = "- " + partName;
                    Node found = null;
                    for (var child : current.children) {
                        if (child.name.equals(nodeName)) {
                            found = child;
                            break;
                        }
                    }
                    if (found == null) {
                        found = new Node(nodeName);
                        current.add(found);
                    }
                    current = found;
                }
            }
            textTopics.setValue(root.toString());
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonBackActionPerformed(ActionEvent e) {
        var builder = new StringBuilder();
        for (var child : root.children) {
            buildPaths(child, "", builder, 1);
        }
        textAsk.setValue(builder.toString());
    }

    private void buildPaths(Node node, String path, StringBuilder builder, int level) {
        var currentPath = path.isEmpty() ? node.name : path + " " + node.name;
        if (level == 4) {
            builder.append("[[");
            builder.append(currentPath);
            builder.append("]]\n\n");
        } else {
            for (var child : node.children) {
                buildPaths(child, currentPath, builder, level + 1);
            }
        }
    }

    private void buttonBringActionPerformed(ActionEvent e) {
        try {
            root.children.clear();
            buildTree(workRef.baseFolder, root);
            textTopics.setValue(root.toString());
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonWriteActionPerformed(ActionEvent e) {
        try {
            if (root.children.isEmpty()) {
                return;
            }
            for (var child : root.children) {
                createFolders(workRef.baseFolder, child);
            }
            WizGUI.close(this);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void createFolders(File parent, Node node) {
        var folder = new File(parent, node.name);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (var child : node.children) {
            createFolders(folder, child);
        }
    }

    private void buildTree(File folder, Node node) {
        var listInside = folder.listFiles();
        if (listInside == null) {
            return;
        }
        Arrays.sort(listInside);
        for (var inside : listInside) {
            if (inside.isDirectory() && inside.getName().startsWith("- ")) {
                var child = new Node(inside.getName());
                node.add(child);
                buildTree(inside, child);
            }
        }
    }

    private class AskThread extends Thread {

        public volatile boolean stop = false;

        public AskThread() {
            super("Asking Structure");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talk(Steps.Structure.getCommand());
                if (stop) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    var actual = textAsk.getValue();
                    if (actual == null || actual.isBlank()) {
                        actual = "";
                    }
                    if (!actual.isBlank()) {
                        actual += "\n\n";
                    }
                    textAsk.setValue(actual + result);
                    textAsk.edit().selectionStart(0);
                    textAsk.edit().selectionEnd(0);
                });
            } catch (Exception ex) {
                WizGUI.showError(ex);
            } finally {
                if (askThread == this) {
                    askThread = null;
                    SwingUtilities.invokeLater(() -> buttonAsk.setText("Ask"));
                }
            }
        }
    }

    private static class Node {

        public final String name;
        public final List<Node> children = new ArrayList<>();

        public Node(String name) {
            this.name = name;
        }

        public void add(Node child) {
            children.add(child);
        }

        public String toString() {
            var builder = new StringBuilder();
            printTree(this, builder, 0);
            return builder.toString();
        }

    }

    private static void printTree(Node node, StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append("  ");
        }
        builder.append(node.name);
        builder.append("\n");
        for (var child : node.children) {
            printTree(child, builder, level + 1);
        }
    }

}
