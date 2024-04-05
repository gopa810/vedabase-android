package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class FDTableCell {

	public boolean closed = false;
	public ArrayList<FDRecordPart> parts = new ArrayList<FDRecordPart>();

	public FDParagraph getLastSafeParagraph() {
		FDRecordPart part = getLastSafePart();
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

	private FDRecordPart getLastSafePart() {
		if (parts.size() == 0) {
			FDRecordPart part = new FDParagraph();
			parts.add(part);
			return part;
		}
		return parts.get(parts.size() - 1);
	}
	
	
}
