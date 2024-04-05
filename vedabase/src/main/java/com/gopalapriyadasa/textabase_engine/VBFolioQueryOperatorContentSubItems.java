package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorContentSubItems extends VBFolioQueryOperator {

	@Override
	public void validate() {
		if (isValid())
			return;

		try {
			int rec = source.getCurrentRecord();
			if (source.isEOF()) {
				setEOF(true);
				return;
			}
			storage.enumerateContentItemsForParent(rec, array);
			readIndex = 0;
			setValid(true);
		} catch (Exception e) {
			setValid(false);
			setEOF(true);
		}
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "CONTENT SUBITEMS operator (" + array.size() + " items)");
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("group CONTENT SUBITEMS      " + recordHits + " hits<CR>");
	}

	@Override
	public int getCurrentRecord() {
		validate();
		if (!isValid() || isEOF())
			return INVALID_RECORD;
		
		if (readIndex < array.size()) {
			return array.get(readIndex);
		}
		return INVALID_RECORD;
	}

	@Override
	public boolean gotoNextRecord() {
		validate();
		if (!isValid() || isEOF())
			return false;

		readIndex++;
		if (array.size() <= readIndex) {
			boolean succ = source.gotoNextRecord();
			if (!succ) {
				setEOF(true);
				return false;
			}
			storage.enumerateContentItemsForParent(source.getCurrentRecord(), array);
			readIndex = 0;
		}

		recordHits++;
		return true;
	}

	int readIndex = 0;
	ArrayList<Integer> array = new ArrayList<Integer>();
	Folio storage = null;
	VBFolioQueryOperator source = null;

}
