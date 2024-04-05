package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.Locale;

public class VBFolioQueryOperator {

	private boolean valid = false;
	protected boolean EOF = false;
	public static final int INVALID_RECORD = 0;
	public int recordHits = 0;

	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void setEOF(boolean eOF) {
		EOF = eOF;
	}

	public void validate() {
	}
	
	public int getCurrentRecord() {
		return 0;
	}

	public int getCurrentRangeEnd() { return Integer.MAX_VALUE; }

	public boolean isEOF() {
		return EOF;
	}

	public boolean gotoNextRecord() {
		return false;
	}

	public void printTree(int level) {
		Log.i("query", printLevel(level) + "DEF implementation operator");
	}

	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("DEF implementation operator<CR>");
	}

	public void flushRecordHits() {
		while(gotoNextRecord()) {
		}
	}

	protected String printLevel(int level) {
		StringBuilder sb = new StringBuilder();
		printLevel(level, sb);
		return sb.toString();
	}

	protected void printLevel(int level, StringBuilder sb) {
		for(int i = 0; i < level; i++) {
			sb.append("        ");
		}
	}

	public boolean moveToRecord(int rec) {
		int curr = 0;
		
		if (EOF)
			return false;
		
		curr = getCurrentRecord();
		while(curr < rec) {
			if (!gotoNextRecord()) {
				setEOF(true);
				return false;
			}
			curr = getCurrentRecord();
		}
		
		return true;
	}
	
	public short getCurrentProximity() {
		return 0;
	}
	
	public boolean gotoNextProximity() {
		return true;
	}
}
