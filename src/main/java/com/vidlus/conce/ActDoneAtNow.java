package com.vidlus.conce;

import java.util.Date;

import br.com.pointel.jarch.mage.WizUtilDate;

public class ActDoneAtNow implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        workRef.ref.props.doneAt = WizUtilDate.formatDateMach(new Date());
        workRef.write();
    }

}
