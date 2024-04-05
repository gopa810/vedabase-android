package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class FDTable extends FDRecordPart {

	public boolean closed = false;
	public ArrayList<FDTableRow> rows = new ArrayList<FDTableRow>();
	public ArrayList<Float> columnWidths = new ArrayList<Float>();
	
	public FDTable() {
	}

	public void addRow(FDTableRow row) {
		
		rows.add(row);
		
	}
	
	public FDTableRow getLastRow() {
		
		if (rows.size() == 0)
			return null;
		return rows.get(rows.size() - 1);
	}
	
	public FDTableRow getSafeLastRow() {
		FDTableRow row = getLastRow();
		if (row == null) {
			row = new FDTableRow();
			addRow(row);
		}
		return row;
	}

	public void addCell(FDTableCell cell) {
		
		FDTableRow row = getSafeLastRow();
		if (row != null) {
			row.addCell(cell);
		}
		
	}

	public FDTableCell getLastCell() {
		FDTableRow row = getLastRow();
		FDTableCell cell = null;
		if (row != null) {
			if (row.cells.size() > 0)
				cell = row.cells.get(row.cells.size() - 1);
		}
		return cell;
	}

	public FDTableCell getLastSafeCell() {
		return null;
	}

}
