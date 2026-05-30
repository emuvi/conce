package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import com.vidlus.conce.Setup;

import br.com.pointel.jarch.desk.DBordPane;
import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DFrame;
import br.com.pointel.jarch.desk.DLinePane;
import br.com.pointel.jarch.desk.DListEditor;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.mage.WizGUI;

public class ReplacesDesk extends DFrame {

    private final DListEditor<Replace> listEditor = new DListEditor<>(ReplaceEditFrame.class);

    private final DButton buttonReplace = new DButton("Replace")
            .onAction(this::buttonReplaceActionPerformed);
    private final DButton buttonUndo = new DButton("Undo")
            .onAction(this::buttonUndoActionPerformed);
    private final DPane paneActions = new DLinePane()
            .put(buttonReplace)
            .put(buttonUndo);

    private final DPane paneBody = new DBordPane()
            .putCenter(listEditor)
            .putSouth(paneActions)
            .borderEmpty(7);

    private final TextEditor textEditor;
    private final List<String> undoList = new ArrayList<>();

    public ReplacesDesk(TextEditor textEditor) {
        super("Replaces");
        this.textEditor = textEditor;
        listEditor.list().selectionMultiple();
        body(paneBody);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                read();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                write();
            }
        });
    }

    private void read() {
        try {
            listEditor.setValue(Setup.readReplacesList());
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void write() {
        try {
            Setup.writeReplacesList(listEditor.getValue());
        } catch (Exception e) {
            WizGUI.showError(e);
        }
    }

    private void buttonReplaceActionPerformed(ActionEvent e) {
        String text = textEditor.getValue();
        undoList.add(text);
        for (Replace replace : listEditor.list().getSelectedValuesList()) {
            text = replace.apply(text);
        }
        textEditor.setValue(text);
    }

    private void buttonUndoActionPerformed(ActionEvent e) {
        if (undoList.isEmpty()) {
            return;
        }
        String lastText = undoList.remove(undoList.size() - 1);
        textEditor.setValue(lastText);
    }

}
