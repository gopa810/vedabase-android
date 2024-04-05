package com.gopalapriyadasa.html_doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class HtmlNode {
    
    public enum HtmlNodeClosingType {
        AlwaysPair,
        AlwaysSingle,
        Dynamic
    }
    

    public HtmlNodeClosingType closingType = HtmlNodeClosingType.AlwaysPair;
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(sb);
        return sb.toString();
    }

    public void buildString(StringBuilder sb) {
    }

}
