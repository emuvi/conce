package com.vidlus.conce;

public interface Sounder {


    public static Sounder get() {
        return  Setup.getSounderKind() == SetupSounderKind.Pool ? new SounderPool() : new SounderBalcon();
    }
    

    public void sound(WorkRef workRef, RefGroup group) throws Exception;

}
