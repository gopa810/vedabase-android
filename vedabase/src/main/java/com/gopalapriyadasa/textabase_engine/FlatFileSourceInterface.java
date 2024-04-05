package com.gopalapriyadasa.textabase_engine;

public interface FlatFileSourceInterface {

	int getRecordCount();

	FDRecordBase getRecord(int recordId, FlatFileDestination dest, int direction);
	
	String getRecordPath(int record);

	VBCustomNotes recordNotesForRecord(int rec);

	boolean canHaveNotes();

	VBCustomHighlights highlightersForRecord(int rec);

	VBCustomNotes safeRecordNotesForRecord(int rec);
	VBCustomHighlights safeHighlightersForRecord(int rec);
}
