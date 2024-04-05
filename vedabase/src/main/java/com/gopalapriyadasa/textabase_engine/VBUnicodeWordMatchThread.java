package com.gopalapriyadasa.textabase_engine;

public class VBUnicodeWordMatchThread {

	public static final int  CompareModeNormal = 0;
	public static final int  CompareModeWild = 1;
	public static final int  CompareModeWaitForEnd = 2;
	public static final int  CompareModeRequiredEnd = 3;
	public static final int  CompareModeRequiredStart = 4;
	public static final int  CompareModeWaitStartWord = 5;
	
	public String word;
	public int compareMode = CompareModeNormal;
	public int currIndex = 0;
	public int partIndex = 0;
	public boolean activeThread = true;
	
	public VBUnicodeWordMatchThread() {

	}
	
	public VBUnicodeWordMatchThread(String w) {
		word = w;
	}
	
	public int checkWildCard() {

		if (matchingIsOver())
	        return CompareModeRequiredEnd;
	    
	    char uc = currentChar();
	    
	    if (uc != '*' && uc != '%')
	    {
	        if (currIndex == 0)
	            return CompareModeWaitStartWord;
	        return CompareModeNormal;
	    }
	    
	    while(!matchingIsOver() && (uc == '*' || uc == '%'))
	    {
	        currIndex = currIndex + 1;
	        uc = currentChar();
	    }
	    partIndex = currIndex;
	    
	    if (matchingIsOver())
	        return CompareModeWaitForEnd;
	    
	    return CompareModeWild;

	}
	
	public boolean matchingIsOver() {
	
		return currIndex >= word.length();
	}
	
	public char currentChar() {
		if (currIndex >= word.length())
			return ' ';
		return word.charAt(currIndex);
	}
}
