package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBHighlightedPhrase {

	public boolean resetParaFlag = true;
	public int currentItem = 0;
	public int currentProximity = 1;
	public int proximity = 1;
	public ArrayList<VBFindRangeAd> items = new ArrayList<VBFindRangeAd>();
	
	public void addWord(String str) {
		
		VBFindRangeAd vb = new VBFindRangeAd();
		vb.setMatch(str);
		items.add(vb);
	}
	
	public boolean testWord(String str, int start, int end) {

		if (currentItem >= items.size())
			currentItem = 0;
		if (currentItem >= items.size())
			return false;
		
		VBFindRangeAd pvb = items.get(currentItem);
		// test current word
		boolean result = pvb.isMatch(str);
		currentProximity--;
		if (result)
		{
			pvb.location = start;
			pvb.locationEnd = end;
			currentItem++;
			currentProximity = proximity;
		}
		// test preceeding word
		else if (currentItem > 0)
		{
			pvb = items.get(currentItem - 1);
			result = pvb.isMatch(str);
			if (result)
			{
				pvb.location = start;
				pvb.locationEnd = end;
				currentProximity = proximity;
			}
		}
	    
		if (currentProximity <= 0)
		{
			currentItem = 0;
		}
		return result;
	}
	
	public boolean isLastWord() {
		return currentItem == items.size() && items.size() > 0;
	}
	
	public void reset() {
		currentItem = 0;
	}
	
	public ArrayList<VBFindRangeAd> getRanges() {

		return items;
	}
	
	public int size() {

		return items.size();
	}
	
	public VBFindRangeAd get(int pos) {
		return items.get(pos);
	}
}
