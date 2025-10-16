package com.rsc_games.sledge.parser;

class TokenBuilder {
    public TokenID id;
    public String data = "";

    public TokenBuilder(TokenID id) {
        this.id = id;
    }

    public void append(String c) {
        this.data += c;
    }
    
    public void replaceLastChar(String c) {
        int lastIndex = this.data.length() - 1;

        this.data = this.data.substring(0, lastIndex) + c;
    }

    public String getLastChar() {
        int lastIndex = this.data.length() - 1;

        if (lastIndex < 0)
            return "";

        return this.data.substring(lastIndex);
    }
}
