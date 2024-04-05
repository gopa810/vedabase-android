package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBContentNode {

	String title;
	String simpletitle;
	int recordId;
	int parentRecordId;
	int level;
	int selected = 0;
	boolean selectedChanged = false;
	boolean childValid = false;

	VBContentNode parent = null;
	VBContentNode child = null;
	VBContentNode next = null;
	Folio folio = null;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSimpletitle() {
		return simpletitle;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
		setSelectedChanged(true);
	}

	public boolean isSelectedChanged() {
		return selectedChanged;
	}

	public void setSelectedChanged(boolean selectedChanged) {
		this.selectedChanged = selectedChanged;
	}

	public boolean isChildValid() {
		return childValid;
	}

	public void setChildValid(boolean childValid) {
		this.childValid = childValid;
	}

	public Folio getFolio() {
		return folio;
	}

	public void setFolio(Folio folio) {
		this.folio = folio;
	}

	public void setChild(VBContentNode child) {
		this.child = child;
	}

	public void setNext(VBContentNode next) {
		this.next = next;
	}

	public void setSimpletitle(String simpletitle) {
		this.simpletitle = simpletitle;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public int getParentRecordId() {
		return parentRecordId;
	}

	public void setParentRecordId(int parentRecordId) {
		this.parentRecordId = parentRecordId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setParent(VBContentNode parent) {
		this.parent = parent;
	}

	
	public VBContentNode findRecordPath(int recId) {
		VBContentNode p = this;
		VBContentNode last = null;
		
		while (p != null) {
			if (p.getRecordId() == recId) {
				return p;
			} else if (p.getRecordId() > recId) {
				return last;
			} else if ((p.getNext() != null && p.getNext().getRecordId() > recId) || p.getNext() == null) {
				if (p.getChild() == null) {
					return p;
				}
				last = p;
				p = p.getChild();
			} else {
				last = p;
				p = p.getNext();
			}
		}
		
		return null;
	}

	public boolean isRecordSelected(int recId) {
		VBContentNode p = this;
		
		while (p != null) {
			if (p.getRecordId() == recId) {
				return p.getSelected() != 0;
			} else if (p.getRecordId() > recId) {
				return false;
			} else if ((p.getNext() != null && p.getNext().getRecordId() > recId) || p.getNext() == null) {
				if (p.isChildValid() == false) {
					return p.getSelected() != 0;
				} else if (p.getSelected() != 2 && p.getRecordId() != 0) {
					return p.getSelected() != 0;
				}
				p = p.getChild();
			} else {
				p = p.getNext();
			}
		}
		
		return false;
	}
	
	
	public VBContentNode getParent() {
		return parent;
	}

	public String getText() {
		return getTitle();
	}

	public VBContentNode findRecord(int parentRecordId2) {
		VBContentNode p = this;
		while( p != null) {
			if (p.getRecordId() == parentRecordId2) {
				return p;
			} else if (p.getRecordId() > parentRecordId2) {
				return null;
			} else if ((p.getNext() != null && p.getNext().getRecordId() > parentRecordId2) || p.getNext() == null) {
				p = p.getChild();
			} else {
				p = p.getNext();
			}
		}
		return null;
	}

	private VBContentNode getChild() {
		if (childValid)
			return child;
		
		ArrayList<VBContentNode> list = folio.findContentItems(recordId);
		if (list.size() > 0) {
			list.get(0).setParent(this);
			for (int i = 1; i < list.size(); i++) {
				list.get(i-1).setNext(list.get(i));
				list.get(i).setParent(this);
			}
			list.get(list.size()-1).setNext(null);
			setChild(list.get(0));
		} else {
			setChild(null);
		}
/*		for(VBContentNode item : list) {
			addChildItem(item);
		}*/
		childValid = true;
		return child;
	}

	public VBContentNode getNext() {
		return next;
	}

	public void addChildItem(VBContentNode row) {
		if (child == null) {
			setChild(row);
			row.setParent(this);
			return;
		}

		VBContentNode p = this.child;
		while(p != null) {
			if (p.getNext() == null) {
				p.setNext(row);
				row.setParent(this);
				break;
			}
			p = p.getNext();
		}
	}

	public VBContentNode findAncestor(int parentRecordId2) {

		VBContentNode item = this;
		while(item.getParent() != null) {
			if (item.getRecordId() == parentRecordId2) {
				return item;
			}
			item = item.getParent();
		}
		return null;
	}

}
