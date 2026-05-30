package com.vidlus.conce;

import com.vidlus.conce.desk.HelperStructure;

public class ActStructure implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperStructure(workRef).setVisible(true);
    }

}
