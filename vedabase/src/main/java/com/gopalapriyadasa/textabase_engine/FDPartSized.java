package com.gopalapriyadasa.textabase_engine;

public class FDPartSized extends FDPartBase {

	public FDLink link = null;
	public float desiredWidth = 1;
	public float desiredHeight = 1;
	public float desiredTop = 1;
	public float desiredBottom = 1;
	
	public FDPartSized() {
	}

	@Override
	public float getWidth() {
		return desiredWidth;
	}
	
	public float getHeight() {
		return desiredHeight;
	}
}
