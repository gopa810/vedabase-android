package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

import android.graphics.Rect;

public class FDParagraphLine {

	// relative offset X to the left of para
	public float startOffsetX = 0;
	// relative offset Y to the top of paragraph
	public float startOffsetY = 0;
	// total height of line
	public float height = 0;
	// total width of line
	public float width = 0;
	// distance from base line to top of line
	public float topOffset = 0;
	// distance from base line to bottom of line
	public float bottomOffset = 0;
	
	public int orderNo = 0;
	
	public FDParagraph parent = null;
	
	public ArrayList<FDPartBase> parts = new ArrayList<FDPartBase>();
	
	public FDParagraphLine(FDParagraph par) {
		parent = par;
	}
	
	public void mergeBounds(Rect bounds) {
		
		mergeTop(bounds.top);
		mergeBottom(bounds.bottom);
	}
	
	public void mergeTop(float top) {
		if (top > topOffset) {
			topOffset = top;
		}		
	}
	
	public void mergeBottom(float bottom) {
		if (bottom < bottomOffset) {
			bottomOffset = bottom;
		}
	}

}
