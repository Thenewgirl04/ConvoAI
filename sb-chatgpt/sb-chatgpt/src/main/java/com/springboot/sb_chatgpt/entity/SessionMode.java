package com.springboot.sb_chatgpt.entity;

public final class SessionMode {

    public static final String THERAPY = "therapy";
    public static final String DEFAULT = "default";

    private SessionMode() {
    }

    public static String normalize(String mode) {
        return THERAPY.equalsIgnoreCase(mode) ? THERAPY : DEFAULT;
    }
}
