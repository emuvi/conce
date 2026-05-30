package com.vidlus.conce;

import com.vidlus.conce.desk.HelperAtomize;

public class ActAtomize implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperAtomize(workRef).setVisible(true);
    }

}
