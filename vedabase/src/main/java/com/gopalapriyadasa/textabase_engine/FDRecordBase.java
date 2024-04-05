package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;


public class FDRecordBase {

	public boolean noteIcon = false;
	public int recordId = 0;
	public ArrayList<FDRecordPart> parts = new ArrayList<FDRecordPart>();
	private float calculatedWidth = -1;
	private float calculatedHeight = 1;
	private float calculatedMultiplyFontSize = -1;
	private float calculatedMultiplyLineSize = -1;
	public boolean loading = true;
	public boolean isFullParaLink = false;
	public boolean visited = false;
	public int relatedRecordId = -1;
	
	// 1 - align top of this to bottom of previous
	// -1 = align bottom of this to top of next
	public int requestedAlign = 1;
	
	public static final float loadingRecordHeight = 240;
	
	public FDRecordBase() {
	}

	/*
	 * returns height of this record
	 * makes new calculation only if width changed
	 */
	public float ValidateForWidth(int width) {
		
		if (loading)
			return loadingRecordHeight;
		
		if (calculatedWidth != width || calculatedMultiplyFontSize != FDCharFormat.getMultiplyFontSize()
				|| calculatedMultiplyLineSize != FDCharFormat.getMultiplySpaces()) {
			calculatedHeight = 1;
			
			for(FDRecordPart part : parts) {
				calculatedHeight += part.ValidateForWidth(width);
				//Log.i("drawy", "   sub height = " + calculatedHeight);
			}
			
			calculatedWidth = width;
			calculatedMultiplyFontSize = FDCharFormat.getMultiplyFontSize();
			calculatedMultiplyLineSize = FDCharFormat.getMultiplySpaces();
		}
		return calculatedHeight;
	}

	public void setNoteIcon(boolean b) {
		this.noteIcon = b;
	}

	public void addElement(FDPartBase sp) {

		FDParagraph para = getLastSafeParagraph();
		
		if (para != null) {
			para.parts.add(sp);
			para.layoutWidth = 0;
		}
	}
	
	public void setParaFormatting(FDParaFormat aFormat) {
		FDParagraph para = getLastSafeParagraph();
		
		if (para != null) {
			para.paraFormat.copyFrom(aFormat);
		}
	}

	public FDParagraph getLastSafeParagraph() {

		FDRecordPart part = getCurrentPart();
		FDParagraph para = null;
		
		if (part instanceof FDParagraph) {
			para = (FDParagraph)part;
			
		} else if (part instanceof FDTable) {
			FDTableCell cell = ((FDTable)part).getLastSafeCell();
			if (cell != null) {
				para = cell.getLastSafeParagraph();
			}
		}
		
		return para;
	}
	
	/*
	 * returns last paragraph part in the list.
	 * If not existing, it creates new one.
	 */
	private FDRecordPart getCurrentPart() {
		if (parts.size() == 0) {
			FDRecordPart part = new FDParagraph();
			parts.add(part);
			return part;
		}
		return parts.get(parts.size() - 1);
	}

	public Object getLastPart() {
		if (parts.size() == 0)
			return null;
		return parts.get(parts.size() - 1);
	}

	public boolean testHit(FDRecordLocation hr, float paddingLeft) {

		for(FDRecordPart part : parts) {
			//Log.i("ClickEvent", "hr(x,y): " + hr.x + "," + hr.y);
			//Log.i("ClickEvent", "part(top,bottom): " + part.absoluteTop + "," + part.absoluteBottom);
			if (part.absoluteTop <= hr.y && part.absoluteBottom > hr.y) {
				hr.record = this;
				hr.partNum = part.orderNo;
				
				part.testHit(hr, paddingLeft);
				if (hr.x < paddingLeft) {
					hr.areaType = FDRecordLocation.AREA_LEFT_SIDE;
				}
				hr.path.add(0, part);
				return true;
			}
		}
		return false;
	}

}
