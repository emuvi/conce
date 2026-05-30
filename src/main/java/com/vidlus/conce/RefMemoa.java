package com.vidlus.conce;

public class RefMemoa {

    public String text = "";

    public boolean isPresent() {
        return text != null && !text.isBlank();
    }

    public boolean contains(String text) {
        return text != null && this.text.contains(text);
    }

    public RefMemoa append(String text) {
        if (text == null || text.isBlank()) {
            return this;
        }
        text = text.trim();
        if (!isPresent()) {
            this.text = text;
        } else {
            this.text = this.text + "\n\n" + text;
        }
        this.text = this.text.trim();
        return this;
    }

    public RefMemoa appendOnTop(String text) {
        if (text == null || text.isBlank()) {
            return this;
        }
        text = text.trim();
        if (!isPresent()) {
            this.text = text;
        } else {
            this.text = text + "\n\n" + this.text;
        }
        this.text = this.text.trim();
        return this;
    }

}
