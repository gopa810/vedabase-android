package com.gopalapriyadasa.textabase_engine;

import java.util.Date;
import java.util.HashSet;

import android.util.SparseArray;


public class VBCustomNotes {

	public int recordId;
	private int thisId;
	private int parentId;

	private String recordPath;
	private String noteText;
	private Date createDate;
	private Date modifyDate;

	public VBCustomNotes() {
		parentId = 0;
		thisId = -1;
		recordId = 0;
		recordPath = "";
		noteText = "";
		createDate = new Date();
		modifyDate = new Date();
	}

	public boolean hasText() {
		return (getNoteText() != null && (getNoteText().length() > 0));
	}

	public int getThisId() {
		return thisId;
	}

	public void setThisId(int thisId) {
		this.thisId = thisId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}


	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getRecordPath() {
		return recordPath;
	}

	public void setRecordPath(String recordPath) {
		this.recordPath = recordPath;
	}

	public String getNoteText() {
		return noteText;
	}

	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}
}
