package com.vidlus.conce.talkers.gemini;

import java.util.List;

public class Content {

    public List<Part> parts;
    public String role;

    public Content(List<Part> parts, String role) {
        this.parts = parts;
        this.role = role;
    }

}
