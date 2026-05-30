package com.vidlus.conce;

import com.vidlus.conce.desk.HelperDidactic;

public class ActDidactic implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperDidactic(workRef).setVisible(true);
    }

}
