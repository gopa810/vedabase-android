package com.gopalapriyadasa.bhaktivedanta_textabase;

/**
 * Created by Gopa702 on 7/26/2016.
 */
public class TextCommand {


    private String token;
    private String remain;

    public TextCommand(String cmd) {
        token = "";
        remain = cmd;
    }

    public boolean parseTo(String str) {
        int i = remain.indexOf(str);
        if (i >= 0) {
            token = remain.substring(0, i);
            remain = remain.substring(i + str.length());
            return true;
        } else {
            token = remain;
            return false;
        }
    }

    public String getToken() {
        return token;
    }

    public boolean hasToken(String str) {
        return token.equals(str);
    }

    public int getTokenInt() {
        return Integer.parseInt(token);
    }
}
