package com.vidlus.conce;

import com.vidlus.conce.desk.HelperOrganize;

public class ActOrganize implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperOrganize(workRef).setVisible(true);
    }

}
