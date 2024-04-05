package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorContentItems extends VBFolioQueryOperator {

	int readIndex = 0;
	ArrayList<Integer> array = null;
	String simpleText = null;
	Folio storage = null;
	boolean exactWords = false;

	@Override
	public void validate() {
		if (isValid())
			return;

		try {
			if (exactWords) {
				storage.enumerateContentItemsWithSimpleText(simpleText, array);
			} else {
				storage.enumerateContentItemsLikeSimpleText(simpleText, array);
			}
			readIndex = 0;
			setValid(true);
		} catch (Exception e) {
			setValid(false);
		}
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "CONTENT ITEMS (" + (array != null ? array.size() : "0") + " items)");
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level,sb);
		sb.append("group CONTENT ITEMS (" + (simpleText != null ? simpleText : "") + ")    " + recordHits + " hits<CR>");
	}

	@Override
	public int getCurrentRecord() {
		if (!isValid())
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
		if (!isValid())
			validate();
		if (!isValid() || isEOF())
			return false;

		readIndex++;
		if (array.size() <= readIndex) {
			setEOF(true);
			return false;
		}

		recordHits++;
		return true;
	}

}
