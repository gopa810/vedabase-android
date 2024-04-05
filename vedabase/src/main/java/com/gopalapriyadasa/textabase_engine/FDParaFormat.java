package com.gopalapriyadasa.textabase_engine;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.util.SparseArray;

@SuppressLint("DefaultLocale")
public class FDParaFormat {

	public static final int ALIGN_JUST = 0;
	public static final int ALIGN_LEFT = 1;
	public static final int ALIGN_CENTER = 2;
	public static final int ALIGN_RIGHT = 3;
	
	// 0 - justified, 1 - left, 2 - center, 3 - right
	public int align = 0;
	
	
	public static final int SIDE_ALL = 0;
	public static final int SIDE_LEFTRIGHT = 1;
	public static final int SIDE_TOPBOTTOM = 2;
	public static final int SIDE_LEFT = 3;
	public static final int SIDE_TOP = 4;
	public static final int SIDE_RIGHT = 5;
	public static final int SIDE_BOTTOM = 6;
	
	// 0-all, 1-left&right, 2-top&bottom, 3-left, 4-top, 5-right, 6-bottom
	public float[] margins = new float[7];
	public int[] borderColor = new int[7];
	public float[] borderWidth = new float[7];
	public float[] padding = new float[7];

	// multiply of default line height 1 1.5 1.25 etc
	public float lineHeight = 1;

	public float firstIndent = 0;

	public int backgroundColor = 0;

	public static HashMap<String,Paint> sharedLines = new HashMap<String,Paint>();
	public static SparseArray<Paint> sharedBackgrounds = new SparseArray<Paint>();
	private static Paint selectionPaint = null;
	private static Paint selectionLinePaint = null;
	
	public FDParaFormat() {
		for(int i = 0; i < 7; i++) {
			borderColor[i] = 0;
			borderWidth[i] = 0;
			padding[i] = 0;
			margins[i] = 0;
		}
	}

	public void copyFrom(FDParaFormat paraFormat) {

		this.align = paraFormat.align;
		this.lineHeight= paraFormat.lineHeight;
		this.firstIndent = paraFormat.firstIndent;
		this.backgroundColor = paraFormat.backgroundColor;
		for(int i = 0; i < 7; i++) {
			this.borderColor[i] = paraFormat.borderColor[i];
			this.borderWidth[i] = paraFormat.borderWidth[i];
			this.padding[i] = paraFormat.padding[i];
			this.margins[i] = paraFormat.margins[i];
		}
	}

	private float getValue(float [] array, int idx1, int idx2, int idx3) {
		if (array[idx1] != 0)
			return array[idx1];
		if (array[idx2] != 0)
			return array[idx2];
		return array[idx3];
	}
	
	private int getIntValue(int [] array, int idx1, int idx2, int idx3) {
		if (array[idx1] != 0)
			return array[idx1];
		if (array[idx2] != 0)
			return array[idx2];
		return array[idx3];
	}
	
	public float getMargin(int side) {
		switch(side) {
		case SIDE_LEFT:
			return getValue(margins, SIDE_LEFT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_RIGHT:
			return getValue(margins, SIDE_RIGHT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_TOP:
			return getValue(margins, SIDE_TOP, SIDE_TOPBOTTOM, SIDE_ALL);
		case SIDE_BOTTOM:
			return getValue(margins, SIDE_BOTTOM, SIDE_TOPBOTTOM, SIDE_ALL);
		}
		return 0;
	}
	
	public float getBorderWidth(int side) {
		switch(side) {
		case SIDE_LEFT:
			return getValue(borderWidth, SIDE_LEFT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_RIGHT:
			return getValue(borderWidth, SIDE_RIGHT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_TOP:
			return getValue(borderWidth, SIDE_TOP, SIDE_TOPBOTTOM, SIDE_ALL);
		case SIDE_BOTTOM:
			return getValue(borderWidth, SIDE_BOTTOM, SIDE_TOPBOTTOM, SIDE_ALL);
		}
		return 0;
	}

	public int getBorderColor(int side) {
		switch(side) {
		case SIDE_LEFT:
			return getIntValue(borderColor, SIDE_LEFT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_RIGHT:
			return getIntValue(borderColor, SIDE_RIGHT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_TOP:
			return getIntValue(borderColor, SIDE_TOP, SIDE_TOPBOTTOM, SIDE_ALL);
		case SIDE_BOTTOM:
			return getIntValue(borderColor, SIDE_BOTTOM, SIDE_TOPBOTTOM, SIDE_ALL);
		}
		return 0;
	}

	public float getPadding(int side) {
		switch(side) {
		case SIDE_LEFT:
			return getValue(padding, SIDE_LEFT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_RIGHT:
			return getValue(padding, SIDE_RIGHT, SIDE_LEFTRIGHT, SIDE_ALL);
		case SIDE_TOP:
			return getValue(padding, SIDE_TOP, SIDE_TOPBOTTOM, SIDE_ALL);
		case SIDE_BOTTOM:
			return getValue(padding, SIDE_BOTTOM, SIDE_TOPBOTTOM, SIDE_ALL);
		}
		return 0;
	}

	public Paint getBackgroundPaint() {
		if (backgroundColor == 0)
			return null;
		return getPaintForBackgroundColor(backgroundColor);
	}

	public static Paint getPaintForBackgroundColor(int color) {
		Paint p;
		p = sharedBackgrounds.get(color);
		if (p == null) {
			p = new Paint();
			p.setStyle(Paint.Style.FILL);
			p.setColor(color);
			sharedBackgrounds.put(color, p);
		}
		return p;
	}
	
	public Paint getLinePaintForBorderSide(int side) {
		float width = getBorderWidth(side);
		int color = getBorderColor(side);
		
		if (width == 0 || color == 0)
			return null;
		
		String key = String.format("%f-%d", width, color);
		if (sharedLines.containsKey(key)) {
			return sharedLines.get(key);
		}
		
		Paint p = new Paint();
		p.setStyle(Paint.Style.STROKE);
		p.setColor(color);
		p.setStrokeWidth(width);
		
		sharedLines.put(key, p);
		
		return p;
	}

	public Paint getSelectionPaint() {
		if (selectionPaint == null) {
			selectionPaint = new Paint();
			selectionPaint.setStyle(Paint.Style.FILL);
			selectionPaint.setColor(0x7f66ccff);
		}
		return selectionPaint;
	}

	public Paint getSelectionLinePaint() {
		if (selectionLinePaint == null) {
			selectionLinePaint = new Paint();
			selectionLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
			selectionLinePaint.setStrokeWidth(1);
			selectionLinePaint.setColor(0xffcc0066);
		}
		return selectionLinePaint;
	}


}
