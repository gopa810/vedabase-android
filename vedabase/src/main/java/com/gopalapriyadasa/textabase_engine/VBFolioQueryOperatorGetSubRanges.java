package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

public class VBFolioQueryOperatorGetSubRanges extends VBFolioQueryOperator {

	VBFolioQueryOperator source = null;
	Folio storage = null;
	int rangeStart = 0;
	int rangeEnd = 0;
	int position = 1;

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "SUBRANGES operator taking records from:");
		if (source != null) {
			source.printTree(level + 1);
		}
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("group Subranges       " + recordHits + " hits<CR>");
		if (source != null) {
			source.printTreeEx(level + 1, sb);
		}
	}

	@Override
	public void validate() {
		if (isValid())
			return;
		int rec = source.getCurrentRecord();
		if (rec == INVALID_RECORD) {
			setEOF(true);
			return;
		}
		
		rangeStart = rec + 1;
		rangeEnd = source.getCurrentRangeEnd(); //findEndRecord(rec);
		position = rangeStart;
		setValid(true);
	}

	public int findEndRecord(int rec) {
		VBContentNode ci = storage.findRecordPath(rec);
		int retVal = 0;
		if (ci.getNext() == null) {
			while(ci != null && ci.getNext() == null) {
				ci = ci.getParent();
			}
		} else {
			ci = ci.getNext();
		}
		
		if (ci != null) {
			retVal = ci.getRecordId();
		} else {
			retVal = storage.getRecordCount() - 1;
		}
		
		return retVal;
	}

	@Override
	public int getCurrentRecord() {
		validate();
		if (!isValid() || isEOF())
			return INVALID_RECORD;
		return position;
	}

	@Override
	public boolean gotoNextRecord() {
		validate();
		if (!isValid() || isEOF()) {
			return false;
		}
		
		position++;
		if (rangeEnd < position) {
			boolean ret1 = source.gotoNextRecord();
			if (!ret1) {
				setEOF(true);
				return false;
			}
			int rec = source.getCurrentRecord();
			rangeStart = rec + 1;
			rangeEnd = findEndRecord(rec);
			position = rangeStart;
		}

		recordHits++;
		return true;
	}

}
