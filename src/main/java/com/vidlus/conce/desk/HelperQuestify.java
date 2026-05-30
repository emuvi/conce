package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.vidlus.conce.AnkiCsvHelper;
import com.vidlus.conce.CKUtils;
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
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DScroll;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.desk.DText;
import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizText;
import br.com.pointel.jarch.mage.WizUtilDate;

public class HelperQuestify extends DFrame {

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
    private final DButton buttonOpen = new DButton("*")
            .onAction(this::buttonOpenActionPerformed);
    private final DButton buttonNamer = new DButton("&")
            .onAction(this::buttonNamerActionPerformed);
    private final DButton buttonDecker = new DButton(">")
            .onAction(this::buttonDeckerActionPerformed);
    
    private final DPane paneAskActs = new DRowPane().insets(2)
        .growNone().put(buttonClear)
        .growNone().put(buttonClearAll)
        .growHorizontal().put(buttonAsk)
        .growNone().put(buttonPaste)
        .growNone().put(buttonBring)
        .growNone().put(buttonWrite)
        .growNone().put(buttonOpen)
        .growNone().put(buttonNamer)
        .growNone().put(buttonDecker);

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

    
    public HelperQuestify(WorkRef workRef) {
        super("Helper Questify");
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
            for (var replace : Setup.getReplacesList(ReplaceAutoOn.OnQuestify)) {
                source = replace.apply(source);
            }
            textAsk.edit().setValue(source);
            var group = workRef.ref.groups.get(index);
            var questsFile = group.getQuestsFile(workRef.baseFolder);
            WizText.write(questsFile, source);
            group.questsAt = WizUtilDate.formatDateMach(new Date());
            workRef.write();
            WizGUI.showNotify("Quests written.", 1);
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
            var questsFile = group.getQuestsFile(workRef.baseFolder);
            var source = "";
            if (questsFile.exists()) {
                source = WizText.read(questsFile);
            }
            textAsk.setValue(source);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonOpenActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to write.");
            }
            var group = workRef.ref.groups.get(index);
            var questsFile = group.getQuestsFile(workRef.baseFolder);
            WizGUI.open(questsFile);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonNamerActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to clear.");
            }
            var group = workRef.ref.groups.get(index);
            var deckName = getDeckName(group);
            WizGUI.putStringOnClipboard(deckName);
            WizGUI.showNotify("Name copied.");
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonDeckerActionPerformed(ActionEvent e) {
        try {
            var index = comboGroup.selectedIndex();
            if (index == -1 || index >= workRef.ref.groups.size()) {
                throw new Exception("Select a group to clear.");
            }
            var group = workRef.ref.groups.get(index);
            var deckName = getDeckName(group);
            var questsFile = group.getQuestsFile(workRef.baseFolder);
            AnkiCsvHelper.setupDeckFromCsv(deckName, questsFile);
            WizGUI.showNotify("Deck created.", 1);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private String getInsertion() {
        var index = comboGroup.selectedIndex();
        if (index == -1 || index >= workRef.ref.groups.size()) {
            return "";
        }
        var group = workRef.ref.groups.get(index);
        return group.topics.trim();
    }

    private void clearGroup(RefGroup group) throws Exception {
        var questsFile = group.getQuestsFile(workRef.baseFolder);
        if (questsFile.exists()) {
            if (!questsFile.delete()) {
                throw new Exception("Failed to delete quest file: " + questsFile.getAbsolutePath());
            }
        }
    }

    private String getDeckName(RefGroup group) {
        return group.classification.trim() + " " + CKUtils.delBrackets(group.titration).trim();
    }

    private class AskThread extends Thread {

        public volatile boolean stop = false;

        public AskThread() {
            super("Asking Identify");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talkWithBase(Steps.Questify.getCommand(getInsertion()));
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
