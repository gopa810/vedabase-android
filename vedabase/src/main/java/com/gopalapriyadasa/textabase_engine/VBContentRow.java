package com.gopalapriyadasa.textabase_engine;

import com.gopalapriyadasa.bhaktivedanta_textabase.R;

public class VBContentRow implements Comparable<VBContentRow> {
	private String mainTitle;
	private String subTitle;
	private long createTime;
	private long modifyTime;
	private int selected;
	private int parent;
	private int type;
	private int record;
	private int nextSibling;
	private String nodeCode;
	private int nodeType;
	private int nodeChildren;
	private boolean checkable;
	private boolean navigable;
	private boolean expandable;
	private Object tag;
	private int ID;
	private int parentID;
	private String action;
	private int iconResourceId;

	private int sortRule = 0;

	public static final int SORT_NAME = 0;
	public static final int SORT_CREATE_DATE = 1;
	public static final int SORT_MODIFY_DATE = 2;

	public static final int TYPE_ITEM = 0;
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_BACK = 2;
	public static final int TYPE_NOTE = 3;
	public static final int TYPE_BOOKMARK = 4;
	public static final int TYPE_HIGHLIGHTER = 5;
	public static final int TYPE_VIEW = 6;
	public static final int TYPE_PLAYLIST = 7;
	public static final int TYPE_NOTICE = 8;
	public static final int TYPE_QUICKLINKS = 9;
	public static final int TYPE_HR = 10;
	public static final int TYPE_TEXT = 11;
	public static final int TYPE_ACTION = 12;
	public static final int TYPE_SPACE = 13;

	public static final int SELECTION_NO  = 0;
	public static final int SELECTION_YES = 1;
	public static final int SELECTION_MAYBE = 2;

	public final int getType() {
		return type;
	}

	public final void setType(int type) {
		this.type = type;
	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}


	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public int getNodeChildren() {
		return nodeChildren;
	}

	public void setNodeChildren(int nodeChildren) {
		this.nodeChildren = nodeChildren;
	}

	public int getNextSibling() {
		return nextSibling;
	}

	public void setNextSibling(int nextSibling) {
		this.nextSibling = nextSibling;
	}

	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	public VBContentRow() {
		baseInit();
	}

	public VBContentRow(int nType) {
		baseInit();
		this.type = nType;
	}

	public VBContentRow(int nType, String main) {
		baseInit();
		setType(nType);
		setMainTitle(main);
	}

	public VBContentRow(int nType, String main, int iconResource, String aaction) {
		baseInit();
		setType(nType);
		setMainTitle(main);
		setAction(aaction);
		setIconResourceId(iconResource);
	}

	public VBContentRow(String main) {
		baseInit();
		setMainTitle(main);
	}
	
	public VBContentRow(String main, String sub) {
		baseInit();
		setMainTitle(main);
		setSubTitle(sub);
	}

	public void baseInit() {
		checkable = true;
		navigable = true;
		selected = SELECTION_NO;
		type = TYPE_ITEM;
		tag = null;
		iconResourceId = -1;
	}

	public String getMainTitle() {
		if (mainTitle == null)
			return "";
		return mainTitle;
	}
	
	public void setMainTitle(String str) {
		mainTitle = str;
	}
	
	public String getSubTitle() {
		return subTitle;
	}
	
	public void setSubTitle(String str) {
		subTitle = str;
	}
	
	public int getSelected() {
		return selected;
	}
	
	public void setSelected(int sel) {
		selected = sel;
	}
	
	public int getParent() {
		return parent;
	}
	
	public void setParent(int par) {
		parent = par;
	}

	public int getImageGoto() {
		if (type == TYPE_ITEM) {
			return R.drawable.hdr_text_fwd;
		}
		
		return R.drawable.hdr_text_fwd;
	}
	public int getImageExpand() {
		if (type == TYPE_ITEM) {
			return R.drawable.cont_expand_folder;
		}
		return R.drawable.cont_expand_folder;
	}

	public boolean isCheckable() {
		return checkable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public boolean isNavigable() {
		return navigable;
	}

	public void setNavigable(boolean navigable) {
		this.navigable = navigable;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getSelectionIcon(int status) {
		if (type == TYPE_ITEM) {
			if (status == SELECTION_NO) {
				return R.drawable.icon_uncheck;
			} else if (status == SELECTION_YES) {
				return R.drawable.icon_check;
			} else if (status == SELECTION_MAYBE) {
				return R.drawable.icon_check_misc;
			}
		} else if (type == TYPE_BOOKMARK) {
			if (record >= 0)
			{
				if (status == SELECTION_NO) {
					return R.drawable.icon_uncheck;
				} else {
					return R.drawable.icon_check;
				}
			} else {
				return R.drawable.cont_bkmk_open;
			}
		} else if (type == TYPE_NOTE) {
			if (record >= 0)
			{
				if (status == SELECTION_NO) {
					return R.drawable.icon_uncheck;
				} else {
					return R.drawable.icon_check;
				}
			} else {
				return R.drawable.cont_note_open;
			}
		}
		
		return R.drawable.cont_folder;
		
	}
	
	public int getAlteredStatus(int status) {
		if (type == TYPE_ITEM) {
			if (status == SELECTION_YES) {
				return SELECTION_NO;
			} else {
				return SELECTION_YES;
			}
		} else if (type == TYPE_BOOKMARK || type == TYPE_NOTE) {
			if (record >= 0) {
				if (status == SELECTION_YES) {
					return SELECTION_NO;
				} else {
					return SELECTION_YES;
				}
			} else {
				return SELECTION_NO;
			}
		} else {
			return SELECTION_NO;
		}
	}

	private int compareIntegers(int a, int b) {
		if (a < b) return -1;
		if (a > b) return +1;
		return 0;
	}

	private int compareLongs(long a, long b) {
		if (a < b) return -1;
		if (a > b) return +1;
		return 0;
	}

	@Override
	public int compareTo(VBContentRow vbContentRow) {
		if (type == VBContentRow.TYPE_ITEM) {
			return compareIntegers(getRecord(), vbContentRow.getRecord());
		} else if (type == VBContentRow.TYPE_HIGHLIGHTER || type == VBContentRow.TYPE_BOOKMARK
				|| type == VBContentRow.TYPE_NOTE) {

			// test if this item is directory
			if (record < 0) {
				// test if another item is directory
				if (vbContentRow.getRecord() < 0) {
					//default sorting for directories is by name
					return mainTitle.compareToIgnoreCase(vbContentRow.getMainTitle());
				} else {
					// directory has precedence
					return -1;
				}
			} else {
				if (vbContentRow.getRecord() < 0) {
					// directory has precedence
					return +1;
				} else {
					// sorting for elementary items is defined in sortRule
					if (sortRule == SORT_NAME) {
						return mainTitle.compareToIgnoreCase(vbContentRow.getMainTitle());
					} else if (sortRule == SORT_CREATE_DATE) {
						return compareLongs(createTime, vbContentRow.getCreateTime());
					} else if (sortRule == SORT_MODIFY_DATE) {
						return compareLongs(modifyTime, vbContentRow.getModifyTime());
					}
				}
			}
		}

		return 0;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getSortRule() {
		return sortRule;
	}

	public void setSortRule(int sortRule) {
		this.sortRule = sortRule;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getIconResourceId() {
		return iconResourceId;
	}

	public void setIconResourceId(int iconResourceId) {
		this.iconResourceId = iconResourceId;
	}
}
