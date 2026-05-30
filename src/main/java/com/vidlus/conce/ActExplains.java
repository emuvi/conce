package com.vidlus.conce;

import com.vidlus.conce.desk.HelperExplains;

public class ActExplains implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperExplains(workRef).setVisible(true);
    }

}
