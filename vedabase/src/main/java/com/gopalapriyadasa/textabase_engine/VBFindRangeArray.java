package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

import android.util.Log;

public class VBFindRangeArray {

	ArrayList<VBFindRange> array = new ArrayList<VBFindRange>();
	
	public VBFindRange findRange(int start, int end) {
		for(VBFindRange item : array) {
			if (item.intersectWithRange(start, end))
				return item;
		}
		return null;
	}
	
	public void addRange(int start, int end, NSRange eRange) {
		
		VBFindRange item = new VBFindRange(start, end);
		item.addEffectiveRange(eRange);
		array.add(item);
	}
	
	public int size() {
		return array.size();
	}
	
	public NSRange rangeAtIndex(int i) {
	
		return array.get(i).getRange();
	}
	
	public void insertRange(int start, int end, NSRange eRange) {
		//Log.i("prepareSR", "Inserting range " + start + ", " + end);
		VBFindRange item = findRange(start, end);
		if (item == null)
		{
			addRange(start, end, eRange);
		}
		else {
			item.mergeRange(start, end);
			item.addEffectiveRange(eRange);
		}

	}
	
	public void sortArray() {
		
		ArrayList<VBFindRange> temp = new ArrayList<VBFindRange>();
		temp.addAll(array);
	        array = new ArrayList<VBFindRange>();
		boolean inserted = false;
		
		for(int b = 0; b < temp.size(); b++)
		{
			VBFindRange item = temp.get(b);
			inserted = false;
			for(int a = 0; a < array.size(); a++)
			{
				VBFindRange itemex = array.get(a);
				if (itemex.location > item.location)
				{
					array.add(a, item);
					inserted = true;
					break;
				}
			}
			if (inserted == false)
			{
				array.add(item);
			}
		}

	}
	
	public void applyRange(int nIndex, String src, StringBuilder dest) {
		
		VBFindRange fr = (VBFindRange)array.get(nIndex);
		NSRange range = fr.getRange();
		
		if (range.location > 0) {
//			dest.append(" &nbsp; ....");
			dest.append("  ....");
		}

		int currPos = range.location;
		for(VBFindRangeBase a : fr.subranges)
		{
	        try {
	            NSRange extractRange = NSRange.makeRange(currPos, a.location);
	            if (src.length() < a.location)
	            {
	                Log.i("high", "weird stuff");
	            }
	            String subString = src.substring(extractRange.location, extractRange.locationEnd);
	            dest.append(subString);
	            //dest.append("<span class=\"FoundText\">" + src.substring(a.location, a.locationEnd) + "</span>");
	            dest.append("<CS:\"FoundText\">" + src.substring(a.location, a.locationEnd) + "</CS>");
	            currPos = a.locationEnd;
	        }
	        catch (Exception exception) {
	            Log.i("highlight", "source = " + src);
	        }
	        finally {
	        }
		}
		if (currPos < range.locationEnd)
		{
			int last = src.length();
			if (last > (range.locationEnd))
				last = range.locationEnd;
			dest.append(src.substring(currPos, last));
		}
		
		if (range.locationEnd < src.length()) {
			dest.append("....");
			//dest.append(".... &nbsp; ");
		}
		

	}
}
