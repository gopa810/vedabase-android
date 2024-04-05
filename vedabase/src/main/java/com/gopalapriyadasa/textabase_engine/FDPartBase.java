package com.gopalapriyadasa.textabase_engine;


public class FDPartBase {

	public boolean hidden = false;
	public int orderNo = 0;
	public int selected = FDSelection.None;
	public FDParagraphLine parentLine = null;
	
	public FDPartBase() {
	}

	public float getWidth() {
		return 0;
	}

}
