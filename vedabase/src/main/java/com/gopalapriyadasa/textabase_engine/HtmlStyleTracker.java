package com.gopalapriyadasa.textabase_engine;

import java.util.HashMap;
import java.util.HashSet;

public class HtmlStyleTracker extends HtmlStyle {

	private HashMap<String,String> formatOld = new HashMap<String,String>();
	private HashSet<String> formatChanges = new HashSet<String>();
	
	public boolean hasChanges() {
		return this.styleNameChanged || (formatChanges.size() > 0);
	}
	
	@Override
	public void setValueForKey(String key, String value) {
		super.setValueForKey(key, value);
		formatChanges.add(key);
	}

	@Override
	public void clearFormat() {
		super.clearFormat();
		formatOld.clear();
	}

	public void clearChanges() {
		this.styleNameChanged = false;
		formatChanges.clear();
		formatOld.clear();
		formatOld.putAll(format);
	}
	
	public HashSet<String> getFormatChanges() {
		return this.formatChanges;
	}
}
