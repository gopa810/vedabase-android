package com.gopalapriyadasa.textabase_engine;

public class VBRecordRange {

	
	public int start;
	public int end;

	public VBRecordRange(int a, int b) {
		start = a;
		end = b;
	}
	
	public boolean isMember(int rec) {
		return ((rec >= start) && (rec < end));
	}
	
}
