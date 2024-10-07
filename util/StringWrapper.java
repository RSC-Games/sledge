package util;

import java.util.ArrayList;

public class StringWrapper {
    String s;

    public StringWrapper(String s) {
        this.s = s;
    }

    // Generates a list of all text between the split token.
    public String[] split(String token) {
        ArrayList<String> out = new ArrayList<String>();
        String temp = s;
        int offset = token.length();

        while (temp.indexOf(token) != -1) {
            int i = temp.indexOf(token);
            String sub = temp.substring(0, i);
            temp = temp.substring(i + offset);
            out.add(sub);
        }
        out.add(temp);

        String[] split = new String[out.size()];
        return out.toArray(split);
    }

    public String join(String[] fragments) {
        String out = "";

        for (int i = 0; i < fragments.length - 1; i++) {
            out += fragments[i] + this.s;
        }
        out += fragments[fragments.length - 1];

        return out;
    }
}
