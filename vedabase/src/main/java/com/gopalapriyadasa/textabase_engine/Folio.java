package com.gopalapriyadasa.textabase_engine;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.gopalapriyadasa.bhaktivedanta_textabase.MainActivity;
import com.gopalapriyadasa.bhaktivedanta_textabase.R;
import com.gopalapriyadasa.bhaktivedanta_textabase.UIManager;
import com.gopalapriyadasa.html_doc.TagFileStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class Folio {

	private SQLiteDatabase database = null;

	private int bodyFontSize = 14;
	private String bodyFontFamily = "Times";
	private String bodyBackgroundImage = "vbase://assets/ylw_bkg.png";
	private int bodyLineSpacing = 120;
	private int bodyPaddingLeft = 24;
	private int bodyPaddingRight = 24;
	private HashMap<String,String> docinfo = new HashMap<String,String>();
	private SparseArray<ArrayList<VBContentRow>> contentPages = new SparseArray<ArrayList<VBContentRow>>();
    private String databasePath = null;
	
	public static final String URL_STYLE_FONTS = "http://textabase.app/assets/fonts.css";
	public static final String URL_STYLE_SHEET = "http://textabase.app/resources/styles.css";
	public static final String URL_MAIN_JAVASCRIPT = "http://textabase.app/assets/maintext.js";

	public static int RESULTS_IN_PAGE = 25;

	private ArrayList<VBBookmark> p_bookmarks = new ArrayList<VBBookmark>();
    private boolean p_bookmarksModified = false;
	private ArrayList<VBCustomNotes> p_customNotes = new ArrayList<VBCustomNotes>();
    private boolean p_customNotesModified = false;
    private ArrayList<VBCustomHighlights> p_customHighlights = new ArrayList<VBCustomHighlights>();
    private boolean p_customHighlightsModified = false;
	public ArrayList<FolioQueryTemplate> queryTemplates = new ArrayList<FolioQueryTemplate>();
	public ArrayList<FolioQueryInstance> queryHistory = new ArrayList<FolioQueryInstance>();
	private ArrayList<FolioDelegate> delegateList = new ArrayList<FolioDelegate>();

    // unique ID of custom objects
    private int uniqueId = 1;


    public Folio() {

	}

	public Folio(SQLiteDatabase db) {
		database = db;
		readDocinfo();
		FolioReadContentTask task = new FolioReadContentTask();
		task.folio = this;
		task.execute(0);
	}

	public int getBodyFontSize() {
		return bodyFontSize;
	}

	public void setBodyFontSize(int bodyFontSize) {
		this.bodyFontSize = bodyFontSize;
	}

	public String getBodyFontFamily() {
		return bodyFontFamily;
	}

	public void setBodyFontFamily(String bodyFontFamily) {
		this.bodyFontFamily = bodyFontFamily;
	}

	public int getBodyLineSpacing() {
		return bodyLineSpacing;
	}

	public void setBodyLineSpacing(int bodyLineSpacing) {
		this.bodyLineSpacing = bodyLineSpacing;
	}

	/**
	 * Finds all items in the content with given parent record
	 * 
	 * @param page
	 * @return
	 */
	public ArrayList<VBContentRow> findContentItemsByParent(int page) {
		ArrayList<VBContentRow> list = null;
		VBContentRow row;

		list = contentPages.get(page); 
		if (list != null) {
			return list;
		}

		list = new ArrayList<VBContentRow>();
		if (database != null) {
			int selectionStatus = VBContentRow.SELECTION_NO;
		
			Cursor cursor = database.rawQuery("select record, title, level, node_type, node_code, node_children, next_sibling"
					+ " from contents where parent = "
					+ page + " order by record", null);
			int colTitle = cursor.getColumnIndex("title");
			int colRecord = cursor.getColumnIndex("record");
			int colNodeType = cursor.getColumnIndex("node_type");
			int colNodeChildren = cursor.getColumnIndex("node_children");
			int colNodeCode = cursor.getColumnIndex("node_code");
			int colNextSibling = cursor.getColumnIndex("next_sibling");

			while (cursor.moveToNext()) {
				int crecid = cursor.getInt(colRecord);
				row = new VBContentRow(cursor.getString(colTitle));
				row.setParent(page);
				row.setSelected(selectionStatus);
				row.setExpandable(false);
				row.setType(VBContentRow.TYPE_ITEM);
				row.setRecord(crecid);
				row.setNavigable(true);
				row.setNodeType(cursor.getInt(colNodeType));
				row.setNodeChildren(cursor.getInt(colNodeChildren));
				row.setNodeCode(cursor.getString(colNodeCode));
				row.setNextSibling(cursor.getInt(colNextSibling));
				if (row.getNodeType() == 1 || row.getNodeChildren() < 1) {
					row.setAction("show text " + crecid);
				} else {
					row.setAction("load contents " + crecid);
				}
				list.add(row);
			}
			cursor.close();
		}

		contentPages.put(page, list);
		return list;
	}

	//
	// find only first content item for given parentId
	//
	public VBContentRow findFirstContentItemOfParent(int page) {

		List<VBContentRow> list = findContentItemsByParent(page);
		if (list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<VBContentRow> findContentItemsContainingRecord(int recordId) {
        VBContentRow row = null;
        ArrayList<VBContentRow> list = new ArrayList<VBContentRow>();

        if (database != null) {

            Cursor cursor = database.rawQuery("select record, parent, title, level, node_type, node_code, node_children, next_sibling"
                    + " from contents where record <= " + recordId
                    + " and next_sibling > " + recordId + " order by record", null);
            int colTitle = cursor.getColumnIndex("title");
            int colParent = cursor.getColumnIndex("parent");
            int colRecord = cursor.getColumnIndex("record");
            int colNodeType = cursor.getColumnIndex("node_type");
            int colNodeChildren = cursor.getColumnIndex("node_children");
            int colNodeCode = cursor.getColumnIndex("node_code");
            int colNextSibling = cursor.getColumnIndex("next_sibling");

            // in the case this is non-zero page
            // add reference to parent directory

            while (cursor.moveToNext()) {
                row = new VBContentRow(cursor.getString(colTitle));
                row.setParent(cursor.getInt(colParent));
                row.setSelected(VBContentRow.SELECTION_NO);
                row.setExpandable(false);
                row.setType(VBContentRow.TYPE_ITEM);
                row.setRecord(cursor.getInt(colRecord));
                row.setNavigable(true);
                row.setNodeType(cursor.getInt(colNodeType));
                row.setNodeChildren(cursor.getInt(colNodeChildren));
                row.setNodeCode(cursor.getString(colNodeCode));
                row.setNextSibling(cursor.getInt(colNextSibling));

                list.add(row);
            }
            cursor.close();
        }

        return list;
    }

    /*
	 * Determines selection status of given item (page) as it is selected or unselected
	 * in its parent (parent). This is used to apply selection status of parent item to child items
	 * for not-yet-loaded pages.
	 */
	private int getSelectionStatusOfPage(int page, int parent) {
		
		List<VBContentRow> list = findContentItemsByParent(parent);
		for(VBContentRow item : list) {
			if (item.getType() == VBContentRow.TYPE_ITEM) {
				if (item.getRecord() == page) {
					return item.getSelected();
				}
			}
		}

		return VBContentRow.SELECTION_NO;
	}

	public void readDocinfo() {

		if (database != null) {
			String[] columns = new String[] { "name", "valuex", "idx" };
			Cursor cursor = database.query("docinfo", columns, null, null, null, null, null);
			int textColumnIndex = cursor.getColumnIndex("name");
			int parentColumnIndex = cursor.getColumnIndex("valuex");
			//int recordIndex = cursor.getColumnIndex("idx");
			Log.i("dbread", "title column index = " + textColumnIndex);

			while (cursor.moveToNext()) {
				Log.i("dbread", "row " + cursor.getString(textColumnIndex));
				
				docinfo.put(cursor.getString(textColumnIndex), cursor.getString(parentColumnIndex));
			}
			cursor.close();
		}
		
		queryTemplates.add(new FolioQueryTemplate("Lectures", "[Field keywords : $1]"));
		queryTemplates.add(new FolioQueryTemplate("String Search", "\"$1\""));
		queryTemplates.add(new FolioQueryTemplate("Verse Texts Only", "[Headings Verse Section , text] $1"));
		queryTemplates.add(new FolioQueryTemplate("Synonyms Only", "[([Headings Verse Section , synonyms] | [Headings Verse Section , word for word meanings])] $1"));
		queryTemplates.add(new FolioQueryTemplate("Translations Only", "[Headings Verse Section , translation] $1"));
		queryTemplates.add(new FolioQueryTemplate("Purports Only", "[Headings Verse Section , purport] $1"));
	}

	public VBContentRow findContentItemByRecord(int record) {

		VBContentRow row = null;

		int crecid = 0;
		
		if (database != null) {
			String[] columns = new String[] { "title", "parent", "record" };
			Cursor cursor = database.query("contents", columns, "record = "
					+ record, null, null, null, null);
			int textColumnIndex = cursor.getColumnIndex("title");
			int parentColumnIndex = cursor.getColumnIndex("parent");
			int recordIndex = cursor.getColumnIndex("record");
			Log.i("dbread", "title column index = " + textColumnIndex);

			if (cursor.moveToNext()) {
				Log.i("dbread", "row " + cursor.getString(textColumnIndex));
				crecid = cursor.getInt(recordIndex);
				row = new VBContentRow();
				row.setMainTitle(cursor.getString(textColumnIndex));
				row.setParent(cursor.getInt(parentColumnIndex));
				row.setRecord(crecid);
				row.setType(VBContentRow.TYPE_ITEM);
				row.setNavigable(true);
				row.setExpandable(contentItemHasChild(crecid));
			}
			cursor.close();
		}

		return row;
	}
	
	public boolean contentItemHasChild(int record) {
		
		int count = 0;
		if (database != null) {
			Cursor cursor = database.rawQuery("select count(*) from contents where parent = "
					+ record, null);

			cursor.moveToFirst();
			count = cursor.getInt(0);
			Log.i("dbread", "count parent " + record + "row " + count);			
			cursor.close();
		}

		return count > 0;		
	}

	public List<FolioTextRecord> findTextRecords(int startRecord, int count) {

		ArrayList<FolioTextRecord> list = new ArrayList<FolioTextRecord>();

		if (database != null) {
			String[] columns = new String[] { "recid", "plain", "levelname" };
			String condition = String.format(Locale.US,
					"recid >= %d and recid <= %d", startRecord, startRecord
							+ count);
			Cursor cursor = database.query("texts", columns, condition, null,
					null, null, "recid");
			int textColumnIndex = cursor.getColumnIndex("plain");
			int recordIndex = cursor.getColumnIndex("recid");
			int levelIndex = cursor.getColumnIndex("levelname");

			// in the case this is non-zero page
			// add reference to parent directory
			while (cursor.moveToNext()) {
				// Log.i("dbread-text", "row " +
				// cursor.getString(textColumnIndex));
				FolioTextRecord text = new FolioTextRecord();
				text.setLevelName(cursor.getString(levelIndex));
				text.setRecord(cursor.getInt(recordIndex));
				text.setPlainText(cursor.getString(textColumnIndex));
				list.add(text);
			}
			cursor.close();

		}
		return list;
	}

	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	public String dataForRecordRange(int record, int recordEnd) {

		Log.i("lrec", "Loading records " + record + " - " + recordEnd);
		StringBuilder sb = new StringBuilder();

		if (bodyFontSize < 3) {
			bodyFontSize = 14;
		}
		sb.append("<html><head><title>Rec</title>");

		sb.append(String.format(Locale.getDefault(), "<link href=\"%s\" type=text/css rel=stylesheet>\n",
				URL_STYLE_FONTS));
		sb.append(String.format(Locale.getDefault(), "<link href=\"%s\" type=text/css rel=stylesheet>\n",
				URL_STYLE_SHEET));
		sb.append(String.format(Locale.getDefault(), "<script type=\"text/javascript\" src=\"%s\">\n",
				URL_MAIN_JAVASCRIPT));
		sb.append("</script>\n");

		sb.append("<style>\n");
		sb.append("<!--\n");
		sb.append(".FolioFoundText { background-color:#ccccff;}\n-->\n");
		sb.append(String.format(Locale.getDefault(), "TD { font-size: %dpt; font-family:'%s';}\n",
				bodyFontSize, bodyFontFamily));
		sb.append("</style>\n");
		sb.append("</head>\n");
		sb.append(String.format(
				"<body id=\"body\" style='%s' background=\"%s\">\n",
				bodyStyleString(), UIManager.getHtmlHexColor(R.color.general_background)));
		sb.append("<div id='pageBody'>\n");

		List<FolioTextRecord> list = findTextRecords(record, Math.min(recordEnd - record + 1, 2048));
		for (FolioTextRecord item : list) {
			sb.append(item.getHtmlText());
		}

		sb.append("</div\n");
		sb.append("</body></html>");

		return sb.toString();
	}

	private String bodyStyleString() {
		return String
				.format(Locale.US,
						"font-family:%s;font-size:%dpt;line-height:%d%%;padding-left:%dpt;padding-right:%dpt;text-align:justify;",
						bodyFontFamily, bodyFontSize, bodyLineSpacing,
						bodyPaddingLeft, bodyPaddingRight);
	}

	private StringBuilder stylesCache = null;
	
	private HashMap<String,FDTextFormat> stylesMap = new HashMap<String,FDTextFormat>();

	private VBContentNode content = null;

	public void clearStylesCache() {
		stylesCache = null;
		stylesMap.clear();
	}

	public String getStyles() {
		if (stylesCache != null)
			return stylesCache.toString();

		stylesCache = new StringBuilder();

		// stylesCache.append("@font-face\n{\nfont-family:Times;\nsrc: url(vbase://assets/vuTimesPlus.ttf);\n}\n");

		// reading style names
		//
		SparseArray<String> styles = new SparseArray<String>();

		String[] columns = new String[] { "id", "name" };
		// String condition = String.format(Locale.US,
		// "recid >= %d and recid <= %d", startRecord, startRecord + count);
		Cursor cursor = database.query("styles", columns, null, null, null,
				null, "id");
		int iId = cursor.getColumnIndex("id");
		int iName = cursor.getColumnIndex("name");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
			styles.put(cursor.getInt(iId), cursor.getString(iName));
		}
		cursor.close();

		// reading styles details
		//
		columns = new String[] { "styleid", "name", "valuex" };
		cursor = database.query("styles_detail", columns, null, null, null,
				null, "styleid");
		iId = cursor.getColumnIndex("styleid");
		iName = cursor.getColumnIndex("name");
		int iValue = cursor.getColumnIndex("valuex");
		int prevStyle = -1;
		int currStyle = 0;

		while (cursor.moveToNext()) {
			currStyle = cursor.getInt(iId);
			if (prevStyle < 0) {
				stylesCache.append(String.format(".%s {\n",
						styles.get(currStyle)));
			} else if (prevStyle != currStyle) {
				stylesCache.append("}\n");
				stylesCache.append(String.format(".%s {\n",
						styles.get(currStyle)));
			} else {
				String valuex = cursor.getString(iValue);
				if (valuex.indexOf(' ') >= 0)
					stylesCache.append(String.format("   %s:\"%s\";\n",
							cursor.getString(iName), cursor.getString(iValue)));
				else
					stylesCache.append(String.format("   %s:%s;\n",
							cursor.getString(iName), cursor.getString(iValue)));
			}
			prevStyle = currStyle;
		}
		cursor.close();

		if (prevStyle >= 0) {
			stylesCache.append("}\n");
		}

		return stylesCache.toString();
	}

	/*
	 * Retrieve FDTextFormat object for given style name
	 */
	public FDTextFormat getRawStyle(String nameRequested) {
		if (stylesMap != null && stylesMap.size() > 0)
		{
			if (stylesMap.containsKey(nameRequested))
				return stylesMap.get(nameRequested);
			return null;
		}

		stylesMap.clear();

		// reading style names
		//
		SparseArray<FDTextFormat> stylesMapIds = new SparseArray<FDTextFormat>();

		String[] columns = new String[] { "id", "name" };
		Cursor cursor = database.query("styles", columns, null, null, null,
				null, "id");
		int iId = cursor.getColumnIndex("id");
		int iName = cursor.getColumnIndex("name");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
			FDTextFormat style = new FDTextFormat();
			style.name = cursor.getString(iName);
			style.styleId = cursor.getInt(iId);
			stylesMap.put(style.name, style);
			stylesMapIds.put(style.styleId, style);
		}
		cursor.close();

		// reading styles details
		//
		columns = new String[] { "styleid", "name", "valuex" };
		cursor = database.query("styles_detail", columns, null, null, null,
				null, "styleid");
		iId = cursor.getColumnIndex("styleid");
		iName = cursor.getColumnIndex("name");
		int iValue = cursor.getColumnIndex("valuex");
		int currStyle = 0;
		FDTextFormat style = null;
		

		while (cursor.moveToNext()) {
			currStyle = cursor.getInt(iId);
			style = stylesMapIds.get(currStyle);
			String namex = cursor.getString(iName);
			String valuex = cursor.getString(iValue);
			
			if (namex.equals("text-indent")) {
				style.paraFormat.firstIndent = dimensionFromString(valuex);
			} else if (namex.equals("margin-left")) {
				style.paraFormat.margins[FDParaFormat.SIDE_LEFT] = dimensionFromString(valuex);
			} else if (namex.equals("margin-right")) {
				style.paraFormat.margins[FDParaFormat.SIDE_RIGHT] = dimensionFromString(valuex); 
			} else if (namex.equals("margin-top")) {
				style.paraFormat.margins[FDParaFormat.SIDE_TOP] = dimensionFromString(valuex);
			} else if (namex.equals("margin-bottom")) {
				style.paraFormat.margins[FDParaFormat.SIDE_BOTTOM] = dimensionFromString(valuex);
			} else if (namex.equals("padding-left")) {
				style.paraFormat.padding[FDParaFormat.SIDE_LEFT] = dimensionFromString(valuex);
			} else if (namex.equals("padding-right")) {
				style.paraFormat.padding[FDParaFormat.SIDE_RIGHT] = dimensionFromString(valuex); 
			} else if (namex.equals("padding-top")) {
				style.paraFormat.padding[FDParaFormat.SIDE_TOP] = dimensionFromString(valuex);
			} else if (namex.equals("padding-bottom")) {
				style.paraFormat.padding[FDParaFormat.SIDE_BOTTOM] = dimensionFromString(valuex);
			} else if (namex.equals("padding")) {
				style.paraFormat.padding[FDParaFormat.SIDE_ALL] = dimensionFromString(valuex);
			} else if (namex.equals("background-color")) {
				style.paraFormat.backgroundColor = colorFromString(valuex);
				style.textFormat.setBackgroundColor(colorFromString(valuex));
			} else if (namex.equals("border-left-color")) {
				style.paraFormat.borderColor[FDParaFormat.SIDE_LEFT] = colorFromString(valuex);
			} else if (namex.equals("border-right-color")) {
				style.paraFormat.borderColor[FDParaFormat.SIDE_RIGHT] = colorFromString(valuex); 
			} else if (namex.equals("border-top-color")) {
				style.paraFormat.borderColor[FDParaFormat.SIDE_TOP] = colorFromString(valuex);
			} else if (namex.equals("border-bottom-color")) {
				style.paraFormat.borderColor[FDParaFormat.SIDE_BOTTOM] = colorFromString(valuex);
			} else if (namex.equals("border-left-width")) {
				style.paraFormat.borderWidth[FDParaFormat.SIDE_LEFT] = dimensionFromString(valuex);
			} else if (namex.equals("border-right-width")) {
				style.paraFormat.borderWidth[FDParaFormat.SIDE_RIGHT] = dimensionFromString(valuex); 
			} else if (namex.equals("border-top-width")) {
				style.paraFormat.borderWidth[FDParaFormat.SIDE_TOP] = dimensionFromString(valuex);
			} else if (namex.equals("border-bottom-width")) {
				style.paraFormat.borderWidth[FDParaFormat.SIDE_BOTTOM] = dimensionFromString(valuex);
			} else if (namex.equals("border-color")) {
				style.paraFormat.borderColor[FDParaFormat.SIDE_ALL] = colorFromString(valuex);
			} else if (namex.equals("border-width")) {
				style.paraFormat.borderWidth[FDParaFormat.SIDE_ALL] = dimensionFromString(valuex);
			} else if (namex.equals("color")) {
				style.textFormat.setForegroundColor(colorFromString(valuex));
			} else if (namex.equals("font-family")) {
				style.textFormat.setFontName(valuex);
			} else if (namex.equals("font-size")) {
				style.textFormat.setTextSize(dimensionFromString(valuex));
			} else if (namex.equals("font-style")) {
				if (valuex.equals("italic")) {
					style.textFormat.setItalic(true);
				} else if (valuex.equals("normal")) {
					style.textFormat.setItalic(false);
				}
			} else if (namex.equals("font-weight")) {
				if (valuex.equals("bold")) {
					style.textFormat.setBold(true);
				} else if (valuex.equals("normal")) {
					style.textFormat.setBold(false);
				}
			} else if (namex.equals("line-height")) {
				style.paraFormat.lineHeight = dimensionFromString(valuex) / 14;
			} else if (namex.equals("text-align")) {
				if (valuex.equals("center")) {
					style.paraFormat.align = FDParaFormat.ALIGN_CENTER;
				} else if (valuex.equals("right")) {
					style.paraFormat.align = FDParaFormat.ALIGN_RIGHT;
				} else if (valuex.equals("left")) {
					style.paraFormat.align = FDParaFormat.ALIGN_LEFT;
				} else {
					style.paraFormat.align = FDParaFormat.ALIGN_JUST;
				}
			} else if (namex.equals("text-decoration")) {
				if (valuex.equals("line-through")) {
					style.textFormat.setStrikeOut(true);
					style.textFormat.setUnderline(false);
				} else if (valuex.equals("underline")) {
					style.textFormat.setStrikeOut(false);
					style.textFormat.setUnderline(true);
				} else if (valuex.equals("normal")) {
					style.textFormat.setStrikeOut(false);
					style.textFormat.setUnderline(false);
				}
			} else if (namex.equals("visibility")) {
				if (valuex.equals("hidden")) {
					style.textFormat.setHidden(true);
				} else if (valuex.equals("visible")) {
					style.textFormat.setHidden(false);
				} 
			}
			
		}
		cursor.close();

		if (stylesMap.containsKey(nameRequested))
			return stylesMap.get(nameRequested);
		return null;
	}

	private int colorFromString(String valuex) {
		int color = 0;
		if (valuex.startsWith("#")) {
			color = Integer.parseInt(valuex.substring(1), 16) | 0xff000000;
		}
		//Log.i("drawe", "colorFromString(" + valuex + ") = " + color);
		return color;
	}

	private float dimensionFromString(String value) {
		
		boolean isPt = false;
		boolean isPerc = false;
		double d = 0.0;
		if (value.endsWith("pt")) {
			value = value.substring(0, value.length() - 2);
			isPt = true;
		} else if (value.endsWith("%")) {
			value = value.substring(0, value.length() - 1);
			isPerc = true;
		}
		try {
			d = Double.parseDouble(value);
		} catch (Exception e) {
			d = 0;
		}
		if (isPt) { 
			
		} else if (isPerc) {
			d = d * 14.0 / 100.0;
		} else {
			d = d * 72.0;
		}
		
		//Log.i("drawe", String.format("[dimensionFromString] original:%s returned:%f", value, d));
		
		return (float)d;
	}

	public String htmlTextForPopup(String popupName) {
		return htmlTextForPopupForPopupNumber(popupName, -1);
	}

	public String htmlTextForNoteRecord(int record) {
		VBCustomNotes recNote = recordNotesForRecord(record);
		if (recNote != null)
			return htmlTextForRecordText(recNote.getNoteText());
		return null;
	}

	private String htmlTextForRecordText(String text) {
		if (text.indexOf('&') >= 0)
			text = text.replace("&", "&amp;");
		if (text.indexOf('<') >= 0)
			text = text.replace("<", "&lt;");
		if (text.indexOf('>') >= 0)
			text = text.replace(">", "&gt;");

		text = String.format("<p>%s</p>", text);
		return envelopeHtmlInBody(text);
	}

    public VBCustomNotes recordNotesForRecord(int record) {
        for(VBCustomNotes note : p_customNotes) {
            if (note.getRecordId() == record) {
                return note;
            }
        }
        return null;
    }

    public VBCustomHighlights highlightersForRecord(int record) {
        for(VBCustomHighlights note : p_customHighlights) {
            if (note.getRecordId() == record) {
                return note;
            }
        }
        return null;
    }

    public String plainText(int record) {
		List<FolioTextRecord> texts = findTextRecords(record, 1);
		if (texts.size() == 0)
			return null;
		return texts.get(0).getPlainText();
	}

	public String textForPopupNumber(int record, int popup) {
		List<FolioTextRecord> texts = findTextRecords(record, 1);
		if (texts.size() == 0)
			return null;

		FolioTextRecord plainDict = texts.get(0);
		plainDict.setLevelName("");
		FlatFileString str = new FlatFileString();
		str.setString(plainDict.getPlainText());
		str.setCatchPwCounter(popup);
		HtmlStringBuilder html = str.htmlStringWithStyles(null, plainDict);

		return envelopeHtmlInBody(html.toString());
	}

	public String htmlTextForPopupForPopupNumber(String decodeLinkSafeString,
			int pwCount) {
		FolioTextRecord plainText = findPopupText(decodeLinkSafeString);
		if (plainText != null) {
			FlatFileString flat = new FlatFileString();
			flat.setString(plainText.getPlainText());
			if (pwCount >= 0)
				flat.setCatchPwCounter(pwCount);
			FolioTextRecord recDict = new FolioTextRecord();
			recDict.setNamedPopup(decodeLinkSafeString);
			recDict.setLevelName(plainText.getLevelName());
			HtmlStringBuilder htmlStr = flat
					.htmlStringWithStyles(null, recDict);
			return envelopeHtmlInBody(htmlStr.toString());
		}
		return null;
	}

	public String envelopeHtmlInBody(String string) {
		StringBuilder sb = new StringBuilder();

		if (bodyFontSize < 3) {
			bodyFontSize = 14;
		}
		sb.append("<html><head><title>Rec</title>");

		sb.append(String.format(Locale.getDefault(),
				"<link href=\"%s\" type=text/css rel=stylesheet>\n",
				URL_STYLE_FONTS));
		sb.append(String.format(Locale.getDefault(),
				"<link href=\"%s\" type=text/css rel=stylesheet>\n",
				URL_STYLE_SHEET));
		// sb.append(String.format("<script type=\"text/javascript\" src=\"%s\">\n",
		// URL_MAIN_JAVASCRIPT));
		// sb.append("</script>\n");

		sb.append("<style>\n");
		sb.append("<!--\n");
		sb.append(".FolioFoundText { background-color:#ccccff;}\n-->\n");
		sb.append(String.format(Locale.getDefault(), "TD { font-size: %dpt; font-family:'%s';}\n",
				bodyFontSize, bodyFontFamily));
		sb.append("</style>\n</head>\n");
		sb.append(String.format(Locale.getDefault(),
				"<body id=\"body\" style='%s' background=\"%s\">\n",
				bodyStyleString(), bodyBackgroundImage));
		sb.append(string);
		sb.append("</body></html>");

		return sb.toString();
	}

	private FolioTextRecord findPopupText(String decodeLinkSafeString) {
		FolioTextRecord result = null;
		String[] columns = new String[] { "class", "plain" };
		String condition = String.format(Locale.US, "title = ?",
				decodeLinkSafeString);
		String[] values = new String[] { decodeLinkSafeString };
		Cursor cursor = database.query("popup", columns, condition, values,
				null, null, null);
		int iClass = cursor.getColumnIndex("class");
		int iText = cursor.getColumnIndex("plain");

		// in the case this is non-zero page
		// add reference to parent directory
		if (cursor.moveToNext()) {
			result = new FolioTextRecord();
			result.setLevelName(cursor.getString(iClass));
			result.setPlainText(cursor.getString(iText));
		}
		cursor.close();

		return result;
	}

	public int findJumpDestination(String jumpDestination) {
		int result = -1;
		String[] columns = new String[] { "recid", "title" };
		String condition = "title = ?";
		String[] values = new String[] { jumpDestination };
		Cursor cursor = database.query("jumplinks", columns, condition, values,
				null, null, "recid");
		int index = cursor.getColumnIndex("recid");

		// in the case this is non-zero page
		// add reference to parent directory
		if (cursor.moveToNext()) {
			result = cursor.getInt(index);
		}
		cursor.close();

		return result;
	}

	public FolioObjectRecord findObject(String objectName, int dataFormat) {
		FolioObjectRecord result = null;
		String[] columns = new String[] { "objectData", "objectName",
				"objectType" };
		String condition = String.format(Locale.US, "objectName = ?",
				objectName);
		String[] values = new String[] { objectName };
		Cursor cursor = database.query("objects", columns, condition, values,
				null, null, null);
		int iData = cursor.getColumnIndex("objectData");
		int iType = cursor.getColumnIndex("objectType");

		// in the case this is non-zero page
		// add reference to parent directory
		if (cursor.moveToNext()) {
			result = new FolioObjectRecord();
			result.objectName = objectName;
			result.objectType = cursor.getString(iType);
			if (dataFormat == FolioObjectRecord.DATA_BYTES)
				result.objectData = cursor.getBlob(iData);
			else if (dataFormat == FolioObjectRecord.DATA_STREAM)
				result.objectStream = new ByteArrayInputStream(
						cursor.getBlob(iData));
		}
		cursor.close();

		return result;
	}

	public int searchFirstRecord(String siksa) {
		VBFolioQuery folioQuery = new VBFolioQuery(this);
		int firstRecord = -1;
		ArrayList<VBFolioQueryItem> arr = folioQuery.sourceToArray(siksa);
		SearchTreeContext ctx = new SearchTreeContext();
		ctx.setQuotes(null);
		ctx.setWordsDomain("");
		ctx.setExactWords(true);
		VBFolioQueryOperator fop = null;

		try {
			fop = folioQuery.convertArrayToTree(arr, ctx);
			fop.validate();

			if (fop.isEOF() == false) {
				firstRecord = fop.getCurrentRecord();
			}

		} catch (Exception e) {

		}

		return firstRecord;
	}

	public int searchFirstRecordLike(String siksa) {
		VBFolioQuery folioQuery = new VBFolioQuery(this);
		int firstRecord = -1;
		ArrayList<VBFolioQueryItem> arr = folioQuery.sourceToArray(siksa);
		SearchTreeContext ctx = new SearchTreeContext();
		ctx.setQuotes(null);
		ctx.setWordsDomain("");
		ctx.setExactWords(false);
		VBFolioQueryOperator fop = null;

		try {
			fop = folioQuery.convertArrayToTree(arr, ctx);
			fop.validate();

			if (fop.isEOF() == false) {
				firstRecord = fop.getCurrentRecord();
			}

		} catch (Exception e) {

		}

		return firstRecord;
	}

	public String highlightWords(String htmlText) {
		return htmlText;
	}

	public List<VBBookmark> getBookmarks() {
		return p_bookmarks;
	}

	public VBBookmark findBookmarkByName(String name) {
		for (VBBookmark bkmk : p_bookmarks) {
			if (name.equals(bkmk.getName()))
				return bkmk;
		}
		return null;
	}

    private void broadcastCustomReferencesChange() {
        for(FolioDelegate dx : delegateList) {
            dx.customReferencesDidChange();
        }
    }

	public void addBookmark(VBBookmark bkmk) {
        if (bkmk.getThisId() <= 0) {
            bkmk.setThisId(getUniqueId());
        }
		p_bookmarks.add(bkmk);
        p_bookmarksModified = true;
        broadcastCustomReferencesChange();
	}

    public void addNote(VBCustomNotes note) {
        if (note.getThisId() <= 0) {
            note.setThisId(getUniqueId());
        }
        p_customNotes.add(note);
        p_customNotesModified = true;
        broadcastCustomReferencesChange();
    }

    public void addCustomHighlight(VBCustomHighlights note) {
        if (note.getThisId() <= 0) {
            note.setThisId(getUniqueId());
        }
        p_customHighlights.add(note);
        p_customHighlightsModified = true;
        broadcastCustomReferencesChange();
    }

    public void removeBookmarkAt(int currentPosition) {
		if (currentPosition >= 0 && currentPosition < p_bookmarks.size()) {
			p_bookmarks.remove(currentPosition);
            p_bookmarksModified = true;
            broadcastCustomReferencesChange();
		}
	}

	public VBContentNode findRecordPath(int recID) {
		initContentObject();
		return content.findRecordPath(recID);
	}

	public void initContentObject() {
		if (content != null)
			return;

		VBContentNode root = new VBContentNode();
		root.recordId = 0;
		root.setFolio(this);
		
		this.content = root;
/*
		VBContentNode row;
		Cursor cursor;
		cursor = database.rawQuery("select * from contents where parent = 0", null);
		int iTitle = cursor.getColumnIndex("title");
		int iRecord = cursor.getColumnIndex("record");
		int iParent = cursor.getColumnIndex("parent");
		int iLevel = cursor.getColumnIndex("level");
		int iSimpletitle = cursor.getColumnIndex("simpletitle");

		VBContentNode previous = null;
		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
			row = new VBContentNode();
			row.setTitle(cursor.getString(iTitle));
			row.setRecordId(cursor.getInt(iRecord));
			row.setLevel(cursor.getInt(iLevel));
			row.setParentRecordId(cursor.getInt(iParent));
			row.setSimpletitle(cursor.getString(iSimpletitle));

			// VBContentNode par =
			// root.findRecord(row.getParentRecordId());
			VBContentNode par = null;

			if (previous != null) {
				par = previous.findAncestor(row.getParentRecordId());
			}

			if (par == null) {
				root.addChildItem(row);
				row.setParent(root);
			} else {
				par.addChildItem(row);
				row.setParent(par);
			}

			previous = row;
		}
		cursor.close();

		this.content = root;*/
	}

	public ArrayList<VBContentNode> findContentItems(int parentId) {
		ArrayList<VBContentNode> list = new ArrayList<VBContentNode>();
		VBContentNode row;

		Cursor cursor = database.rawQuery("select * from contents where parent = "
				+ parentId, null);
		
		int iTitle = cursor.getColumnIndex("title");
		int iRecord = cursor.getColumnIndex("record");
		int iParent = cursor.getColumnIndex("parent");
		int iLevel = cursor.getColumnIndex("level");
		int iSimpletitle = cursor.getColumnIndex("simpletitle");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
			row = new VBContentNode();
			row.setTitle(cursor.getString(iTitle));
			row.setRecordId(cursor.getInt(iRecord));
			row.setLevel(cursor.getInt(iLevel));
			row.setParentRecordId(cursor.getInt(iParent));
			row.setSimpletitle(cursor.getString(iSimpletitle));
			row.setFolio(this);
			list.add(row);
		}
		cursor.close();

		return list;
	}

	public boolean isContentInitialized() {
		return content != null;
	}

	public String getRecordPath(int record) {
		String title = "";
		VBContentNode item = findRecordPath(record);
		if (item != null) {
			title = getDocumentPath(item);
		}
		return title;
	}
	
	public String getDocumentPath(VBContentNode item) {
		if (item == null)
			return "";
		if (content == null)
			return "";

		StringBuilder sb = new StringBuilder();

		while (item.getParent() != null) {
			if (sb.length() == 0) {
				sb.append(item.getText());
			} else {
				sb.insert(0, " / ");
				sb.insert(0, item.getText());
			}
			item = item.getParent();
		}

		return sb.toString();
	}

	public int getContentSelected() {
		if (content == null)
			return 0;
		return content.getSelected();
	}

	public void search(String text,
			FlatFileSearchResults results,
			VBHighlightedPhraseSet phrases, NSRange recordRange) {

		VBFolioQuery folioQuery = new VBFolioQuery(this);
		ArrayList<VBFolioQueryItem> arr = folioQuery.sourceToArray(text);
		SearchTreeContext ctx = new SearchTreeContext();
		ctx.setQuotes(phrases);
		ctx.setWordsDomain("");
		ctx.setExactWords(true);
		VBFolioQueryOperator fop = null;
		ArrayList<SearchResultItem> currentPage = results.results;
		SearchResultItem resultItem = null;
		int currRecX = 0;
        if (recordRange.getLocation() == 0)
            recordRange.setLocation(1);

        currentPage.clear();
		results.emptyResults = false;

		try {
            fop = folioQuery.convertArrayToTree(arr, ctx);
            fop.printTree(0);
            fop.validate();

            while (!fop.isEOF()) {
                currRecX = fop.getCurrentRecord();
                //Log.i("currRec", "is " + currRecX);
                if (recordRange.contains(currRecX)) {
                    resultItem = new SearchResultItem();
                    resultItem.setRecordId(currRecX);
                    currentPage.add(resultItem);
                }

                fop.gotoNextRecord();
            }

			if (currentPage.size() == 0) {
				results.emptyResults = true;
			}

		} catch (Exception e) {

			results.flatTextAlternative = String.format(Locale.getDefault(), "<CR>Search process was interrupted after record with number %d."
					+ " There is most probably some problem with search engine. If you are willing to report this issue, please contact developers"
					+ " of this application and provide then this message.<CR><CR>Query that caused this issue is:<CR>%s<CR><CR><CR>", currRecX, text);
			results.emptyResults = true;
		}
	}

	public String explainSearch(String text) {

		VBFolioQuery folioQuery = new VBFolioQuery(this);
		/*if (recordRange.getLocation() == 0)
			recordRange.setLocation(1);

		results.clear();*/

		try {
			StringBuilder sb = new StringBuilder();

			// prepare search context
			SearchTreeContext ctx = new SearchTreeContext();
			//ctx.setQuotes(phrases);
			ctx.setWordsDomain("");
			ctx.setExactWords(true);

			// analyze query to array
			ArrayList<VBFolioQueryItem> arr = folioQuery.sourceToArray(text);

			// prepare query
			VBFolioQueryOperator fop = folioQuery.convertArrayToTree(arr, ctx);
			fop.validate();
			fop.flushRecordHits();

			sb.append("<PS:Normal>");
			sb.append("<HR><PS:Normal>");
			sb.append("<HR><PS:Normal>");
			sb.append("<HR><PS:HeadA>No Results");
			sb.append("<HR><PS:Normal>No Results for query: \"" + text + "\"");
			sb.append("<HR><PS:Normal>Explanation of the query (with number of hits for particular part) follows. You can see here what word has zero hits to adjust your query.");
			sb.append("<HR><PS:Normal>");
			fop.printTreeEx(0, sb);
			sb.append("<HR><PS:Normal>");
			sb.append("<HR><PS:Normal>");
			sb.append("<HR><PS:Normal>");

			return sb.toString();

		} catch (Exception e) {
		}

		return "";
	}

	public void enumerateContentItemsWithSimpleText(String simpleText,
			ArrayList<Integer> array) {

		Cursor c = database
				.rawQuery(
						"select record from contents where simpletitle = ? order by record",
						new String[] { simpleText });
		while (c.moveToNext()) {
			array.add(c.getInt(0));
		}
		c.close();

	}

	public void enumerateContentItemsLikeSimpleText(String simpleText,
			ArrayList<Integer> array) {

		Cursor c = database
				.rawQuery(
						"select record from contents where simpletitle like ? order by record",
						new String[] { simpleText });
		while (c.moveToNext()) {
			array.add(c.getInt(0));
		}
		c.close();
	}

	public void enumerateContentItemsForParent(int rec, ArrayList<Integer> array) {

		Cursor c = database
				.rawQuery(
						String.format(
								"select record from contents where parent = %d order by record",
								rec), null);
		while (c.moveToNext()) {
			array.add(c.getInt(0));
		}
		c.close();
	}

	public int getSubRangeEndForRecord(int i) {
		Cursor cursor = database
				.rawQuery(
						"select min(b.record) from contents a, contents b where a.record = "
								+ i
								+ " and b.level <= a.level and b.record > a.record",
						null);
		int subrange = 0;

		if (cursor.moveToNext()) {
			subrange = cursor.getInt(0) - 1;
		} else {
			cursor.close();
			cursor = database.rawQuery("select max(recid) from texts", null);
			if (cursor.moveToNext()) {
				subrange = cursor.getInt(0) - 1;
			}
		}
		cursor.close();
		return subrange;
	}

	public int getRecordCount() {
		int subrange = 0;
		Cursor cursor = database.rawQuery("select max(recid) from texts", null);
		if (cursor.moveToNext()) {
			subrange = cursor.getInt(0) - 1;
		}
		cursor.close();
		return subrange;
	}

	public void enumerateLevelRecordsWithSimpleTitle(int levelIndex,
			String simpleTitle, ArrayList<NSRange> levelRecords) {
		String[] columns = new String[] { "record" };
		String where = "";
        String query = "";
        String[] selArgs = null;
		if (levelIndex < 0) {
            query = "select record, next_sibling from contents where simpletitle=?";
            selArgs = new String[] { simpleTitle};
		} else {
			if (simpleTitle == null || simpleTitle.length() == 0) {
                query = "select record, next_sibling from contents where level=" + levelIndex;
			} else {
                query = "select record, next_sibling from contents where level=" + levelIndex + " and simpletitle=?";
                selArgs = new String[] { simpleTitle};
            }
		}

        Log.i("db", "Query: " + query + "   , simpletitle: " + (simpleTitle != null ? simpleTitle : "(null)"));
        Cursor cursor = database.rawQuery(query, selArgs);
		int iRecord = cursor.getColumnIndex("record");
        int iNext = cursor.getColumnIndex("next_sibling");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
            NSRange range = new NSRange();
            range.setLocation(cursor.getInt(iRecord));
            range.setLocationEnd(cursor.getInt(iNext));
			levelRecords.add(range);
		}
		cursor.close();

    }

	public void enumerateLevelRecordsLikeSimpleTitle(int levelIndex,
			String simpleTitle, ArrayList<NSRange> levelRecords) {

		String[] columns = new String[] { "record", "next_sibling" };
		Cursor cursor = database.query("contents", columns, "level = "
				+ levelIndex + " and simpletitle like ?", new String[] { "%"
				+ simpleTitle + "%" }, null, null, "record");
		int iRecord = cursor.getColumnIndex("record");
        int iNext = cursor.getColumnIndex("next_sibling");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
            NSRange range = new NSRange();
            range.setLocation(cursor.getInt(iRecord));
            range.setLocationEnd(cursor.getInt(iNext));
            levelRecords.add(range);
		}
		cursor.close();
	}

	public void enumerateLevelRecords(int levelIndex, ArrayList<NSRange> arr) {
		String[] columns = new String[] { "record", "next_sibling" };
		Cursor cursor = database.query("contents", columns, "level = "
				+ levelIndex, null, null, null, "record");
		int iRecord = cursor.getColumnIndex("record");
        int iNext = cursor.getColumnIndex("next_sibling");

		// in the case this is non-zero page
		// add reference to parent directory
		while (cursor.moveToNext()) {
            NSRange range = new NSRange();
            range.setLocation(cursor.getInt(iRecord));
            range.setLocationEnd(cursor.getInt(iNext));
            arr.add(range);
		}
		cursor.close();
	}

	public String simpleContentTextForRecord(int rec) {
		Cursor c = database.rawQuery(
				"select simpletitle from contents where record = " + rec, null);
		String str = null;
		while (c.moveToNext()) {
			str = c.getString(0);
		}
		c.close();
		return str;
	}

	public void enumerateGroupRecords(String groupName, ArrayList<Integer> array) {

		String query = null;
		String[] selArgs = null;
		if (groupName == null || groupName.length() == 0) {
			query = "select distinct recid from groups order by recid";
		} else {
			query = "select recid from groups where groupname = ? order by recid";
			selArgs = new String[] { groupName };
		}

		Cursor c = database.rawQuery(query, selArgs);
		while (c.moveToNext()) {
			array.add(c.getInt(0));
		}
		c.close();
	}

	public void searchWordsForIndex(String word, String idxTag,
			ArrayList<String> array) {

		word = word.replace("?", "_").replace("*", "%");
		Cursor c = database.rawQuery(
				"select word from words where word like ? and idx = ?",
				new String[] { word, idxTag });

		int ic = c.getColumnIndex("word");

		if (ic != 0)
			Log.w("warn",
					"warning: index of column WORD is not 0 !!!!!! check other queries");
		while (c.moveToNext()) {
			array.add(c.getString(ic));
		}

		c.close();
	}

	public ArrayList<VBFolioRecordStream> getWordIndexData(String word, String idxTag) {

		ArrayList<VBFolioRecordStream> streams = new ArrayList<VBFolioRecordStream>();

		Cursor c = database.rawQuery(
				"select data, indexbase from words where word = ? and idx = ? order by indexbase",
				new String[] { word, idxTag });
	
		int ic = c.getColumnIndex("data");
		int ibi = c.getColumnIndex("indexbase");
		int count =0;
		while (c.moveToNext()) {
			VBFolioRecordStream stream = new VBFolioRecordStream();
			stream.setBytes(c.getBlob(ic));
			stream.baseRecordId = c.getInt(ibi);
			streams.add(stream);
			count++;
		}

		c.close();

		Log.i("index", "Found " + count + " streams for word " + word);
		
		return streams;
	}

	public int findOriginalLevelIndex(String levelName) {

		int value = -1;
		Cursor c = database.rawQuery(
				"select id from levels where original = ?",
				new String[] { levelName });

		if (c.moveToFirst()) {
			value = c.getInt(c.getColumnIndex("id"));
		}

		c.close();

		return value;
	}

    public VBCustomNotes safeRecordNotesForRecord(int i) {
        VBCustomNotes notes = recordNotesForRecord(i);
        if (notes == null) {
            notes = new VBCustomNotes();
            notes.setRecordId(i);
            notes.setThisId(getUniqueId());
            notes.setRecordPath(getRecordPath(i));
            notes.setCreateDate(new Date());
            notes.setModifyDate(new Date());

            insertCustomNoteSorted(notes);
        }
        return notes;
    }

    public VBCustomHighlights safeHighlightersForRecord(int i) {
        VBCustomHighlights notes = highlightersForRecord(i);
        if (notes == null) {
            notes = new VBCustomHighlights();
            notes.setRecordId(i);
            notes.setThisId(getUniqueId());
            notes.setRecordPath(getRecordPath(i));
            notes.setCreateDate(new Date());
            notes.setModifyDate(new Date());

            p_customHighlights.add(notes);
            p_customHighlightsModified = true;
            broadcastCustomReferencesChange();
        }
        return notes;
    }

    private void insertCustomNoteSorted(VBCustomNotes notes) {
        for (int m = 0; m < p_customNotes.size(); m++) {
            if (p_customNotes.get(m).recordId > notes.getRecordId()) {
                p_customNotes.add(m, notes);
                return;
            }
        }
        p_customNotes.add(notes);
    }

    public int bookmarksCount() {
		return p_bookmarks.size();
	}

	public int notesCount() {
		int count = 0;
		if (p_customNotes != null) {
            count = p_customNotes.size();
		}
		return count;
	}
	
	public int highlightersCount() {
		int count = 0;
		if (p_customHighlights != null) {
			count = p_customHighlights.size();
		}

		return count;
	}

	public List<VBCustomNotes> getRecordNotes() {
		return p_customNotes;
	}

	public void removeBookmark(VBBookmark bkmk) {
        removeBookmarkWithId(bkmk.getThisId());
	}

    //
    // Removes Bookmark with given ID
    //
	public void removeBookmarkWithId(int bookmarkID) {
		int bk = findBookmarkIndexById(bookmarkID);
		if (bk >= 0) {
            int parentId = p_bookmarks.get(bk).getParentId();
            for(VBBookmark item : p_bookmarks) {
                if (item.getParentId() == bookmarkID)
                    item.setParentId(parentId);
            }
            p_bookmarksModified = true;
			p_bookmarks.remove(bk);
            broadcastCustomReferencesChange();
		}
	}

    //
    // Removes Note with given ID
    //
	public void removeRecordNoteWithId(int noteId) {
		int note = findRecordNoteIndexById(noteId);
		if (note >= 0) {
            int parentId = p_customNotes.get(note).getParentId();
            for(VBCustomNotes item : p_customNotes) {
                if (item.getParentId() == noteId) {
                    item.setParentId(parentId);
                }
            }
            p_customNotesModified = true;
            p_customNotes.remove(note);
            broadcastCustomReferencesChange();
        }
	}

    public void removeCustomHighlightsWithId(int hid) {
        int index = findCustomHighlightsIndexById(hid);
        if (index >= 0) {
            int parentId = p_customHighlights.get(index).getParentId();
            for(VBCustomHighlights item : p_customHighlights) {
                if (item.getParentId() == hid) {
                    item.setParentId(parentId);
                }
            }
            p_customHighlightsModified = true;
            p_customHighlights.remove(index);
            broadcastCustomReferencesChange();
        }
    }

    public String getCustomReferencesFileName() {
        return getDatabasePath() + File.separatorChar + "CustomRefs.txt";
    }

    public String getCustomQueriesFileName() {
        return getDatabasePath() + File.separatorChar + "CustomQuer.txt";
    }

	public void saveCustomReferences() {

        TagFileStream writer = null;

        try {
            Log.i("Folio", "#################################### SAVE CUSTOM REFS ########################");
            Log.i("Folio", getCustomReferencesFileName());
            writer = new TagFileStream(getCustomReferencesFileName(), true);

            // save bookmarks
            for(VBBookmark bm : p_bookmarks) {

                writer.writeTag("bookmark", Integer.toString(bm.getThisId()));
                writer.writeAttribute("parentId", Integer.toString(bm.getParentId()));
                writer.writeAttribute("recordId", Integer.toString(bm.getRecordId()));
                writer.writeAttribute("created", Long.toString(bm.getCreateDate().getTime()));
                writer.writeAttribute("modifyDate", Long.toString(bm.getModifyDate().getTime()));
                writer.writeAttribute("name", bm.getName());
            }

            // save notes
            for(VBCustomNotes rn : p_customNotes) {
                writer.writeTag("note", "" + rn.getThisId());
                writer.writeAttribute("parentId", "" + rn.getParentId());
                writer.writeAttribute("recordId", Integer.toString(rn.recordId));
                writer.writeAttribute("createDate", Long.toString(rn.getCreateDate().getTime()));
                writer.writeAttribute("modifyDate", Long.toString(rn.getModifyDate().getTime()));
                writer.writeAttribute("noteText", rn.getNoteText());
                writer.writeAttribute("recordPath", rn.getRecordPath());
            }

            // save highlighted texts
            for(VBCustomHighlights rn : p_customHighlights) {
                writer.writeTag("hightext", "" + rn.getThisId());
                writer.writeAttribute("parentId", "" + rn.getParentId());
                writer.writeAttribute("recordId", Integer.toString(rn.recordId));
                writer.writeAttribute("createDate", Long.toString(rn.getCreateDate().getTime()));
                writer.writeAttribute("modifyDate", Long.toString(rn.getModifyDate().getTime()));
                writer.writeAttribute("highlightedText", rn.getHighlightedText());
                writer.writeAttribute("recordPath", rn.getRecordPath());

                int count = rn.anchorsCount();
                for(int j = 0; j < count; j++) {
                    int key = rn.anchors.keyAt(j);
                    writer.writeTag("anchor", Integer.toString(key));
                    writer.writeAttribute("value", rn.getAnchorText(key));
                }
            }


            p_bookmarksModified = false;
            p_customNotesModified = false;
            p_customHighlightsModified = false;

        } catch (Exception e) {

            Log.e("Folio", "Exception during saving custom references.");
            e.printStackTrace();

        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
                Log.e("Folio", "Exception during saving custom references / closing file.");
                e.printStackTrace();
            }
        }
	}

	public void loadCustomReferences() {

        TagFileStream parser = null;

        try {
            Log.i("Folio", "#################################### LOAD CUSTOM REFS ########################");
            Log.i("Folio", getCustomReferencesFileName());
            parser = new TagFileStream(getCustomReferencesFileName(), false);

            VBBookmark currentBookmark = null;
            VBCustomNotes currentNote = null;
            VBCustomHighlights currentHightext = null;


            p_bookmarks.clear();
            p_customNotes.clear();
            p_customHighlights.clear();
            int mode = 0;

            while(parser.next()) {
                if (parser.isTag("bookmark")) {
                    currentBookmark = new VBBookmark();
                    currentBookmark.setThisId(Integer.parseInt(parser.getValue()));
                    p_bookmarks.add(currentBookmark);
                    mode = 1;
                } else if (parser.isTag("note")) {
                    currentNote = new VBCustomNotes();
                    currentNote.setThisId(Integer.parseInt(parser.getValue()));
                    p_customNotes.add(currentNote);
                    mode = 2;
                } else if (parser.isTag("hightext")) {
                    currentHightext = new VBCustomHighlights();
                    currentHightext.setThisId(Integer.parseInt(parser.getValue()));
                    p_customHighlights.add(currentHightext);
                    mode = 3;
                } else if (parser.isTag("anchor")) {
                    mode = 4;
                } else if (mode == 1) {
                    if (parser.isAttribute("parentId")) {
                        currentBookmark.setParentId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("recordId")) {
                        currentBookmark.setRecordId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("created")) {
                        Date createdDate = new Date();
                        createdDate.setTime(Long.parseLong(parser.getValue()));
                        currentBookmark.setCreateDate(createdDate);
                    } else if (parser.isAttribute("name")) {
                        currentBookmark.setName(parser.getValue());
                    }
                } else if (mode == 2) {
                    if (parser.isAttribute("parentId")) {
                        currentNote.setParentId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("recordId")) {
                        currentNote.setRecordId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("createDate")) {
                        Date createdDate = new Date();
                        createdDate.setTime(Long.parseLong(parser.getValue()));
                        currentNote.setCreateDate(createdDate);
                    } else if (parser.isAttribute("modifyDate")) {
                        Date modifyDate = new Date();
                        modifyDate.setTime(Long.parseLong(parser.getValue()));
                        currentNote.setModifyDate(modifyDate);
                    } else if (parser.isAttribute("noteText")) {
                        currentNote.setNoteText(parser.getValue());
                    } else if (parser.isAttribute("recordPath")) {
                        currentNote.setRecordPath(parser.getValue());
                    }
                } else if (mode == 3) {
                    if (parser.isAttribute("parentId")) {
                        currentHightext.setParentId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("recordId")) {
                        currentHightext.setRecordId(Integer.parseInt(parser.getValue()));
                    } else if (parser.isAttribute("createDate")) {
                        Date createdDate = new Date();
                        createdDate.setTime(Long.parseLong(parser.getValue()));
                        currentHightext.setCreateDate(createdDate);
                    } else if (parser.isAttribute("modifyDate")) {
                        Date modifyDate = new Date();
                        modifyDate.setTime(Long.parseLong(parser.getValue()));
                        currentHightext.setModifyDate(modifyDate);
                    } else if (parser.isAttribute("highlightedText")) {
                        currentHightext.setHighlightedText(parser.getValue());
                    } else if (parser.isAttribute("recordPath")) {
                        currentHightext.setRecordPath(parser.getValue());
                    }
                } else if (mode == 4) {
                    if (currentHightext != null) {
                        if (parser.isAttribute("value")) {
                            currentHightext.setAnchorText(parser.getValue());
                        }
                    }
                }
            }

        } catch (Exception e) {

        } finally {
            try {
                if (parser != null)
                    parser.close();
            } catch (Exception e) {
            }
        }
	}
	
	public void saveQueries() {
        TagFileStream sw = null;
		try {
            sw = new TagFileStream(getCustomQueriesFileName(), true);
			for(FolioQueryInstance qi : queryHistory) {
                sw.writeTag("query", qi.name);
                sw.writeAttribute("pattern", qi.pattern);
                sw.writeAttribute("data", qi.data);
			}
		} catch (Exception e) {
			
		} finally {
            if (sw != null)
                sw.close();
        }

	}
	
	public void loadQueries() {
        TagFileStream inputStream = null;
		try {
            FolioQueryInstance qi = null;
			inputStream = new TagFileStream(getCustomQueriesFileName(), false);

            while (inputStream.next()) {
                //Log.i("line", line);
                if (inputStream.isTag("query")) {
                    qi = new FolioQueryInstance();
                    qi.name = inputStream.getValue();
                    queryHistory.add(qi);
                } else if (inputStream.isAttribute("data")) {
                    if (qi != null)
                        qi.data = inputStream.getValue();
                } else if (inputStream.isAttribute("pattern")) {
                    if (qi != null)
                        qi.pattern = inputStream.getValue();
                }
            }

		} catch (Exception e) {
			
		} finally {
            if (inputStream != null)
                inputStream.close();
        }

	}
	
	
	public FolioQueryTemplate findQueryTemplate(String templateName) {
		for(FolioQueryTemplate template : queryTemplates) {
			if (template.name.equals(templateName)) {
				return template;
			}
		}
		return null;
	}

	public void addQueryHistory(FolioQueryInstance fqi) {
		queryHistory.add(fqi);
		if (queryHistory.size() > 30) {
			queryHistory.remove(0);
		}
		saveQueries();
	}
	
	public String docInfoValue(String key) {
		if (docinfo.containsKey(key)) {
			return docinfo.get(key);
		}
		
		return "";
	}

	public void selectContentItem(VBContentRow contItem, int newStatus) {
		
		Log.i("contsel", "item selected : " + contItem.getRecord() + ", new status: " + newStatus);
		int parent = contItem.getParent();
		int itemid = contItem.getRecord();
		List<VBContentRow> list = null;
		list = findContentItemsByParent(parent);
		for(VBContentRow item : list) {
			if (item.getType() == VBContentRow.TYPE_ITEM) {
				if (item.getRecord() == itemid) {
					item.setSelected(newStatus);
				}
			}
		}
		
		// set new status to child
		propagateStatusToChildren(newStatus, itemid);
		
		// set new status to parent
		propagateStatusToParent(newStatus, list);
	}

	public void propagateStatusToParent(int newStatus,
			List<VBContentRow> list) {
		int backParent = -1;
		int prevSelection = -1;
		int currParent = -1;
		for(VBContentRow item : list) {
			if (item.getType() == VBContentRow.TYPE_BACK) {
				backParent = item.getRecord();
			}
			if (item.getType() == VBContentRow.TYPE_ITEM) {
				if (prevSelection < 0) {
					prevSelection = item.getSelected();
					currParent = item.getParent();
				}
				if (item.getSelected() != prevSelection) {
					newStatus = VBContentRow.SELECTION_MAYBE;
				}
				prevSelection = item.getSelected();
			}
		}
		if (backParent >= 0) {
			list = findContentItemsByParent(backParent);
			for(VBContentRow item : list) {
				if (item.getType() == VBContentRow.TYPE_ITEM) {
					if (item.getRecord() == currParent) {
						item.setSelected(newStatus);
					}
				}
			}
			propagateStatusToParent(newStatus, list);
		}
	}

	public void propagateStatusToChildren(int newStatus, int itemid) {
		List<VBContentRow> list;
		list = contentPages.get(itemid);
		if (list != null) {
			for(VBContentRow item : list) {
				if (item.getType() == VBContentRow.TYPE_ITEM) {
					item.setSelected(newStatus);
					propagateStatusToChildren(newStatus, item.getRecord());
				}
			}
		}
	}

    public List<VBContentRow> getNotesContentItems(ArrayList<VBContentRow> list,
                                                   int parentID) {

        VBContentRow row = null;
		int record;

        for (VBCustomNotes note : p_customNotes) {
            if (note.getParentId() == parentID) {
                if (note.hasText()) {
					record = note.getRecordId();
                    row = new VBContentRow();
                    row.setMainTitle(note.getNoteText());
                    row.setParent(parentID);
                    row.setID(note.getThisId());
                    row.setType(VBContentRow.TYPE_NOTE);
                    row.setRecord(note.recordId);
                    row.setTag(note);
                    row.setCreateTime(note.getCreateDate().getTime());
                    row.setModifyTime(note.getModifyDate().getTime());
                    row.setSortRule(VBContentRow.SORT_NAME);
					if (record < 0) {
						row.setAction("load note " + note.getThisId());
					} else {
						row.setAction("show text " + record);
					}

					list.add(row);
                }
            }
        }

        if (list.size() == 0) {
            row = new VBContentRow();
            row.setMainTitle("No custom notes defined for this directory. You can define new note or highlight some text by long-tap in the text view and defining your custom note or highlighting.");
            row.setParent(parentID);
            row.setID(0);
            row.setType(VBContentRow.TYPE_NOTICE);
            list.add(row);
        } else {
            Collections.sort(list);
        }

        return list;
    }

    public List<VBContentRow> getHighlightsContentItems(ArrayList<VBContentRow> list,
                                                   int parentID) {

        VBContentRow row = null;
		int record;

        for (VBCustomHighlights note : p_customHighlights) {
            if (note.getParentId() == parentID) {
                if (note.hasHighlighter()) {
					record = note.getRecordId();
                    row = new VBContentRow();
                    row.setMainTitle(note.getHighlightedText());
                    row.setParent(parentID);
                    row.setID(note.getThisId());
                    row.setType(VBContentRow.TYPE_HIGHLIGHTER);
                    row.setRecord(note.recordId);
                    row.setTag(note);
                    row.setCreateTime(note.getCreateDate().getTime());
                    row.setModifyTime(note.getModifyDate().getTime());
                    row.setSortRule(VBContentRow.SORT_NAME);
					if (record < 0) {
						row.setAction("load hightext " + note.getThisId());
					} else {
						row.setAction("show text " + record);
					}
                    list.add(row);
                }
            }
        }

        if (list.size() == 0) {
            row = new VBContentRow();
            row.setMainTitle("No highlighted texts defined for this directory. You can define new note or highlight some text by long-tap in the text view and defining your custom note or highlighting.");
            row.setParent(parentID);
            row.setID(0);
            row.setType(VBContentRow.TYPE_NOTICE);
            list.add(row);
        } else {
            Collections.sort(list);
        }

        return list;
    }

    public List<VBContentRow> getBookmarksContentItems(ArrayList<VBContentRow> list,
                                                       int parentID) {

		VBContentRow row = null;

//		appendBackItem(list, "Contents", 0);
//		appendTitleItem(list, "Bookmarks");
		int record;

        for (VBBookmark bkmk : p_bookmarks) {
            if (parentID == bkmk.getParentId()) {
				record = bkmk.getRecordId();
                row = new VBContentRow();
                row.setMainTitle(bkmk.getName());
                row.setType(VBContentRow.TYPE_BOOKMARK);
                row.setRecord(bkmk.getRecordId());
                row.setID(bkmk.getThisId());
                row.setParent(parentID);
                row.setTag(bkmk);
                row.setCreateTime(bkmk.getCreateDate().getTime());
                row.setModifyTime(bkmk.getModifyDate().getTime());
                row.setSortRule(VBContentRow.SORT_NAME);
				if (record < 0) {
					row.setAction("load bookmark " + bkmk.getThisId());
				} else {
					row.setAction("show text " + record);
				}
                list.add(row);
            }
        }

        if (list.size() == 0) {
            row = new VBContentRow();
            row.setMainTitle("No bookmarks defined for this directory. You can define your bookmark by tapping on the left side of paragraph in the text view and then select Create Bookmark.");
            row.setParent(parentID);
            row.setID(0);
            row.setType(VBContentRow.TYPE_NOTICE);
            list.add(row);
        } else {
            Collections.sort(list);
        }

		return list;
	}

    //
    // after loading of all custom files related to the current
    // folio database, unique ID should be validated, so it is actually unique in true sense
    //
    public void validateUniqueId() {

        for (VBBookmark b: p_bookmarks ) {
            if (b.getThisId() >= uniqueId) {
                uniqueId = b.getThisId() + 1;
            }
        }

        for (VBCustomNotes rn: p_customNotes) {
            if (rn.getThisId() >= uniqueId) {
                uniqueId = rn.getThisId() + 1;
            }
        }

        for (VBCustomHighlights rn: p_customHighlights) {
            if (rn.getThisId() >= uniqueId) {
                uniqueId = rn.getThisId() + 1;
            }
        }

    }

    //
    // generates new unique ID for custom object
    //
    public int getUniqueId() {
        int newId = uniqueId;
        uniqueId++;
        return newId;
    }

    public int getParentIdForContentItem(int recordId) {
        VBContentRow ci = findContentItemByRecord(recordId);
        if (ci == null)
            return 0;
        return ci.getParent();
    }

    public int getParentIdForBookmark(int bkmkId) {
        for(VBBookmark bk : p_bookmarks) {
            if (bk.getThisId() == bkmkId)
                return bk.getParentId();
        }
        return 0;
    }

    public int getParentIdForNote(int noteId) {
        for(VBCustomNotes rn : p_customNotes) {
            if (rn.getThisId() == noteId)
                return rn.getParentId();
        }
        return 0;
    }

    public int getParentIdForHighlighter(int noteId) {
        for(VBCustomHighlights highItem : p_customHighlights) {
            if (highItem.getThisId() == noteId)
                return highItem.getParentId();
        }
        return 0;
    }

    public VBBookmark findBookmarkById(int bid) {
        for(VBBookmark bm : p_bookmarks) {
            if (bm.getThisId() == bid)
                return bm;
        }
        return null;
    }

    public VBCustomNotes findRecordNoteById(int rnid) {
        for(VBCustomNotes rn : p_customNotes) {
            if (rn.getThisId() == rnid)
                return rn;
        }
        return null;
    }

    public VBCustomHighlights findCustomHighlightsById(int rnid) {
        for(VBCustomHighlights rn : p_customHighlights) {
            if (rn.getThisId() == rnid)
                return rn;
        }
        return null;
    }

	public int findBookmarkIndexById(int bid) {
		for(int i = 0; i < p_bookmarks.size(); i++) {
			if (p_bookmarks.get(i).getThisId() == bid)
				return i;
		}
		return -1;
	}

    public int findRecordNoteIndexById(int rnid) {
        for(int i = 0; i < p_customNotes.size(); i++) {
            if (p_customNotes.get(i).getThisId() == rnid)
                return i;
        }
        return -1;
    }

    public int findCustomHighlightsIndexById(int rnid) {
        for(int i = 0; i < p_customHighlights.size(); i++) {
            if (p_customHighlights.get(i).getThisId() == rnid)
                return i;
        }
        return -1;
    }

	public void registerDelegate(FolioDelegate dg) {
		if (delegateList.indexOf(dg) >= 0) {
			return;
		}
		delegateList.add(dg);
	}

	public void unregisterDelegate(FolioDelegate dg) {
		int idx = delegateList.indexOf(dg);
		if (idx >= 0) {
			delegateList.remove(idx);
		}
	}

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }
}
