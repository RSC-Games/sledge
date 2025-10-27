package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.parser.Token;

class ConditionLiteral {
    public ConditionLiteral(ArrayList<Token> tokenStream) {
        System.out.println("processing tokens into condition");
        System.out.println("got args " + tokenStream);

        // TODO: My friend we have a full conditional parser to write.

        // TODO: Parse down a binary tree of boolean operators, such as &&/||/==, with operator
        // precedence.
    }
}
