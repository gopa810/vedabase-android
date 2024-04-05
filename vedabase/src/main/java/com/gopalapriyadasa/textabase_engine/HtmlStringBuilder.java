package com.gopalapriyadasa.textabase_engine;

public class HtmlStringBuilder {

	private StringBuilder buffer = new StringBuilder();
	private boolean acceptText = false;
	
	public final boolean isAcceptText() {
		return acceptText;
	}
	public final void setAcceptText(boolean acceptText) {
		this.acceptText = acceptText;
	}
	
	public void clear() {
		buffer.delete(0, buffer.length());
	}
	
	@Override
	public String toString() {
		return buffer.toString();
	}
	
	public boolean setString(String str) {
		
		if (acceptText) {
			clear();
			buffer.append(str);
		}
		return acceptText;
	}
	
	public boolean addCharacter(char chr) {
		if (acceptText) {
			if (chr == '<') {
				buffer.append("&lt;");
			} else if (chr == '>') {
				buffer.append("&gt;");
			} else if (chr == '&') {
				buffer.append("&amp;");
			} else if (chr < 128) {
				buffer.append(chr);
			} else {
				buffer.append(String.format("&#%d;", (int)chr));
			}
		}
		return acceptText;
	}
	
	public boolean appendString(String str) {
		if (acceptText) {
			buffer.append(str);
		}
		return acceptText;
	}
	
	public void insertString(String str, int offset) {
		buffer.insert(offset, str);
	}
	
	public int findTag(String tag) {
		
		int pos = buffer.indexOf("<" + tag + " ");
		if (pos < 0) {
			pos = buffer.indexOf("<" + tag + ">");
		}
		return pos;
	}
}
