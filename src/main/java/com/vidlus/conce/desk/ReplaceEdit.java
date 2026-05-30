package com.vidlus.conce.desk;

import br.com.pointel.jarch.desk.DCheckEdit;
import br.com.pointel.jarch.desk.DColPane;
import br.com.pointel.jarch.desk.DComboEdit;
import br.com.pointel.jarch.desk.DEdit;
import br.com.pointel.jarch.desk.DFieldEdit;
import br.com.pointel.jarch.desk.DLabelEdit;
import br.com.pointel.jarch.desk.DPane;
import br.com.pointel.jarch.desk.DRowPane;
import br.com.pointel.jarch.desk.DStringField;

public class ReplaceEdit extends DEdit<Replace> {

    private DCheckEdit fieldActive = new DCheckEdit().text("Active");
    private DFieldEdit<String> fieldName = new DStringField();
    private DCheckEdit fieldRegex = new DCheckEdit().text("Regex");
    private DComboEdit<ReplaceAutoOn> fieldAutoOn = new DComboEdit<>(ReplaceAutoOn.class);
    private DPane paneOptions = new DRowPane().insets(3)
            .growNone().put(fieldActive)
            .growHorizontal().put(fieldName)
            .growNone().put(fieldRegex)
            .growNone().put(fieldAutoOn);

    private DFieldEdit<String> fieldOf = new DStringField();
    private DLabelEdit<String> titledOf = new DLabelEdit<>("Of:", fieldOf);
    private DFieldEdit<String> fieldTo = new DStringField();
    private DLabelEdit<String> titledTo = new DLabelEdit<>("To:", fieldTo);
    private DPane paneReplace = new DRowPane().insets(3)
            .growHorizontal().put(titledOf)
            .growHorizontal().put(titledTo);

    private DPane paneBody = new DColPane()
            .growHorizontal().put(paneOptions)
            .growHorizontal().put(paneReplace);

    public ReplaceEdit() {
        comp(paneBody);
    }

    @Override
    public Replace getValue() {
        return new Replace(fieldActive.getValue(), fieldName.getValue(),
                fieldRegex.getValue(), fieldAutoOn.getValue(),
                fieldOf.getValue(), fieldTo.getValue());
    }

    @Override
    public void setValue(Replace value) {
        fieldActive.setValue(value.active);
        fieldName.setValue(value.name);
        fieldRegex.setValue(value.regex);
        fieldAutoOn.setValue(value.autoOn);
        fieldOf.setValue(value.of);
        fieldTo.setValue(value.to);
    }

}
