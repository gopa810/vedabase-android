package com.gopalapriyadasa.html_doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class HtmlElement extends HtmlNode {

    // Nodes
    public ArrayList<HtmlNode> Nodes = new ArrayList<HtmlNode>();

    // node name
    public String Name = "";

    // node attributes
    public HashMap<String,String> Parameters = new HashMap<String,String>();

    // node styles (placed into 'style' attribute later)
    public HashMap<String,String> Styles = new HashMap<String,String>();

    public HtmlElement() {

    }

    public HtmlElement(String s) {
        Name = s;
    }

    public HtmlElement createNode(String name) {

        HtmlElement node = new HtmlElement();
        node.Name = name;

        Nodes.add(node);

        return node;
    }

    public HtmlNode appendNode(HtmlNode node) {
        Nodes.add(node);
        return node;
    }

    public HtmlTextNode appendText(String text) {
        HtmlTextNode textNode = new HtmlTextNode();

        textNode.setText(text);
        Nodes.add(textNode);

        return textNode;
    }

    public void appendHtml(String text) {
        appendText(text);
    }

    public void setStyle(String styleName, String styleVale) {
        Styles.put(styleName, styleVale);
    }

    public void setParam(String attrKey, String attrValue) {
        Parameters.put(attrKey, attrValue);
    }

    @Override
    public void buildString(StringBuilder sb) {

        boolean isSingle = (closingType == HtmlNodeClosingType.Dynamic && Nodes.isEmpty())
                || closingType == HtmlNodeClosingType.AlwaysSingle;
        sb.append("<");
        sb.append(Name);

        if (Styles.size() > 0) {
            StringBuilder sbstyle = new StringBuilder();
            for (Map.Entry<String,String> entry: Styles.entrySet()) {
                if (entry.getValue().indexOf(' ') > 0) {
                    sbstyle.append(String.format("%s:\"%s\";", entry.getKey(), entry.getValue()));
                } else {
                    sbstyle.append(String.format("%s:%s;", entry.getKey(), entry.getValue()));
                }
            }

            sb.append(" style=\'");
            sb.append(sbstyle.toString());
            sb.append("\'");
        }

        if (Parameters.size() > 0) {
            for (Map.Entry<String,String> entry : Parameters.entrySet()) {
                sb.append(String.format(" %s=\"%s\"", entry.getKey(), entry.getValue()));
            }
        }

        if (isSingle) {
            sb.append(" /");
        }
        sb.append(">\n");


        if (!isSingle) {
            // subnodes
            if (Nodes.size() > 0) {
                for (HtmlNode child : Nodes) {
                    child.buildString(sb);
                }
            }
            sb.append(String.format("</%s>\n", Name));
        }

    }

}
