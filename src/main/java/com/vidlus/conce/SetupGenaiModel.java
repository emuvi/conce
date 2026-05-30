package com.vidlus.conce;

public enum SetupGenaiModel {

    Gemini3Flash("gemini-3-flash-preview"),
    Gemini3Pro("gemini-3-pro-preview"),
    Gemini2Flash("gemini-2.5-flash"),
    Gemini2Pro("gemini-2.5-pro");

    private final String code;

    SetupGenaiModel(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
