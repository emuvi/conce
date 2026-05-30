package com.vidlus.conce;

import com.vidlus.conce.desk.HelperQuestify;

public class ActQuestify implements Act {

    @Override
    public void execute(WorkRef workRef) throws Exception {
        new HelperQuestify(workRef).setVisible(true);
    }

}
