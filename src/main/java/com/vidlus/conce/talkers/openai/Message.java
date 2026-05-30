package com.vidlus.conce.talkers.openai;

public class Message {

    public String role;
    public String content;

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
    
}