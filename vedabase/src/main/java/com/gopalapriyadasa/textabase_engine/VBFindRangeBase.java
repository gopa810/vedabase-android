package com.gopalapriyadasa.textabase_engine;

public class VBFindRangeBase extends NSRange {

	public boolean intersectWithRange(int start, int end) {
		
		if (start >= locationEnd)
			return false;
		if (end <= location)
			return false;
		
		return true;
	}
	
	public void mergeRange(int start, int end) {
		
		location = (location < start) ? location : start;
		locationEnd = (locationEnd > end) ? locationEnd : end;
	}
	
	public NSRange getRange() {
		if (location < 0) {
			locationEnd = 0;
			location = 0;
		}
		return NSRange.makeRange(getLocation(), getLocationEnd());
	}

}
