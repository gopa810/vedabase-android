package com.gopalapriyadasa.textabase_engine;

//import android.graphics.Paint;

public class FDPartString extends FDPartSized {

	public FDPaint format = null;
	//public Paint formatSelection = null;
	public String text = null;
	public int backgroundColor = 0;
	
	public FDPartString() {
	}

	@Override
	public float getWidth() {
		if (format != null && text != null) {
			return format.measureText(text);
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
		return text;
	}
}
