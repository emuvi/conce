package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.vidlus.conce.RefGroup;
import com.vidlus.conce.Setup;
import com.vidlus.conce.Steps;
import com.vidlus.conce.WorkRef;

import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DCheckEdit;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DComboEdit;
import br.com.pointel.jarch.desk.DEdit;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DSplitter;
import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizUtilDate;

public class HelperIdentify extends DFrame {

    private final DButton buttonClear = new DButton("Clear")
            .onAction(this::buttonClearActionPerformed);
    private final DButton buttonAsk = new DButton("Ask")
            .onAction(this::buttonAskActionPerformed);
    private final DButton buttonPaste = new DButton("Ʇ")
            .onAction(this::buttonPasteActionPerformed);
    private final DButton buttonDel = new DButton("-")
            .onAction(this::buttonDelActionPerformed);
    private final DButton buttonJoin = new DButton("~")
            .onAction(this::buttonJoinActionPerformed);
    private final DButton buttonParse = new DButton("Parse")
            .onAction(this::buttonParseActionPerformed);
    private final DButton buttonBring = new DButton("Bring")
            .onAction(this::buttonBringActionPerformed);
    
    private final DPane paneAskActs = new DRowPane().insets(2)
        .growNone().put(buttonClear)
        .growHorizontal().put(buttonAsk)
        .growNone().put(buttonPaste)
        .growNone().put(buttonDel)
        .growNone().put(buttonJoin)
        .growNone().put(buttonParse)
        .growNone().put(buttonBring);

    private final TextEditor textAsk = new TextEditor();
     
    private final DPane paneAsk = new DColPane().insets(2)
            .growHorizontal().put(paneAskActs)
            .growBoth().put(textAsk);

    private final DButton buttonAdd = new DButton("Add")
            .onAction(this::buttonAddActionPerformed);
    private final DButton buttonSet = new DButton("Set")
            .onAction(this::buttonSetActionPerformed);
    private final DComboEdit<String> comboGroup = new DComboEdit<String>()
            .onAction(this::comboGroupActionPerformed);
    private final DEdit<Boolean> checkAutoSave = new DCheckEdit()
            .name("AutoSave");
    private final DButton buttonSave = new DButton("Save")
            .onAction(this::buttonSaveActionPerformed);
    private final DButton buttonWrite = new DButton("Write")
            .onAction(this::buttonWriteActionPerformed);
    private final DPane paneGroupActs = new DRowPane().insets(2)
            .growNone().put(buttonAdd)
            .growNone().put(buttonSet)
            .growHorizontal().put(comboGroup)
            .growNone().put(checkAutoSave)
            .growNone().put(buttonSave)
            .growNone().put(buttonWrite);

    private final TextEditor textTopics = new TextEditor()
            .onFocusLost(this::callSaveOnFocusLost);

    private final DPane paneGroup = new DColPane().insets(2)
            .growHorizontal().put(paneGroupActs)
            .growBoth().put(textTopics);

    private final DSplitter splitterBody = new DSplitter()
            .horizontal().left(paneAsk).right(paneGroup)
            .divider(0.5f)
            .name("splitterBody")
            .borderEmpty(7);


    private final WorkRef workRef;

    
    public HelperIdentify(WorkRef workRef) {
        super("Helper Identify");
        this.workRef = workRef;
        body(splitterBody);
        comboGroup.clear();
        for (int i = 0; i < workRef.ref.groups.size(); i++) {
            comboGroup.add("Group " + String.format("%02d", i + 1));
        }
        onFirstActivated(e -> buttonBringActionPerformed(null));
    }

    private void buttonClearActionPerformed(ActionEvent e) {
        workRef.ref.groups.clear();
        comboGroup.clear();
        comboGroupActionPerformed(e);
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

    private void buttonDelActionPerformed(ActionEvent e) {
        try {
            var text = textAsk.getValue();
            var caret = textAsk.edit().selectionStart();
            if (caret > text.length()) {
                caret = text.length();
            }
            var prevSep = text.lastIndexOf("---", caret);
            var nextSep = text.indexOf("---", caret);
            if (prevSep != -1 && nextSep != -1 && prevSep == nextSep) {
                return;
            }
            var start = prevSep == -1 ? 0 : prevSep + 3;
            var end = nextSep == -1 ? text.length() : nextSep;
            var newText = text.substring(0, start) + text.substring(end);
            newText = newText.replaceAll("(?s)---\\s*---", "\n\n---\n\n");
            newText = newText.replaceAll("(?s)^\\s*---\\s*", "");
            newText = newText.replaceAll("(?s)\\s*---\\s*$", "");
            newText = newText.replaceAll("\n{3,}", "\n\n");
            textAsk.setValue(newText);
            textAsk.edit().selectionStart(start);
            textAsk.edit().selectionEnd(start);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonJoinActionPerformed(ActionEvent e) {
        try {
            var text = textAsk.getValue();
            var caretStart = textAsk.edit().selectionStart();
            var caretEnd = textAsk.edit().selectionEnd();
            if (caretStart > text.length()) caretStart = text.length();
            if (caretEnd > text.length()) caretEnd = text.length();
            if (caretStart == caretEnd) {
                return;
            }
            if (caretStart > caretEnd) {
                var temp = caretStart;
                caretStart = caretEnd;
                caretEnd = temp;
            }
            var selected = text.substring(caretStart, caretEnd);
            selected = selected.replace("---", "");
            var newText = text.substring(0, caretStart) + selected + text.substring(caretEnd);
            newText = newText.replaceAll("(?s)---\\s*---", "\n\n---\n\n");
            newText = newText.replaceAll("(?s)^\\s*---\\s*", "");
            newText = newText.replaceAll("(?s)\\s*---\\s*$", "");
            newText = newText.replaceAll("\n{3,}", "\n\n");
            textAsk.setValue(newText);
            textAsk.edit().selectionStart(caretStart);
            textAsk.edit().selectionEnd(caretStart);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonParseActionPerformed(ActionEvent e) {
        try {
            var source = textAsk.edit().getValue().trim();
            if (source.isBlank()) {
                return;
            }
            for (var replace : Setup.getReplacesList(ReplaceAutoOn.OnIdentify)) {
                source = replace.apply(source);
            }
            textAsk.edit().setValue(source);
            var parts = source.split("\\-\\-\\-");
            if (parts.length == 0) {
                return;
            }
            workRef.ref.groups.clear();
            comboGroup.clear();
            for (int i = 0; i < parts.length; i++) {
                var group = new RefGroup();
                group.topics = parts[i].trim();
                workRef.ref.groups.add(group);
                comboGroup.add("Group " + String.format("%02d", i + 1));
            }
            comboGroup.select(parts.length > 0 ? 0 : -1);
            comboGroupActionPerformed(e);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void buttonBringActionPerformed(ActionEvent e) {
        var builder = new StringBuilder();
        var first = true;
        for (var group : workRef.ref.groups) {
            if (first) {
                first = false;
            } else {
                builder.append("\n\n---\n\n");
            }
            builder.append(group.topics);
        }
        textAsk.setValue(builder.toString());
    }

    private void buttonAddActionPerformed(ActionEvent e) {
        var group = new RefGroup();
        group.topics = textAsk.edit().selectedText().trim();
        workRef.ref.groups.add(group);
        comboGroup.add("Group " + String.format("%02d", workRef.ref.groups.size()));
        comboGroup.select(workRef.ref.groups.size() - 1);
        comboGroupActionPerformed(e);
    }

    private void buttonSetActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index > -1) {
            textTopics.setValue(textAsk.edit().selectedText().trim());
            saveIfAutoSave(e != null ? e.getSource() : null);
        }
    }

    private void comboGroupActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index == -1 || index >= workRef.ref.groups.size()) {
            textTopics.setValue("");
            return;
        }
        var group = workRef.ref.groups.get(index);
        textTopics.setValue(group.topics);
    }

    private void buttonSaveActionPerformed(ActionEvent e) {
        var index = comboGroup.selectedIndex();
        if (index == -1) {
            return;
        }
        var group = workRef.ref.groups.get(index);
        group.topics = textTopics.getValue().trim();
    }

    private void buttonWriteActionPerformed(ActionEvent e) {
        try {
            workRef.ref.props.identifiedAt = WizUtilDate.formatDateMach(new Date());
            workRef.write();
            WizGUI.close(this);
        } catch (Exception ex) {
            WizGUI.showError(ex);
        }
    }

    private void callSaveOnFocusLost(FocusEvent e) {
        saveIfAutoSave(e != null ? e.getSource() : null);
    }

    private void saveIfAutoSave(Object source) {
        if (Boolean.TRUE.equals(checkAutoSave.value())) {
            buttonSaveActionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "AutoSave"));
        }
    }

    private class AskThread extends Thread {

        public volatile boolean stop = false;

        public AskThread() {
            super("Asking Identify");
        }

        @Override
        public void run() {
            try {
                var result = workRef.talkWithBase(Steps.Identify.getCommand());
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
