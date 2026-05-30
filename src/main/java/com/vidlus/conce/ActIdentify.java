package com.vidlus.conce;

import com.vidlus.conce.desk.HelperIdentify;

public class ActIdentify implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperIdentify(workRef).setVisible(true);
    }

}
