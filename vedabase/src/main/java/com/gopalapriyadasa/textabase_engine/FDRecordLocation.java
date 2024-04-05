package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class FDRecordLocation {

	public int selectionMarkOriginal = FDSelection.None;
	public int selectionMarkType = FDSelection.None;
	public float hotSpotX = 0;
	public float hotSpotY = 0;
	public float x;
	public float y;
	public static final int AREA_UNDEFINED = 0;
	public static final int AREA_LEFT_SIDE = 1;
	public static final int AREA_PARA = 2;
	
	public int areaType = AREA_UNDEFINED;

	// valid for all areas
	public FDRecordBase record = null;
	
	// valid for AREA_PARA
	public int partNum = 0;
	public int cellNum = 0;
	public FDPartBase cell = null;
	public FDParagraph para = null;
	
	

	// path of objects in hierarchy
	public ArrayList<Object> path = new ArrayList<Object>();
	
	
	public FDRecordLocation clone() {
		FDRecordLocation loc = new FDRecordLocation();
		loc.copyFrom(this);
		return loc;
	}
	
	public void copyFrom(FDRecordLocation loc) {
		this.x = loc.x;
		this.y = loc.y;
		this.areaType = loc.areaType;
		this.record = loc.record;
		this.partNum = loc.partNum;
		this.cellNum = loc.cellNum;
		this.cell = loc.cell;
		this.para = loc.para;
		this.path.clear();
		this.path.addAll(loc.path);
	}
}
