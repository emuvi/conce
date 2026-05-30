package com.vidlus.conce;

import java.util.Objects;

import br.com.pointel.jarch.mage.WizGUI;
import br.com.pointel.jarch.mage.WizThread;

public class TalkerClipboard implements Talker {

    @Override
    public String talk(String command, UriMime... attachs) throws Exception {
        var window = WizGUI.getActiveWindow();
        var checkWindow = window != null && window.isVisible();
        WizGUI.putStringOnClipboard(command);
        var errorCount = 0;
        while (true) {
            WizThread.sleep(100);
            if (checkWindow && !window.isVisible()) {
                throw new Exception("Window is not visible.");
            }
            try {
                var clipboard = WizGUI.getStringFromClipboard();
                if (!Objects.equals(clipboard, command)) {
                    if (window != null) {
                        window.toFront();
                        window.requestFocus();
                        window.requestFocusInWindow();
                        window.toFront();
                    }
                    return clipboard;
                }
            } catch (Exception e) {
                errorCount++;
                if (errorCount > 10) {
                    throw e;
                }
            }
        }
    }

}
