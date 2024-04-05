package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBFindRange extends VBFindRangeBase {

	public ArrayList<VBFindRangeBase> subranges = new ArrayList<VBFindRangeBase>();
	
	
	public VBFindRange(NSRange ran) {
		location = ran.location;
		locationEnd = ran.locationEnd;
	}
	
	public VBFindRange(int start, int end) {
		location = start;
		locationEnd = end;
	}

	public void addEffectiveRange(NSRange eRange) {

		int i = 0;
		for (i = 0; i < subranges.size(); i++) 
	    {
	        VBFindRangeBase rn = subranges.get(i);
	        if (rn.intersectWithRange(eRange.location, eRange.locationEnd))
	            return;
			if (rn.location > eRange.location)
			{
				VBFindRangeBase r = new VBFindRangeBase();
				r.location = eRange.location;
				r.locationEnd = eRange.locationEnd;
				subranges.add(i, r);
				return;
			}
		}
		
		VBFindRange r = new VBFindRange(eRange);
		subranges.add(r);

	}
	
	
}
