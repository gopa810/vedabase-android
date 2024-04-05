package com.gopalapriyadasa.textabase_engine;

import android.graphics.Bitmap;
import android.graphics.Paint;

public class FDPartImage extends FDPartSized {

	public static final int IMAGE_AUDIO = 1;
	
	public String imageName = null;
	public Bitmap bitmap = null;
	public int predefinedBitmap = 0;
	public Paint format = new Paint();
	
	public FDPartImage() {
		desiredWidth = 16;
		desiredHeight = 16;
	}

	@Override
	public float getWidth() {
		return desiredWidth;
	}

	@Override
	public float getHeight() {
		return desiredHeight;
	}

	@Override
	public String toString() {
		return " ";
	}
	
}
