package com.vidlus.conce;

import java.util.Date;

import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizUtilDate;

public class ActUpload implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        workRef.refBase.upload(workRef.sourceFile, workRef.refWithExtension);
        workRef.ref.props.uploadedAt = WizUtilDate.formatDateMach(new Date());
        workRef.write();
        WizGUI.showInfo("Upload executed.");
    }

}
