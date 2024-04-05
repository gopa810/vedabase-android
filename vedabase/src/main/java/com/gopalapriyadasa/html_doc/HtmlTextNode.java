package com.gopalapriyadasa.html_doc;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class HtmlTextNode extends HtmlNode {
    private StringBuilder mBuilder = new StringBuilder();

    public void setText(String str) {
        mBuilder.delete(0, mBuilder.length());
        mBuilder.append(str);
    }

    public void appendText(String str) {
        mBuilder.append(str);
    }

    @Override
    public void buildString(StringBuilder sb) {
        sb.append(mBuilder.toString());
    }
}
