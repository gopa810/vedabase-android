package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.FDRecordBase;

public interface EndlessTextViewCallback {

	void endlessTextViewLeftAreaClicked(int nRecordId);

	void endlessBookmarksChanged();
	void endlessHighlightsChanged();
	void endlessTextNotesChanged();

	void endlessTextHistoryChanged();

	void endlessTextViewRecordChanged(int nRecordId);
	
	void endlessTextViewLinkActivated(String type, String link);

	void endlessTextViewRecordClicked(FDRecordBase record);

	void endlessTextLoading(boolean flag);
}
