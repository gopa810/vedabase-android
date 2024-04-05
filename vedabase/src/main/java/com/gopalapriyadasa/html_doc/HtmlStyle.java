package com.gopalapriyadasa.html_doc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class HtmlStyle extends HtmlNode {

    // node name
    public String Name = "";

    // node styles (placed into 'style' attribute later)
    public HashMap<String,String> Styles = new HashMap<String,String>();


    public HtmlStyle(String str) {
        Name = str;
    }

    public void setStyle(String styleName, String styleVale) {
        Styles.put(styleName, styleVale);
    }

    public void buildString(StringBuilder sb) {
        sb.append(String.format(".%s {\n", Name));
        if (Styles.size() > 0) {
            for (Map.Entry<String,String> entry: Styles.entrySet()) {
                if (entry.getValue().indexOf(' ') > 0) {
                    sb.append(String.format("    %s : \"%s\";\n", entry.getKey(), entry.getValue()));
                } else {
                    sb.append(String.format("    %s : %s;\n", entry.getKey(), entry.getValue()));
                }
            }
        }

        sb.append("}\n");
    }
}
