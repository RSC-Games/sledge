package com.rsc_games.sledge.cli;

public class ArgsParseException extends RuntimeException {
    String flag;

    public ArgsParseException(String message, String flag) {
        super(message);
        this.flag = flag;
    }

    public String getFaultingFlag() {
        return this.flag;
    }
}
