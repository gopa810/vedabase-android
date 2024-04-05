package com.gopalapriyadasa.textabase_engine;


import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.PointF;

public class FDRecordPart {

	public FDParaFormat paraFormat = new FDParaFormat();
	public ArrayList<FDPartBase> parts = new ArrayList<FDPartBase>();

	public float absoluteTop = 0;
	public float absoluteBottom = 0;
	public int orderNo = 0;
	public int selected = FDSelection.None;

	public FDRecordPart() {
	}

	/*
	 * this method has to be overridden in subclass
	 */
	public float ValidateForWidth(int width) {
		return 1;
	}

	public float draw(Canvas canvas, FDDrawTextContext context) {
		return context.yStart;
	}

	public void testHit(FDRecordLocation hr, float paddingLeft) {
		
	}

	public void getSelectedText(StringBuilder sb) {
		
	}

	public boolean hasSelection() {
		if (selected != FDSelection.None)
			return true;
		
		for(FDPartBase part : parts) {
			if (part.selected != FDSelection.None)
				return true;
		}
		
		return false;
	}
}
