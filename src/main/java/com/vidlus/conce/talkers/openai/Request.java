package com.vidlus.conce.talkers.openai;

import java.util.List;

public class Request {

    public String model;
    public List<Message> messages;

    public Request() {}

    public Request(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
    
}