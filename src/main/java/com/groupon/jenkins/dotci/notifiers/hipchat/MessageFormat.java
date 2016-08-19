package com.groupon.jenkins.dotci.notifiers.hipchat;

public enum MessageFormat {
    HTML("html"), TEXT("text");

    private final String value;

    MessageFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
