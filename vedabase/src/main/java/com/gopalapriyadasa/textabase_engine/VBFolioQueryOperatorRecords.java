package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorRecords extends VBFolioQueryOperator {

	ArrayList<Integer> array = new ArrayList<Integer>();
	int readIndex = 0;
	
	@Override
	public int getCurrentRecord() {
		if (readIndex < array.size()) {
			return array.get(readIndex);
		}
		return INVALID_RECORD;
	}

	@Override
	public boolean gotoNextRecord() {
		readIndex++;
		if (readIndex >= array.size()) {
			setEOF(true);
			return false;
		}

		recordHits++;
		return true;
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "RECORDS list (" + (array.size()) + " items)");
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level,sb);
		sb.append("group Records list        " + recordHits + " hits<CR>");
	}

	public void add(int recid) {
		array.add(recid);
	}
	
	public void AddArray(ArrayList<Integer> arr) {
		array.addAll(arr);
	}

}
