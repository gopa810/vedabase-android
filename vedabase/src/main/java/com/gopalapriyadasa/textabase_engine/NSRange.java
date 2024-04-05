package com.gopalapriyadasa.textabase_engine;

public class NSRange {

	int location;
	int locationEnd;
	
	public int getLocation() {
		return location;
	}
	public void setLocation(int location) {
		this.location = location;
	}
	public int getLength() {
		return locationEnd - location;
	}
	public void setLength(int length) {
		this.locationEnd = location + length;
	}
	public int getLocationEnd() {
		return locationEnd;
	}
	public void setLocationEnd(int locationEnd) {
		this.locationEnd = locationEnd;
	}
	public static NSRange makeRange(int i, int j) {
		NSRange range = new NSRange();
		range.setLocation(i);
		range.setLocationEnd(j);
		return range;
	}

	// Checks whether
	// position is within range <location,locationEnd>
	// including range boundaries
	public boolean contains(int position) {
		return ((position >= location) && (position <= locationEnd));
	}
}
