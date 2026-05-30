package com.vidlus.conce;

import br.com.pointel.jarch.mage.WizCMD;
import br.com.pointel.jarch.mage.WizFile;
import br.com.pointel.jarch.mage.WizGUI;

public class SounderBalcon implements Sounder {

    @Override
    public void sound(WorkRef workRef, RefGroup group) throws Exception {
        var origin = workRef.workFile().getAbsolutePath();
        var destiny = WizFile.changeExtension(workRef.workFile().getAbsolutePath(), ".mp3");
        var command = new String[] {
            "balcon",
            "-n", Setup.getBalconVoice(),
            "-f", origin,
            "-w", destiny
        };
        WizCMD.run(command);
        var titrationFile = group.getTitrationFile(workRef.baseFolder);
        CKUtils.putMarkDownLink(titrationFile, destiny);
        WizGUI.showNotify("Sound inserted.", 1);
    }

}
