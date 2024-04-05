package com.gopalapriyadasa.textabase_engine;

import android.util.SparseArray;

import java.util.Date;
import java.util.HashSet;

/**
 * Created by Gopa702 on 6/23/2016.
 */
public class VBCustomHighlights {
    public SparseArray<VBHighlighterAnchor> anchors = new SparseArray<VBHighlighterAnchor>();
    public int recordId;
    private int thisId;
    private int parentId;

    private String recordPath;
    private String highlightedText;
    private Date createDate;
    private Date modifyDate;

    public VBCustomHighlights() {
        parentId = 0;
        thisId = -1;
        recordId = 0;
        recordPath = "";
        highlightedText = "";
        createDate = new Date();
        modifyDate = new Date();
    }

    public VBHighlighterAnchor safeAnchorForKey(int key) {
        VBHighlighterAnchor anch = anchorForKey(key);
        if (anch == null) {
            anch = new VBHighlighterAnchor();
            anchors.append(key, anch);
        }
        return anch;

    }
    public VBHighlighterAnchor anchorForKey(int key) {
        return anchors.get(key);
    }

    public VBHighlighterAnchor anchorAtIndex(int index) {
        return anchors.get(anchors.keyAt(index));
    }

    public int anchorsCount() {
        return anchors.size();
    }

    // returns HTML codes of color or the highlighted texts in this note
    // if note does not contain highlighted text, then empty set is returned
    public HashSet<String> getHtmlColorCodes() {
        HashSet<String> colors = new HashSet<String>();

        for (int i = 0; i < anchors.size(); i++) {
            VBHighlighterAnchor vba = anchors.valueAt(i);
            vba.getHtmlColorCodes(colors);
        }

        return colors;
    }

    public boolean hasHighlighter() {
        if (getHighlightedText() == null)
            return false;
        return getHighlightedText().length() > 0;
    }

    public String getAnchorText(int key) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d=", key));
        VBHighlighterAnchor anchor = anchors.get(key);
        for(int k = 0; k < anchor.highlighterMap.length; k++) {
            if (k > 0) {
                sb.append(',');
            }
            sb.append(String.format("%d", anchor.highlighterMap[k]));
        }
        return sb.toString();
    }

    public void setAnchorText(String str) {
        String[] partsMain = str.split("=");
        if (partsMain.length == 2) {
            String [] parts = partsMain[1].split(",");
            VBHighlighterAnchor anchor = new VBHighlighterAnchor();
            anchors.put(Integer.parseInt(partsMain[0]), anchor);
            anchor.highlighterMap = new byte[parts.length];
            for(int i= 0; i < parts.length; i++) {
                anchor.highlighterMap[i] = Byte.parseByte(parts[i]);
            }
        }
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

    public String getHighlightedText() {
        return highlightedText;
    }

    public void setHighlightedText(String highlightedText) {
        this.highlightedText = highlightedText;
    }


}
