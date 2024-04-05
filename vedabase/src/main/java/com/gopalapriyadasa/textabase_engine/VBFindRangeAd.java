package com.gopalapriyadasa.textabase_engine;

public class VBFindRangeAd extends VBFindRangeBase {

	public String word = null;
	public String predicate = null;
	public boolean partial = false;
	
	public VBFindRangeAd() {
		
	}
	
	public VBFindRangeAd(NSRange orig) {
		setLocation(orig.location);
		setLocationEnd(orig.locationEnd);
	}
	
	public void setMatch(String s) {
		word = s.replace('%', '*').replace('_', '?');
		predicate = word.replace(".", "(.)").replace("*", "(.*)");
	}
	
	public boolean isMatch(String str) {
		
		return str.matches(predicate);
	}

	public void setWord(String w) {
		word = w;
	}
	
	public String getWord() {
		
		return word;
	}
	


	
}
