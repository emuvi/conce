package com.vidlus.conce.desk;

import java.io.Serializable;

public class Replace implements Serializable {

    public Boolean active;
    public String name;
    public Boolean regex;
    public ReplaceAutoOn autoOn;
    public String of;
    public String to;

    public Replace() {
        this.active = true;
        this.name = "";
        this.regex = false;
        this.autoOn = ReplaceAutoOn.NeverAuto;
        this.of = "";
        this.to = "";
    }

    public Replace(Boolean active, String name, Boolean regex, ReplaceAutoOn autoOn, String of, String to) {
        this.active = active;
        this.name = name;
        this.regex = regex;
        this.autoOn = autoOn;
        this.of = of;
        this.to = to;
    }

    public String apply(String over) {
        if (!Boolean.TRUE.equals(active)) {
            return over;
        }
        if (Boolean.TRUE.equals(regex)) {
            return over.replaceAll(of, to);
        } else {
            return over.replace(of, to);
        }
    }

    @Override
    public String toString() {
        return (Boolean.TRUE.equals(active) ? "Active - " : "Inactive - ") + name + " { Auto: " + String.valueOf(autoOn) + (Boolean.TRUE.equals(regex) ? " } Regex" : " } Literal") + " of: | " + of + " | to: | " + to + " | ";
    }

}
