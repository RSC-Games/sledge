package com.rsc_games.sledge.parser;

public class MissingTargetException extends RuntimeException {
    String target;

    public MissingTargetException(String message, String target) {
        super(message);
        this.target = target;
    }

    public String getTarget() {
        return this.target;
    }
}
