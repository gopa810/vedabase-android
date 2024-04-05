package com.gopalapriyadasa.textabase_engine;

import java.util.Date;

public class VBBookmark {

	private String name;
	private int recordId;
	private int parentId;
	private int thisId;
	private Date createDate;
	private Date modifyDate;

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	public VBBookmark() {
		setParentId(0);
		setThisId(-1);
		recordId = 0;
		name = "";
		createDate = new Date();
		setModifyDate(new Date());
	}

	@Override
	public String toString() {
		return this.name;
	}
	@Override
	public boolean equals(Object o) {
		
		if (o instanceof VBBookmark) {
			VBBookmark vb = (VBBookmark)o;
			return (vb.recordId == this.recordId) && (vb.parentId == this.parentId);
		}
		return false;
	}


	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getThisId() {
		return thisId;
	}

	public void setThisId(int thisId) {
		this.thisId = thisId;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
}
