package com.vidlus.conce.desk;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

import com.vidlus.conce.Setup;

import br.com.pointel.jarch.desk.DBordPane;
import br.com.pointel.jarch.desk.DButton;
import br.com.pointel.jarch.desk.DLinePane;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DScroll;
import br.com.pointel.jarch.desk.DText;

public class TextEditor extends DBordPane {

    private final DText textEdit = new DText()
            .lineWrap(true).wrapStyleWord(true);
    private final DScroll scrollPane = new DScroll(textEdit);

    private final DButton buttonReplaces = new DButton("#")
            .onAction(this::buttonReplacesActionPerformed);
    private final DButton buttonReplacesAct = new DButton(">")
            .onAction(this::buttonReplacesActActionPerformed);
    private final DButton buttonGroovy = new DButton("$")
            .onAction(this::buttonGroovyActionPerformed);
    private final DPane paneActions = new DLinePane()
            .put(buttonReplaces)
            .put(buttonReplacesAct)
            .put(buttonGroovy);

    public TextEditor() {
        putCenter(scrollPane);
        putSouth(paneActions);
    }

    public TextEditor addButton(DButton button) {
        paneActions.add(button);
        return this;
    }

    public DText edit() {
        return textEdit;
    }

    public String getValue() {
        return textEdit.value();
    }

    public void setValue(String text) {
        textEdit.value(text);
    }

    public void append(String text) {
        textEdit.append(text);
    }

    public TextEditor onFocusGained(Consumer<FocusEvent> consumer) {
        textEdit.onFocusGained(consumer);
        return this;
    }

    public TextEditor onFocusLost(Consumer<FocusEvent> consumer) {
        textEdit.onFocusLost(consumer);
        return this;
    }

    private void buttonReplacesActionPerformed(ActionEvent e) {
        new ReplacesDesk(this).setVisible(true);
    }

    private void buttonReplacesActActionPerformed(ActionEvent e) {
        try {
            String text = getValue();
            for (Replace replace : Setup.readReplacesList()) {
                text = replace.apply(text);
            }
            textEdit.setValue(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buttonGroovyActionPerformed(ActionEvent e) {
        
    }

}
