package com.vidlus.conce;

import java.util.Date;

import br.com.pointel.jarch.mage.WizInteger;
import br.com.pointel.jarch.mage.WizUtilDate;

public class ActRevisedAtNow implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        workRef.ref.props.revisedAt = WizUtilDate.formatDateMach(new Date());
        Integer revisedCount = WizInteger.get(workRef.ref.props.revisedCount, 0);
        revisedCount++;
        workRef.ref.props.revisedCount = revisedCount.toString();
        workRef.write();
    }

}
