package com.gopalapriyadasa.textabase_engine;

import java.util.HashMap;
import java.util.Map.Entry;

public class HtmlStyle {
	protected HashMap<String,String> format = new HashMap<String,String>();
	protected String styleName;
	protected boolean styleNameChanged = false;
	
	public final HashMap<String, String> getFormat() {
		return format;
	}
	
	public final String getStyleName() {
		return styleName;
	}
	public final void setStyleName(String styleName) {
		this.styleNameChanged = true;
		this.styleName = styleName;
	}
	public final boolean isStyleNameChanged() {
		return styleNameChanged;
	}
	
	public String valueForKey(String key) {
		return format.get(key);
	}
	
	public void setValueForKey(String key, String value) {
		format.put(key, value);
	}
	
	public void clearFormat() {
		format.clear();
	}
	
	public void clear() {
		format.clear();
		this.styleName = null;
		this.styleNameChanged = false;
	}
	
	public String htmlTextForTag(String tag) {
		StringBuilder target = new StringBuilder();
		target.append("<");
		target.append(tag);
		if (styleName != null && styleName.length() > 0) {
			target.append(String.format(" class=\"%s\"", styleName));
		}
		
		if (format.size() > 0) {
			target.append(String.format(" style=\"%s\"", styleCssText()));
		}
		target.append(">");
		
		return target.toString();
	}
	
	public String styleCssText() {
		String val;
		StringBuilder str = new StringBuilder();
		
		for(Entry<String,String> entry : format.entrySet()) {
			if (str.length() > 0) {
				str.append(";");
			}
			val = entry.getValue();
			if (val.indexOf(32) < 0 && val.indexOf(9) < 0) {
				str.append(String.format("%s:%s", entry.getKey(), val));
			} else {
				str.append(String.format("%s:'%s'", entry.getKey(), val));
			}
		}
		return str.toString();
	}
}
