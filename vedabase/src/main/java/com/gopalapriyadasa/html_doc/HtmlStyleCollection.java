package com.gopalapriyadasa.html_doc;

import java.util.ArrayList;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class HtmlStyleCollection extends HtmlNode {


    ArrayList<HtmlStyle> Styles = new ArrayList<HtmlStyle>();


    public HtmlStyle addStyle(String styleName) {
        HtmlStyle style = new HtmlStyle(styleName);
        Styles.add(style);
        return style;
    }

    public void buildString(StringBuilder sb) {

        for (HtmlStyle style : Styles ) {
            style.buildString(sb);
        }

    }
}
