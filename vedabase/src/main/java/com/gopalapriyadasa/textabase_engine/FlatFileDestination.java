package com.gopalapriyadasa.textabase_engine;

public interface FlatFileDestination {

	/**
	 * 
	 * @param page
	 * @param direction
	 *  1  align top of this to bottom of previous
	 * -1  align bottom of this to top of the next 
	 */
	public void RecordPageLoaded(int page, int direction);
}
