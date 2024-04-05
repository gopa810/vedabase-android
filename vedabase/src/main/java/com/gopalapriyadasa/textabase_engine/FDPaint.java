package com.gopalapriyadasa.textabase_engine;

import android.graphics.Paint;

public class FDPaint extends Paint {

	public float originalFontSize = 14;
	public boolean generalizedFontFace = false;
	public boolean bold = false;
	public boolean italic = false;
	public int bkgColor = 0;
	
	public void setDefaultTypeface() {
		setTypeface(FDTypeface.getDefaultTypeface(bold, italic));
	}
}
