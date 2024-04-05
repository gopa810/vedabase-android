package com.gopalapriyadasa.textabase_engine;

public class SearchResultItem {

	int recordId = -1;
	int pageNo = 0;
	int resultNo = 0;
	String path = null;
	String text = null;
	FDRecordBase record = null;

	public SearchResultItem() {
	}

	public SearchResultItem(int recid, String intext) {
		recordId = recid;
		text = intext;
	}
	public int getRecordId() {
		return recordId;
	}
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getResultNo() {
		return resultNo;
	}
	public void setResultNo(int resultNo) {
		this.resultNo = resultNo;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

}
