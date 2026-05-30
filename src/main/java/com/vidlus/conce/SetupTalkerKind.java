package com.vidlus.conce;

public enum SetupTalkerKind {

    GCloud(TalkerGCloud.class),
    Genai(TalkerGemini.class), 
    Openai(TalkerOpenai.class), 
    Clipboard(TalkerClipboard.class);

    private final Class<? extends Talker> talkerClass;

    SetupTalkerKind(Class<? extends Talker> talkerClass) {
        this.talkerClass = talkerClass;
    }

    public Class<? extends Talker> getTalkerClass() {
        return talkerClass;
    }
    
}
