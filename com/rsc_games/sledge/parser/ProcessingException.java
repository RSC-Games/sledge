package com.rsc_games.sledge.parser;

public class ProcessingException extends RuntimeException {
    private int lno;

    public ProcessingException(int lno, String message) {
        super(message);
        this.lno = lno;
    }

    public int getLineNumber() {
        return this.lno;
    }
}
