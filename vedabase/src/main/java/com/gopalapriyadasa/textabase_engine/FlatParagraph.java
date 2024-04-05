package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import com.gopalapriyadasa.bhaktivedanta_textabase.MainActivity;
import com.gopalapriyadasa.bhaktivedanta_textabase.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

public class FlatParagraph {

	public final static int ACTION_NONE = 0;
	public final static int ACTION_CR = 1;
	public final static int ACTION_HR = 2;
	public final static int ACTION_HS = 3;
	public final static int ACTION_IGNOREREC = 4;
	
	public boolean hcParaStarted;
	public boolean acceptText = false;
	public FDParaFormat paraStyleRead = null;	
	public String dataObjectName = "";
	public Object validator = null;
	public int catchPwLevel = 0;
	public int catchPwCounter = 0;
	public int catchNtCounter = 0;
	public int hcPwCounter;
	public int hcNtCounter;
	public int hcTableRows;
	public int hcTableColumns;

	
	private FDRecordBase target = null;
	private FDCharFormat origCharFormatting = null;
	private FDCharFormat charFormatting = null;
	private FDParaFormat paraStyle = null;
	private static HashMap<String,FDPaint> charFormattingMap = null;
	private FDLink currLink = null;
	private Stack<Integer> pwLevel = new Stack<Integer>();
	private Stack<Boolean> pwParaStart = new Stack<Boolean>();
	private Stack<String> pwLinkStyle = new Stack<String>();
	private Stack<FDCharFormat> charStyleStack = new Stack<FDCharFormat>();
	private Stack<FDCharFormat> origCharStyleStack = new Stack<FDCharFormat>();
	private int startIndex = 0;
	private StringBuilder wordBuilder = new StringBuilder();
	public HashMap<String,FDTextFormat> alternativeFormats = null;
	public static HashMap<String,Bitmap> alternativeBitmaps = new HashMap<String,Bitmap>();
	
	//public boolean prevCfBold = false;
	//public boolean prevCfUnderline = false;
	//public boolean prevCfHidden = false;
	//public boolean prevCfStrikeOut = false;
	//public int prevCfForeColor;
	//public String prevCfFontName;
	//public boolean prevCfItalic;
	//public float prevCfFontSize;
	public Folio folio = null;

	public FlatParagraph(Folio source) {
		folio = source;
	}

	
	public FDRecordBase convertToRaw(FolioTextRecord recDict) {

		target = new FDRecordBase();
		target.recordId = recDict.getRecord();
		charFormatting = new FDCharFormat();
		origCharFormatting = new FDCharFormat();
		paraStyle = new FDParaFormat();
		if (charFormattingMap == null) {
			charFormattingMap = new HashMap<String, FDPaint>();
		}
		
		// FIXME enable try - catch in htmlStringWithStyes
		// try {
		int charCounter = 0;
		VBCustomNotes recNotes = null;
		VBCustomHighlights recHighs = null;
		int recNumberId = recDict.getRecord();
		int globalRecordId = -1;
		if (recNumberId != 0)
			globalRecordId = recNumberId;
		if (globalRecordId >= 0 && validator != null) {
			int localRecordId = 0xffffff & globalRecordId;
			FolioObjectValidator source = (FolioObjectValidator) validator;
			recNotes = source.recordNotesForRecord(localRecordId);
			recHighs = source.highlightersForRecord(localRecordId);
		}
		int recHighlightersIndex = 0;
		VBHighlighterAnchor highAnch = null;
		if (recHighs != null)
			recHighs.anchorAtIndex(0);
		// NSLog("==> record_id (%d) notes (%p)", globalRecordId, recNotes);

		String text = recDict.getPlainText();
		int status = 0;
		int start = 0;
		int end = 0;
		int tagIdentified = 0;
		int returnAction = 0;
		
		dataObjectName = "";
		FlatFileTagString tag = new FlatFileTagString();
		//FDParaFormat paraStyles = new FDParaFormat();
		paraStyleRead = paraStyle;

		if (catchPwCounter == 0 && catchNtCounter == 0)
			acceptText = true;

		if (recNotes != null && recNotes.hasText() && globalRecordId >= 0) {
			target.setNoteIcon(true);
		}

		if (recDict.getLevelName() != null
				&& recDict.getLevelName().length() > 0) {
			setStyleName(recDict.getLevelName(), paraStyle, charFormatting);
		}

		char[] charArray = text.toCharArray();
		//StringBuilder sb = new StringBuilder();

		for (int i = 0; i < charArray.length; i++) {
			if (status == 0) {
				if (charArray[i] == '<') {
					status = 1;
				}
			} else if (status == 1) {
				if (charArray[i] == '<') {
					status = 0;
				} else {
					start = i - 1;
					status = 2;
				}
			} else if (status == 2) {
				if (charArray[i] == '>') {
					end = i;
					tagIdentified = 1;
					status = 0;
				} else if (charArray[i] == '"') {
					status = 3;
				}
			} else if (status == 3) {
				if (charArray[i] == '"') {
					status = 4;
				}
			} else if (status == 4) {
				if (charArray[i] == '"') {
					status = 3;
				} else if (charArray[i] == '>') {
					end = i;
					tagIdentified = 1;
					status = 0;
				}
			}

			if (tagIdentified == 1) {
				tagIdentified = 0;
				tag.clear();
				try {
					tag.appendString(text.substring(start, end + 1));
				} catch (Exception exception) {
					Log.i("conversion", String.format(
							"Invalid range: %d, %d in string %s", start, end
									- start + 1, text));
				} finally {
				}
				returnAction = processTag(tag, recDict);
				if (returnAction == ACTION_HR) {
					target.setParaFormatting(paraStyle);
					FDParagraph newPara = new FDParagraph();
					target.parts.add(newPara);
					paraStyle = new FDParaFormat();
				} else if (returnAction == ACTION_IGNOREREC) {
					acceptText = false;
					target.parts.clear();
					break;
				}
				//processTag(tag, target, styles, paraStyles, charStyles,recDict, pwLevel, pwParaStart, pwLinkStyle);
			} else if (status == 0) {
				while (highAnch != null && highAnch.startChar == charCounter) {
					charFormatting.setBackgroundColor(VBHighlighterAnchor.binColors[highAnch.highlighterId]);
					recHighlightersIndex++;
					highAnch = recHighs.anchorAtIndex(recHighlightersIndex);
				}

				if (acceptText) {
					if (Character.isWhitespace(charArray[i])) {
						processWord(wordBuilder);
						processSpace(false);
					} else {
						wordBuilder.append(charArray[i]);
					}
				}
				charCounter++;
			}
		}

		// copy of para formatting into last paragraph
		// because we know the paragraph formatting only at the end of paragraph
		// because para formatting tags can be wherever in the text
		target.setParaFormatting(paraStyle);

		if (acceptText) {
			processWord(wordBuilder);
		}
		
		/*
		 * } catch (Exception exception) { Log.e("exception",
		 * exception.getMessage()); target.clear();
		 * target.appendString(String.format("<p>Fail to load record %d",
		 * recDict.getRecord())); }
		 */

		target.loading = false;
		return target;
		
	}


	private void setStyleName(String levelName, FDParaFormat paraStyles, FDCharFormat charStyles) {

		FDTextFormat tf = null;
		if (folio != null) {
			tf = folio.getRawStyle(levelName);
		} else if (alternativeFormats != null) {
			tf = alternativeFormats.get(levelName);
		}

		if (tf != null) {
			paraStyles.copyFrom(tf.paraFormat);
			charStyles.copyFrom(tf.textFormat);
			origCharFormatting.copyFrom(tf.textFormat);
		}
	}


	private int processTag(FlatFileTagString tag, FolioTextRecord recordDict) {
		ArrayList<String> tagArr = tag.createArray();
		String str = tagArr.get(0);
		FDCharFormat charStyle = charFormatting;

		int returnAction = ACTION_NONE;
		
		//
		// first processing is for taga, which can influence levels of text
		//
		if (str.equals("PW")) {
			
			processWord(wordBuilder);
			
			hcPwCounter++;
			pwLevel.add(hcPwCounter);
			pwParaStart.add(hcParaStarted);
			hcParaStarted = false;
			acceptText = (hcPwCounter == catchPwCounter);
			pwLinkStyle.add(tagArr.size() > 2 ? tagArr.get(2) : "");
		} else if (str.equals("LT")) {
			
			processWord(wordBuilder);

			int restCount = 0;
			if (!pwLevel.empty())
				pwLevel.pop();
			if (!pwLevel.empty())
				restCount = pwLevel.pop();
			else
				restCount = 0;
			if (!pwParaStart.empty()) {
				hcParaStarted = pwParaStart.pop();
			}
			String classFormat = "Popup";
			if (!pwLinkStyle.empty()) {
				classFormat = pwLinkStyle.peek();
			}
			acceptText = (restCount == catchPwCounter);
			//checkParagraphStart(target, paraStyle);

			if (recordDict.getNamedPopup() != null) {

				currLink = new FDLink();
				currLink.type = "DP";
				currLink.link = String.format("vbase://inlinepopup/DP/%s/%d", recordDict
						.getNamedPopup(), hcPwCounter);
				pushCharStyle("LK_" + classFormat);
				/*target.appendString(String.format("<a class=\"LK_%s\" href=\"vbase://inlinepopup/DP/%s/%d\">",
								classFormat, FlatFileUtils.encodeLinkSafeString(recordDict
												.getNamedPopup()), hcPwCounter));*/
			} else {
				currLink = new FDLink();
				currLink.type = "PW";
				currLink.link = String.format("%d/%d", recordDict
						.getRecord(), hcPwCounter);
				pushCharStyle("LK_" + classFormat);
				/*target.appendString(String
						.format("<a class=\"LK_%s\" href=\"vbase://inlinepopup/RD/%d/%d\">",
								classFormat, recordDict.getRecord(),
								hcPwCounter));*/
			}
			if (!pwLinkStyle.empty()) {
				pwLinkStyle.pop();
			}
		} else if (str.equals("NT")) {
			processWord(wordBuilder);
			hcNtCounter++;
			acceptText = (hcNtCounter == catchNtCounter);
		} else if (str.equals("/NT")) {
			processWord(wordBuilder);
			hcNtCounter--;
			acceptText = (hcNtCounter == catchNtCounter);
		}

		//
		// if text is not accepted, then also tags are rejected to write
		//
		if (!acceptText) {
			return ACTION_NONE;
		}

		if (str.equals("ETH")) {
			// TODO eth bude ignorovany cely
			// namiesto toho bude len link do contents na dany record
			// ignoruje vsetko ostatne z tohoto recordu
			return ACTION_IGNOREREC;
		}
		
		if (str.equals("AUDIO")) {
			FDPartImage img = new FDPartImage();
			target.addElement(img);

			Bitmap bmp = getPredefinedBitmap(FDPartImage.IMAGE_AUDIO);
			if (bmp != null) {
				img.bitmap = bmp;
				img.desiredWidth = bmp.getWidth();
				img.desiredHeight = bmp.getHeight();
			}
			
			img.link = new FDLink();
			img.link.type = "DL";
			img.link.link = tagArr.get(2);
			
		}
		// extended para styles
		if (str.equals("PS")) {
			String safeString = FlatFileString.stringToSafe(tagArr.get(2), "PA");
			setStyleName(safeString, paraStyle, charFormatting);
		} else if (str.equals("LV")) {
			String safeString = FlatFileString.stringToSafe(tagArr.get(2), "LE");
			setStyleName(safeString, paraStyle, charFormatting);
		}

		// reading paragraph styles
		if (str.equals("AP")) {
			paraStyle.margins[FDParaFormat.SIDE_BOTTOM] = inchToPoints(tagArr.get(2));
		} else if (str.equals("BP")) {
			paraStyle.margins[FDParaFormat.SIDE_TOP] = inchToPoints(tagArr.get(2));
		} else if (str.equals("BR")) {
			startIndex = 2;
			readBorders(tagArr, paraStyle);
		} else if (str.equals("JU")) {
			paraStyle.align = alignFromString(tagArr.get(2));
		} else if (str.equals("LH")) {
			try {
				double v = Double.parseDouble(tagArr.get(2));
				paraStyle.lineHeight = (float)v;
			} catch (Exception e) {
			}
		} else if (str.equals("IN")) {
			startIndex = 2;
			readIndentFormating(tagArr, paraStyle);
		} else if (str.equals("SD")) {
			/*if (tagArr.size() == 1 || tagArr.get(2).equals("false")) {
				paraStyle.setValueForKey("background-color-x", "");
			} else {
				startIndex = 2;
				readColor(tagArr, "background-color-x", paraStyle);
			}*/
		} else if (str.equals("TS")) {
		}

		if (str.equals("BC")) {

		} else if (str.equals("BD-")) {
			processWord(wordBuilder);
			//origCharFormatting.setBold(charStyle.isBold());
			charStyle.setBold(false);
		} else if (str.equals("BD")) {
			processWord(wordBuilder);
			charStyle.setBold(origCharFormatting.isBold());
		} else if (str.equals("BD+")) {
			processWord(wordBuilder);
			//origCharFormatting.setBold(charStyle.isBold());
			charStyle.setBold(true);
		} else if (str.equals("UN-")) {
			processWord(wordBuilder);
//			prevCfUnderline = charStyle.isUnderline();
			charStyle.setUnderline(false);
		} else if (str.equals("UN")) {
			processWord(wordBuilder);
			charStyle.setUnderline(origCharFormatting.isUnderline());
		} else if (str.equals("UN+")) {
			processWord(wordBuilder);
//			prevCfUnderline = charStyle.isUnderline();
			charStyle.setUnderline(true);
		} else if (str.equals("SO-")) {
			processWord(wordBuilder);
			//prevCfStrikeOut = charStyle.isStrikeOut();
			charStyle.setStrikeOut(false);
		} else if (str.equals("SO")) {
			processWord(wordBuilder);
			charStyle.setStrikeOut(origCharFormatting.isStrikeOut());
		} else if (str.equals("SO+")) {
			processWord(wordBuilder);
			//prevCfStrikeOut = charStyle.isStrikeOut();
			charStyle.setStrikeOut(true);
		} else if (str.equals("HD-")) {
			processWord(wordBuilder);
			//prevCfHidden = charStyle.isHidden();
			charStyle.setHidden(false);
		} else if (str.equals("HD")) {
			processWord(wordBuilder);
			charStyle.setHidden(origCharFormatting.isHidden());
		} else if (str.equals("HD+")) {
			processWord(wordBuilder);
			//prevCfHidden = charStyle.isHidden();
			charStyle.setHidden(true);
		} else if (str.equals("CS")) {
			processWord(wordBuilder);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "CS"));
		} else if (str.equals("/CS")) {
			processWord(wordBuilder);
			popCharStyle();
		} else if (str.equals("FC")) {
			processWord(wordBuilder);
			if (tagArr.size() == 1 || tagArr.get(2).equals("DC")) {
				charStyle.setForegroundColor(origCharFormatting.getForegroundColor());
			} else {
				startIndex = 2;
				//prevCfForeColor = charStyle.getForegroundColor();
				charStyle.setForegroundColor(readColor(tagArr));
			}
		} else if (str.equals("FT")) {
			processWord(wordBuilder);
			if (tagArr.size() == 1) {
				charStyle.setFontName(origCharFormatting.getFontName());
			} else {
				String fontName = tagArr.get(2);
				//prevCfFontName = charStyle.getFontName();
				charStyle.setFontName(fontName);
			}
		} else if (str.equals("IT-")) {
			processWord(wordBuilder);
			//prevCfItalic = charStyle.isItalic();
			charStyle.setItalic(false);
		} else if (str.equals("IT")) {
			processWord(wordBuilder);
			charStyle.setItalic(origCharFormatting.isItalic());
		} else if (str.equals("IT+")) {
			processWord(wordBuilder);
			//prevCfItalic = charStyle.isItalic();
			charStyle.setItalic(true);
		} else if (str.equals("PN")) {
			processWord(wordBuilder);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "PD"));
		} else if (str.equals("/PN")) {
			processWord(wordBuilder);
			popCharStyle();
		} else if (str.equals("PT")) {
			processWord(wordBuilder);
			if (tagArr.size() == 1) {
				charStyle.setTextSize(origCharFormatting.getTextSize());
			} else {
				String ptSizeDescr = tagArr.get(2);
				if (ptSizeDescr.endsWith("pt"))
					ptSizeDescr = ptSizeDescr.substring(0,
							ptSizeDescr.length() - 2);
				if (!ptSizeDescr.equals("14")) {
					int ptSize = Integer.parseInt(ptSizeDescr);
					charStyle.setTextSize(ptSize);
					if (paraStyle.lineHeight < 1) {
						paraStyle.lineHeight = (float)1.2;
					}
				}
			}
		} else if (str.equals("SP")) {
			processWord(wordBuilder);
			charStyle.setSuperScript(true);
		} else if (str.equals("SB")) {
			processWord(wordBuilder);
			charStyle.setSubScript(true);
		} else if (str.equals("/SS")) {
			processWord(wordBuilder);
			charStyle.setSubScript(false);
			charStyle.setSuperScript(false);
		}

		//
		// tag for controlling
		//

		if (str.equals("CR")) {
			processWord(wordBuilder);
			processSpace(true);
		} else if (str.equals("HR")) {
			processWord(wordBuilder);
			returnAction = ACTION_HR;
		} else if (str.equals("HS")) {
			wordBuilder.append(" ");
		} else if (str.equals("OB")) {

			processWord(wordBuilder);
			
			//String ob_type = tagArr.get(2);
			String ob_name = tagArr.get(4);
			String ob_width = "";
			String ob_height = "";
			if (tagArr.size() > 6)
				ob_width = tagArr.get(6);
			if (tagArr.size() > 8)
				ob_height = tagArr.get(8);
			// NSMutableDictionary * form = [[NSMutableDictionary alloc]
			// initWithCapacity:10];
			//StringBuilder s = new StringBuilder();
			String objectExtension = (ob_name.lastIndexOf('.') >= 0 ? ob_name
					.substring(ob_name.lastIndexOf('.') + 1) : "");
			if (folio != null) {
				if (FlatFileUtils.isImageFileExtension(objectExtension)) {
					FDPartImage img = new FDPartImage();
					img.imageName = ob_name;
					img.desiredWidth = inchToPoints(ob_width);
					img.desiredHeight = inchToPoints(ob_height);
					target.addElement(img);
	
					//Log.i("draa", "Wants bitmap name: " + ob_name);
					Bitmap bmp = null;
					if (alternativeBitmaps.containsKey(ob_name)) {
						bmp = alternativeBitmaps.get(ob_name);
					} else {
						FolioObjectRecord ob = folio.findObject(ob_name, FolioObjectRecord.DATA_STREAM);
						if (ob != null) {
							bmp = BitmapFactory.decodeStream(ob.objectStream);
						}
					}
					if (bmp != null) {
						img.bitmap = bmp;
						img.desiredWidth = bmp.getWidth();
						img.desiredHeight = bmp.getHeight();
					}
					//Log.i("drawq", "bitmap with name " + ob_name + " height: " + img.desiredHeight);
				}
			}
		} else if (str.equals("QL") || str.equals("EN")) {
			processWord(wordBuilder);
			String query = tagArr.get(4);

			currLink = new FDLink();
			currLink.type = str;
			currLink.link = query;
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "LK"));

		} else if (str.equals("PX") || str.equals("REL")) {

			processWord(wordBuilder);
			currLink = new FDLink();
			currLink.type = str;
			currLink.link = tagArr.get(4);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "LK"));

		} else if (str.equals("DL") || str.equals("ML") || str.equals("PL")) {

			processWord(wordBuilder);
			dataObjectName = tagArr.get(4);

			currLink = new FDLink();
			currLink.type = str;
			currLink.link = tagArr.get(4);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "LK"));

		} else if (str.equals("WW")) {

			processWord(wordBuilder);
			currLink = new FDLink();
			currLink.type = str;
			currLink.link = tagArr.get(4);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "LK"));

		} else if (str.equals("/DL") || str.equals("/ML") || str.equals("EL")
				|| str.equals("/EN") || str.equals("/JL") || str.equals("/PX")
				|| str.equals("/OL") || str.equals("/PL") || str.equals("/QL")
				|| str.equals("/PW") || str.equals("/WW")) {
			
			if (currLink != null) {
				Log.i("link", "type:" + currLink.type + " link:" + currLink.link);
			}
			processWord(wordBuilder);
			currLink = null;
			popCharStyle();
			
		} else if (str.equals("JL")) {
			
			processWord(wordBuilder);
			currLink = new FDLink();
			currLink.type = str;
			currLink.link = tagArr.get(4);
			pushCharStyle(FlatFileString.stringToSafe(tagArr.get(2), "LK"));

		} else if (str.equals("RO")) {
			processWord(wordBuilder);
			FDTable table = null;
			FDTableRow row = new FDTableRow();
			table = getLastSafeTable();		
			table.addRow(row);
			
		} else if (str.equals("TB")) {
			
			processWord(wordBuilder);
			FDPartSpace sp = new FDPartSpace();
			sp.link = getCurrentLink();
			sp.format = getCurrentFormat();
			sp.tab = true;
			sp.backgroundColor = getCurrentBackground();
			target.addElement(sp);
			
		} else if (str.equals("TA")) {
			
			processWord(wordBuilder);
			FDTable table = new FDTable();
			target.parts.add(table);
			if (tagArr.size() > 2) {
				int counts = 0;
				if (tagArr.get(2).equals(";") || tagArr.get(2).equals(","))
				{
					startIndex = 3;
					readParaFormating(tagArr, table.paraFormat);
				}
				else
				{
					int idx = 2;
					int widthIdx = 0;
					Integer i;
					i = Integer.getInteger(tagArr.get(idx), -1);
					if (i > -1) {
						counts = i.intValue();
						idx+=2;
						while(widthIdx < counts) {
							Float f = Float.parseFloat(tagArr.get(idx));
							table.columnWidths.add(f);
							widthIdx++;
							idx+=2;
						}
					} else {
						idx = 2;
					}
					startIndex = idx;
					readParaFormating(tagArr, table.paraFormat);
				}
			}

		} else if (str.equals("CE")) {
			
			processWord(wordBuilder);
			FDTable table = getLastSafeTable();
			FDTableCell cell = new FDTableCell();
			table.addCell(cell);
			
		} else if (str.equals("/CE")) {

			processWord(wordBuilder);
			FDTableCell cell = getLastTableCell();
			if (cell != null) {
				cell.closed = true;
			}
		} else if (str.equals("/TA")) {
			processWord(wordBuilder);
			FDTable table = getLastTable();
			table.closed = true;
		}
		
		
		return returnAction;
	}

	private SparseArray<Bitmap> predefinedBitmaps = new SparseArray<Bitmap>();
	private Bitmap getPredefinedBitmap(int imageId) {
		
		Bitmap bmp = predefinedBitmaps.get(imageId);
		if (bmp != null)
			return bmp;
		
		switch(imageId) {
		case FDPartImage.IMAGE_AUDIO:
			bmp = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.audio_icon);
			break;
		}
		
		if (bmp != null) {
			predefinedBitmaps.append(imageId, bmp);
		}
		return bmp;
	}


	public float percentValue(String value) {
		float d = 1;
		try {
			d = Float.parseFloat(value);
			if (d < 0.3) {
				d = 1;
			}
		} catch (Exception e) {
		}
		return d;
	}

	private void readParaFormating(ArrayList<String> arrTag, FDParaFormat paraStyle) {

		while (startIndex < arrTag.size()) {
			String tag = arrTag.get(startIndex);
			if (tag.equals("AP")) {
				paraStyle.margins[FDParaFormat.SIDE_BOTTOM] = inchToPoints(arrTag.get(startIndex + 2));
				startIndex += 4;
			} else if (tag.equals("BP")) {
				paraStyle.margins[FDParaFormat.SIDE_TOP] = inchToPoints(arrTag.get(startIndex + 2));
				startIndex += 4;
			} else if (tag.equals("JU")) {
				paraStyle.align = alignFromString(arrTag.get(startIndex + 2));
				startIndex += 4;
			} else if (tag.equals("SD")) {
				startIndex += 2;
				paraStyle.backgroundColor = readColor(arrTag);
			} else if (tag.equals("LH")) {
				paraStyle.lineHeight = inchToPoints(arrTag.get(startIndex + 2)) / 14;
				startIndex += 4;
			} else if (tag.equals("LS")) {
				paraStyle.lineHeight = percentValue(arrTag.get(startIndex + 2));
				startIndex += 4;
			} else if (tag.equals("IN")) {
				startIndex += 2;
				readIndentFormating(arrTag, paraStyle);
			} else if (tag.equals("BR")) {
				startIndex += 2;
				readBorders(arrTag, paraStyle);
			} else {
				while (startIndex < arrTag.size() && arrTag.get(startIndex).equals(";") == false) {
					startIndex++;
				}
			}

		}
		
	}


	private FDTableCell getLastTableCell() {
		FDTable table = getLastTable();
		FDTableCell cell = null;
		if (table != null) {
			cell = table.getLastCell();
		}
		return cell;
	}


	public FDTable getLastTable() {
		FDTable table;
		if (target.parts.size() == 0) {
			return null;
		} else {
			if (target.getLastPart() instanceof FDTable) {
				table = (FDTable)target.getLastPart();
				if (table.closed)
					return null;
			} else {
				return null;
			}
		}
		return table;
	}


	public FDTable getLastSafeTable() {
		FDTable table = getLastTable();
		if (table == null) {
			table = new FDTable();
			target.parts.add(table);
		}
		return table;
	}


	private void popCharStyle() {
		if (charStyleStack.size() > 0) {
			FDCharFormat fes = charStyleStack.pop();
			charFormatting.copyFrom(fes);
		}
		
		if (origCharStyleStack.size() > 0) {
			FDCharFormat oes = origCharStyleStack.pop();
			origCharFormatting.copyFrom(oes);
		}
	}


	private void pushCharStyle(String stringToSafe) {
		
		charStyleStack.push(charFormatting.clone());
		origCharStyleStack.push(origCharFormatting.clone());

		FDTextFormat tf = null;
		if (folio != null) {
			tf = folio.getRawStyle(stringToSafe);
		} else if (alternativeFormats != null) {
			tf = alternativeFormats.get(stringToSafe);
		}
		if (tf != null && tf.textFormat != null) {
			charFormatting.overloadFrom(tf.textFormat);
			origCharFormatting.copyFrom(tf.textFormat);
		}
	}


	private void processSpace(boolean breakLine) {
		FDPartSpace sp = new FDPartSpace();
		sp.desiredWidth = -1;
		sp.format = getCurrentFormat();
		//sp.formatSelection = null;
		sp.link = getCurrentLink();
		sp.breakLine = breakLine;
		sp.backgroundColor = getCurrentBackground();

		target.addElement(sp);
	}


	private int getCurrentBackground() {
		if (charFormatting.getBackgroundColor() != 0) {
			return charFormatting.getBackgroundColor();
		}
		return paraStyle.backgroundColor;
	}


	private FDLink getCurrentLink() {
		return currLink;
	}


	private FDPaint getCurrentFormat() {
		
		String hash = charFormatting.getHash();
		if (charFormattingMap.containsKey(hash))
			return charFormattingMap.get(hash);
		
		FDPaint pt = charFormatting.getPaint();
		charFormattingMap.put(hash, pt);
		return pt;
	}


	private void processWord(StringBuilder string) {

		if (string.length() > 0) {
			FDPartString ps = new FDPartString();
			ps.format = getCurrentFormat();
			ps.link = getCurrentLink();
			ps.text = string.toString();
			ps.backgroundColor = getCurrentBackground();
			ps.hidden = charFormatting.isHidden();
	
			target.addElement(ps);
			
			string.delete(0, string.length());
		}
	}

	public int sideIndexFromAbbr(String side) {
		if (side.equals("AL"))
			return FDParaFormat.SIDE_ALL;
		if (side.equals("LF"))
			return FDParaFormat.SIDE_LEFT;
		if (side.equals("RT"))
			return FDParaFormat.SIDE_RIGHT;
		if (side.equals("BT"))
			return FDParaFormat.SIDE_BOTTOM;
		if (side.equals("TP"))
			return FDParaFormat.SIDE_TOP;
		if (side.equals("VT"))
			return FDParaFormat.SIDE_LEFTRIGHT;
		if (side.equals("HZ"))
			return FDParaFormat.SIDE_TOPBOTTOM;
		return FDParaFormat.SIDE_ALL;
	}
	
	public float inchToPoints(String value) {
	
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

	public void readBorders(ArrayList<String> arrTag, FDParaFormat obj) {
		String side;
		int postfix = 0;
		float value;

		float strWidth = 0;
		int strColor = 0;

		while (startIndex < arrTag.size()) {
			strWidth = 0;
			strColor = 0;
			side = arrTag.get(startIndex);
			if (side.equals(";"))
				return;
			postfix = sideIndexFromAbbr(side);
			startIndex += 2;

			// read width
			strWidth = inchToPoints(arrTag.get(startIndex));
			paraStyle.borderWidth[postfix] = strWidth;
			startIndex += 2;

			// read padding
			value = inchToPoints(arrTag.get(startIndex));
			if (value > 0) {
				paraStyle.padding[postfix] = value;
			}
			
			// test end of tag
			startIndex += 1;
			if (startIndex >= arrTag.size()) {
				return;
			}
			startIndex += 1;
			
			// read color
			side = arrTag.get(startIndex);
			if (side.equals("FC")) {
				startIndex += 2;
				strColor = readColor(arrTag);
				startIndex += 1;
			} else {
				strColor = 0;
			}
			paraStyle.borderColor[postfix] = strColor;
		}

	}

	public int readColor(ArrayList<String> tagArr) {
		int vr, vg, vb;
		String str;
		int strColor = 0;

		if (startIndex < tagArr.size()) {
			str = tagArr.get(startIndex);
			if (str.equals("DC") || str.equals("NO")) {
				startIndex += 1;
				return 0;
			}
			vr = Integer.parseInt(str);
			startIndex += 2;
			vg = Integer.parseInt(tagArr.get(startIndex));
			startIndex += 2;
			vb = Integer.parseInt(tagArr.get(startIndex));

			strColor = (vr << 16) | (vg << 8) | vb | 0xff000000;
			startIndex += 2;
			
			//Log.i("drawe-fc", String.format("readColor(%s,%s,%s) = %d", tagArr.get(startIndex - 6), tagArr.get(startIndex - 4),
			//		tagArr.get(startIndex-2), strColor));
			if (startIndex >= tagArr.size())
				return strColor;
			if (tagArr.get(startIndex).equals("DC")) {
				startIndex += 1;
			} else {
				startIndex -= 1;
			}
		}
		return strColor;
	}
	
	public int alignFromString(String str) {
		int a = FDParaFormat.ALIGN_JUST;
		if (str.equals("CN"))
			a = FDParaFormat.ALIGN_CENTER;
		if (str.equals("RT"))
			a = FDParaFormat.ALIGN_RIGHT;
		if (str.equals("FL"))
			a = FDParaFormat.ALIGN_JUST;
		if (str.equals("CA"))
			a = FDParaFormat.ALIGN_LEFT;
		return a;
	}

	public void readIndentFormating(ArrayList<String> arrTag, FDParaFormat paraStyles) {
		String str;
		String paramName = "ml";
		float f;

		f = inchToPoints(arrTag.get(startIndex));
		if (f == 0) {
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			while (arrTag.size() > startIndex) {
				str = arrTag.get(startIndex);
				paramName = str;

				startIndex += 2;
				if (arrTag.size() <= startIndex
						|| arrTag.get(startIndex).equals(";")) {
					return;
				}
				f = inchToPoints(arrTag.get(startIndex));
				if (paramName.equals("LF")) {
					paraStyles.margins[FDParaFormat.SIDE_LEFT] = f;
				} else if (paramName.equals("RT")) {
					paraStyles.margins[FDParaFormat.SIDE_RIGHT] = f;
				} else if (paramName.equals("FI")) {
					paraStyles.firstIndent = f;
				}
				startIndex += 1;
				if (arrTag.size() <= startIndex
						|| arrTag.get(startIndex).equals(";")) {
					return;
				}
				startIndex += 1;
			}
		} else {
			paraStyles.margins[FDParaFormat.SIDE_LEFT] = f;
			startIndex += 1;
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			startIndex += 1;
			f = inchToPoints(arrTag.get(startIndex));
			paraStyles.margins[FDParaFormat.SIDE_RIGHT] = f;
			startIndex += 1;
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			startIndex += 1;
			f = inchToPoints(arrTag.get(startIndex));
			paraStyles.firstIndent = f;
			return;
		}

		return;
	}

	public static void setFontMultiplyer(float multi) {
		FDCharFormat.setMultiplyFontSize(multi);
		if (charFormattingMap != null) {
			for (FDPaint pt : charFormattingMap.values()) {
				pt.setTextSize(pt.originalFontSize * multi);
			}
		}
	}

	public static void setDefaultFont(String fontName) {
		FDTypeface.setDefaultFontName(fontName);
		if (charFormattingMap != null) {
			for (FDPaint pt : charFormattingMap.values()) {
				if (pt.generalizedFontFace) {
					pt.setDefaultTypeface();
				}
			}
		}
	}
	
	public static String getDefaultFont() {
		return FDTypeface.getDefaultFontName();
	}
}
