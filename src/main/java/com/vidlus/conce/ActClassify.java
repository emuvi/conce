package com.vidlus.conce;

import com.vidlus.conce.desk.HelperClassify;

public class ActClassify implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperClassify(workRef).setVisible(true);
    }

}
