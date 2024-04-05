package com.gopalapriyadasa.textabase_engine;

public class SearchTreeContext {
	VBHighlightedPhraseSet quotes;
	String wordsDomain;
	boolean exactWords;
	
	
	public VBHighlightedPhraseSet getQuotes() {
		return quotes;
	}
	public void setQuotes(VBHighlightedPhraseSet quotes) {
		this.quotes = quotes;
	}
	public String getWordsDomain() {
		return wordsDomain;
	}
	public void setWordsDomain(String wordsDomain) {
		this.wordsDomain = wordsDomain;
	}
	public boolean isExactWords() {
		return exactWords;
	}
	public void setExactWords(boolean exactWords) {
		this.exactWords = exactWords;
	}
	
	
}
