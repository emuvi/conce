package com.vidlus.conce;

import com.vidlus.conce.desk.Desk;

import br.com.pointel.jarch.flow.App;
import br.com.pointel.jarch.flow.AppGUI;

public class Conce {

    public static void main(String[] args) throws Exception {
        new App(new AppGUI(Desk.class)).start("Conce", args);
    }

}
