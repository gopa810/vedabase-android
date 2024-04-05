package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorGetLevelRecords extends VBFolioQueryOperator {

	Folio storage = null;
	int readIndex = 0;
	int levelIndex = -1;
	String simpleTitle = null;
	ArrayList<NSRange> levelRecords = null;
	boolean exactWords = false;
	
	
	public VBFolioQueryOperatorGetLevelRecords() {
		
	}
	
	public VBFolioQueryOperatorGetLevelRecords(Folio aStorage) {
		storage = aStorage;
	}
	
	public VBFolioQueryOperatorGetLevelRecords(Folio aStorage, int aLevel) {
		storage = aStorage;
		levelIndex = aLevel;				
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "LEVEL records (" + (simpleTitle == null ? "NO SIMPLE TEXT" : simpleTitle) + ")");
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level);
		sb.append("group Levels (" + (simpleTitle == null ? "" : simpleTitle) + ")       " + recordHits + " hits<CR>");
	}

	@Override
	public void validate() {
		if (isValid())
			return;
		levelRecords = new ArrayList<NSRange>();
		if (simpleTitle != null) {
			if (exactWords) {
				storage.enumerateLevelRecordsWithSimpleTitle(levelIndex, simpleTitle, levelRecords);
			} else {
				storage.enumerateLevelRecordsLikeSimpleTitle(levelIndex, simpleTitle, levelRecords);
			}
		} else {
			storage.enumerateLevelRecords(levelIndex, levelRecords);
		}
		readIndex = 0;
		if (levelRecords == null || levelRecords.size() == 0) {
			setEOF(true);
		}
		setValid(true);
	}

	@Override
	public int getCurrentRecord() {
		validate();
		if (!isValid() || isEOF())
			return INVALID_RECORD;
		
		if (readIndex < levelRecords.size()) {
			return levelRecords.get(readIndex).getLocation();
		}
		return INVALID_RECORD;
	}

	@Override
	public int getCurrentRangeEnd() {

		if (readIndex >= 0 && levelRecords != null
				&& readIndex < levelRecords.size()) {
			return levelRecords.get(readIndex).getLocationEnd();
		}

		return super.getCurrentRangeEnd();
	}

	@Override
	public boolean gotoNextRecord() {
		if (isEOF())
			return false;
		
		readIndex++;
		if (readIndex >= levelRecords.size()) {
			setEOF(true);
			return false;
		}

		recordHits++;
		return true;
	}

}
