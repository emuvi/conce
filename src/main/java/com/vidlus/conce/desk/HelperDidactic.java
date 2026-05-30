package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.vidlus.conce.CKUtils;
import com.vidlus.conce.ClassDatex;
import com.vidlus.conce.RefGroup;
import com.vidlus.conce.Setup;
import com.vidlus.conce.Steps;
import com.vidlus.conce.WorkRef;

import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DComboEdit;
import br.com.pointel.jarch.desk.DFieldEdit;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DIntegerField;
import br.com.pointel.jarch.desk.DListDesk;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DScroll;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.desk.DText;
import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizString;
import br.com.pointel.jarch.mage.WizText;
import br.com.pointel.jarch.mage.WizUtilDate;

public class HelperDidactic extends DFrame {

    private final DComboEdit<String> comboGroup = new DComboEdit<String>()
            .onAction(this::comboGroupActionPerformed);    
    private final DButton buttonNext = new DButton("Next")
            .onAction(this::buttonNextActionPerformed);
    private final DPane paneGroupActs = new DRowPane().insets(2)
            .growHorizontal().put(comboGroup)
            .growNone().put(buttonNext);

    private final DFieldEdit<Integer> fieldClassOrder = new DIntegerField()
            .cols(4).editable(false).horizontalAlignmentCenter();
    private final DText fieldClassTitle = new DText().editable(false);
    private final DScroll scrollClassTitle = new DScroll(fieldClassTitle);
    private final DSplitter splitterClass = new DSplitter()
            .horizontal().left(fieldClassOrder).right(scrollClassTitle)
            .divider(0.2f)
            .name("splitterClass");

    private final DText textTitration = new DText().editable(false);
    private final DScroll scrollTitration = new DScroll(textTitration);
    private final DText textTopics = new DText().editable(false);
    private final DScroll scrollTopics = new DScroll(textTopics);
    private final DSplitter splitterGroup = new DSplitter()
            .vertical().top(scrollTitration).bottom(scrollTopics)
            .divider(0.5f)
            .name("splitterGroup");

    private final DSplitter splitterClassGroup = new DSplitter()
            .vertical().top(splitterClass).bottom(splitterGroup)
            .divider(0.3f)
            .name("splitterClassGroup");

    private final DPane paneGroup = new DColPane().insets(2)
            .growHorizontal().put(paneGroupActs)
            .growBoth().put(splitterClassGroup);


    private final DButton buttonClear = new DButton("Clear")
            .onAction(this::buttonClearActionPerformed);
    private final DButton buttonClearAll = new DButton("All")
            .onAction(this::buttonClearAllActionPerformed);
    private final DButton buttonAsk = new DButton("Ask")
            .onAction(this::buttonAskActionPerformed);
    private final DButton buttonPaste = new DButton("Ʇ")
            .onAction(this::buttonPasteActionPerformed);
    private final DButton buttonBring = new DButton("<")
            .onAction(this::buttonBringActionPerformed);
    private final DButton buttonWrite = new DButton("Write")
            .onAction(this::buttonWriteActionPerformed);
    private final DButton buttonSound = new DButton(">")
            .onAction(this::buttonSoundActionPerformed);
    
    private final DPane paneAskActs = new DRowPane().insets(2)
        .growNone().put(buttonClear)
        .growNone().put(buttonClearAll)
        .growHorizontal().put(buttonAsk)
        .growNone().put(buttonPaste)
        .growNone().put(buttonBring)
        .growNone().put(buttonWrite)
        .growNone().put(buttonSound);

    private final TextEditor textAsk = new TextEditor();
     
    private final DPane paneAsk = new DColPane().insets(2)
            .growHorizontal().put(paneAskActs)
            .growBoth().put(textAsk);


    private final DSplitter splitterBody = new DSplitter()
            .horizontal().left(paneGroup).right(paneAsk)
            .divider(0.5f)
            .name("splitterBody")
            .borderEmpty(7);


    private final WorkRef workRef;

    
    public HelperDidactic(WorkRef workRef) {
        super("Helper Didactic");
        this.workRef = workRef;
        body(splitterBody);
        comboGroup.clear();
        for (int i = 0; i < workRef.ref.groups.size(); i++) {
            comboGroup.add("Group " + String.format("%02d", i + 1));
        }
    }

    private void comboGroupActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index == -1 || index >= workRef.ref.groups.size()) {
            fieldClassTitle.setValue("");
            textTitration.setValue("");
            textTopics.setValue("");
            textAsk.setValue("");
            return;
        }
        var group = workRef.ref.groups.get(index);
        Integer orderInt = null;
        try {
            orderInt = Integer.parseInt(group.order.trim());
        } catch (Exception ex) {}
        fieldClassOrder.setValue(orderInt);
        fieldClassTitle.setValue(group.classification);
        textTitration.setValue(group.titration);
        textTopics.setValue(group.topics);
        textAsk.setValue("");
        buttonBringActionPerformed(e);
    }

    private void buttonNextActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index < comboGroup.itemsCount() - 1) {
            comboGroup.select(index + 1);
        } else if (comboGroup.itemsCount() > 0) {
            comboGroup.select(0);
        }
    }

    private void buttonClearActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to clear.");
            }
            var group = workRef.ref.groups.get(index);
            clearGroup(group);
            buttonBringActionPerformed(e);
            WizGUI.showNotify("Cleared.");
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonClearAllActionPerformed(ActionEvent e) {
        try {
            if (!WizGUI.showConfirm("Are you sure to clear all?")) {
                return;
            }
            for (var group : workRef.ref.groups) {
                clearGroup(group);
            }
            buttonBringActionPerformed(e);
            WizGUI.showNotify("Cleared All.");
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void clearGroup(RefGroup group) throws Exception {
        var titrationFile = group.getTitrationFile(workRef.baseFolder);
        if (!titrationFile.exists()) {
            return;
        }
        var titrationData = ClassDatex.read(titrationFile);
        var folder = group.getClassificationFolder(workRef.baseFolder);
        for (var link : titrationData.didacticLinks) {
            var didacticFile = new File(folder, link + ".md");
            if (didacticFile.exists()) {
                if (!didacticFile.delete()) {
                    throw new Exception("Failed to delete didactic file: " + didacticFile.getAbsolutePath());
                }
            }
        }
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

    private void buttonWriteActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to write.");
            }
            var source = textAsk.edit().getValue().trim();
            if (source.isBlank()) {
                throw new Exception("Ask for a content to write.");
            }
            for (var replace : Setup.getReplacesList(ReplaceAutoOn.OnDidactic)) {
                source = replace.apply(source);
            }
            textAsk.edit().setValue(source);
            var group = workRef.ref.groups.get(index);
            var title = CKUtils.cleanFileName(WizString.getFirstLine(source)).trim();
            if (title.isBlank()) {
                throw new Exception("The first line of the content must have a title.");
            }
            if (!title.startsWith("♣")) {
                title = "♣ " + title;
            }
            if (!source.contains("*Refs:*")) {
                source = source + "\n\n*Refs:* " + workRef.ref.props.hashMD5;
            }
            var folder = group.getClassificationFolder(workRef.baseFolder);
            var didacticFile = new File(folder, title + ".md");
            WizText.write(didacticFile, source);
            workRef.workFile(didacticFile);
            var titrationFile = group.getTitrationFile(workRef.baseFolder);
            CKUtils.putMarkDownLink(titrationFile, title);
            group.didacticAt = WizUtilDate.formatDateMach(new Date());
            workRef.write();
            WizGUI.showNotify("Didactic written.", 1);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonBringActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to clear.");
            }
            var group = workRef.ref.groups.get(index);            
            var titrationFile = group.getTitrationFile(workRef.baseFolder);
            var titrationData = ClassDatex.read(titrationFile);
            var didacticLinks = CKUtils.filterMarkDownLinks(titrationData.didacticLinks);
            if (didacticLinks.isEmpty()) {
                return;
            }
            var folder = group.getClassificationFolder(workRef.baseFolder);
            if (didacticLinks.size() > 1) {
                var selectLink = new DListDesk<String>("Select a text to bring");
                selectLink.options(didacticLinks);
                selectLink.onSelect(selected -> {
                    try {
                        if (selected == null || selected.isBlank()) {
                            return;
                        }
                        bringDidactic(folder, selected);
                    } catch (Exception ei) {
                        WizGUI.showError(ei);
                    }
                });
                selectLink.setVisible(true);
            } else {
                var link = didacticLinks.get(0);
                bringDidactic(folder, link);
            }
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonSoundActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("There are no group selected.");
            }
            var group = workRef.ref.groups.get(index);
            if (!workRef.hasWorkFile()) {
                throw new Exception("There are no didactic selected.");
            }
            workRef.sounder.sound(workRef, group);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void bringDidactic(File folder, String link) throws Exception {
        var didacticFile = new File(folder, link + ".md");
        if (!didacticFile.exists()) {
            return;
        }
        var source = WizText.read(didacticFile);
        textAsk.setValue(source);
        workRef.workFile(didacticFile);
    }

    private String getInsertion() {
        var index = comboGroup.selectedIndex();
        if (index == -1 || index >= workRef.ref.groups.size()) {
            return "";
        }
        var group = workRef.ref.groups.get(index);
        return group.topics.trim();
    }

    private class AskThread extends Thread {

        public volatile boolean stop = false;

        public AskThread() {
            super("Asking Didactic");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talkWithBase(Steps.Didactic.getCommand(getInsertion()));
                if (stop) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    textAsk.setValue(result);
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

}
