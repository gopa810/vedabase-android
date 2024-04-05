package com.gopalapriyadasa.textabase_engine;

import android.graphics.Paint;

public class FDPartSpace extends FDPartSized {

	public Paint format = null;
	//public Paint formatSelection = null;
	//public float desiredWidth = -1;
	public boolean breakLine = false;
	public boolean tab = false;
	public int backgroundColor = 0;
	
	public FDPartSpace() {
	}

	@Override
	public float getWidth() {
		if (desiredWidth > 0)
			return desiredWidth;
		if (format != null) {
			return format.measureText(" ");
		}
		return super.getWidth();
	}

	@Override
	public float getHeight() {
		if (format != null) {
			return format.getTextSize();
		}
		return super.getHeight();
	}

	@Override
	public String toString() {
		return " ";
	}
}
