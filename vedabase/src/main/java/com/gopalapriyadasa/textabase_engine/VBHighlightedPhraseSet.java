package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBHighlightedPhraseSet {

	public ArrayList<VBHighlightedPhrase> items = new ArrayList<VBHighlightedPhrase>();
	public ArrayList<VBFindRangeAd> highlightedRanges = new ArrayList<VBFindRangeAd>();
	
	
	public void add(VBHighlightedPhrase arrTemp) {

		items.add(arrTemp);
	}
	
	public void removeAllObjects() {
		items = new ArrayList<VBHighlightedPhrase>();
	}

	public int size() {
		return items.size();
	}
	
	public boolean testWord(String str, NSRange range) {
		
		boolean addedItems = false;
		for(VBHighlightedPhrase phrase : items)
		{
			if (phrase.testWord(str, range.location, range.locationEnd))
			{
				if (phrase.isLastWord())
				{
					int i;
					int length = phrase.size();
					for(i = 0; i < length; i++)
					{
						VBFindRangeAd found = phrase.get(i);
						VBFindRangeAd newRange = new VBFindRangeAd();
						newRange.location = found.location;
						newRange.locationEnd = found.locationEnd;
						newRange.partial = found.partial;
						highlightedRanges.add(newRange);
						addedItems = true;
					}
					phrase.reset();
				}
			}
		}
		return addedItems;

	}
	
	public void clearHighlightedRanges() {
		
		highlightedRanges = new ArrayList<VBFindRangeAd>();
	}

	public void onNewParagraphTag() {
		
		for(VBHighlightedPhrase hp : items) {
			
			if (hp.resetParaFlag == true) {
				hp.reset();
			}
		}
	}
}
