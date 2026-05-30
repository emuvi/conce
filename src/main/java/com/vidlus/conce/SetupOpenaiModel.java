package com.vidlus.conce;

public enum SetupOpenaiModel {

    GPT4("gpt-4"),
    GPT4Turbo("gpt-4-turbo"),
    GPT35Turbo("gpt-3.5-turbo");

    private final String code;

    SetupOpenaiModel(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}