package com.vidlus.conce;

import com.vidlus.conce.RefBaseGCloud;

public enum SetupRefBaseKind {

    FTP(RefBaseFTP.class),
    GCloud(RefBaseGCloud.class);

    private final Class<? extends RefBase> refBaseClass;

    SetupRefBaseKind(Class<? extends RefBase> refBaseClass) {
        this.refBaseClass = refBaseClass;
    }

    public Class<? extends RefBase> getRefBaseClass() {
        return refBaseClass;
    }
    
}
