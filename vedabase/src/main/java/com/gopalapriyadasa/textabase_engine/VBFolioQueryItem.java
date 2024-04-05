package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBFolioQueryItem {

	StringBuilder word;
	String type;
	ArrayList<VBFolioQueryItem> array;

	public VBFolioQueryItem() {
		
	}
	public VBFolioQueryItem(String string, String string2) {
		setType(string);
		if (string2 != null) {
			setWord(string2);
		}
	}
	public String getWord() {
		return this.word.toString().toLowerCase();
	}
	public void setWord(String word) {
		this.word = new StringBuilder(word);  
	}
	public StringBuilder getWordBuilder() {
		return word;
	}
	public void setWordBuilder(StringBuilder word) {
		this.word = word;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ArrayList<VBFolioQueryItem> getArray() {
		return array;
	}
	public void setArray(ArrayList<VBFolioQueryItem> array) {
		this.array = array;
	}
	public Object getOriginalWord() {
		return this.word.toString();
	}
	
}
