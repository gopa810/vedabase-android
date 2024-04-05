package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class FDTableRow {

	public ArrayList<FDTableCell> cells = new ArrayList<FDTableCell>();
	
	public FDTableRow() {
		
	}
	
	public void addCell(FDTableCell cell) {
		cells.add(cell);
	}
}
