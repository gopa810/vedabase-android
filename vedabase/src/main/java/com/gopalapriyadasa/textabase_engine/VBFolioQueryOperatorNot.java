package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

public class VBFolioQueryOperatorNot extends VBFolioQueryOperator {

	VBFolioQueryOperator partAnd = null;
	VBFolioQueryOperator partOr = null;

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "NOT operator");
		if (partAnd != null)
			partAnd.printTree(level + 1);
		if (partOr != null)
			partOr.printTree(level + 1);
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("operator NOT       " + recordHits + " hits<CR>");
		if (partAnd != null)
			partAnd.printTreeEx(level + 1, sb);
		if (partOr != null)
			partOr.printTreeEx(level + 1, sb);
	}

	@Override
	public void flushRecordHits() {
		if (partAnd != null)
			partAnd.flushRecordHits();
		if (partOr != null)
			partOr.flushRecordHits();
	}

	@Override
	public void validate() {
		if (isValid())
			return;

		if (partAnd == null) {
			setEOF(true);
			setValid(true);
			return;
		}

		partAnd.validate();
		if (partAnd.isEOF() == true) {
			setValid(true);
			return;
		}
		if (partOr != null) {
			partOr.validate();
			partOr.moveToRecord(partAnd.getCurrentRecord());
			while (partOr.isEOF() == false
					&& partAnd.getCurrentRecord() == partOr.getCurrentRecord()
					&& partAnd.isEOF() == false) {
				partAnd.gotoNextRecord();
				partOr.moveToRecord(partAnd.getCurrentRecord());
			}
		}
		setEOF(partAnd.isEOF());
		setValid(true);
	}

	@Override
	public boolean isEOF() {
		if (partAnd != null)
			return partAnd.isEOF();
		return true;
	}

	@Override
	public int getCurrentRecord() {
		if (partAnd == null)
			return INVALID_RECORD;
		
		return partAnd.getCurrentRecord();
	}

	@Override
	public boolean gotoNextRecord() {

		if (EOF) return false;

		if (partAnd != null) {
			EOF = partAnd.gotoNextRecord();
			setValid(false);
			validate();
		} else {
			EOF = true;
		}

		if (!EOF) recordHits++;
		return EOF;
	}

}
