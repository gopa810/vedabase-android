package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.util.Log;

public class FlatFileString {

	private static boolean dataLinkAsButton = true;

	StringBuilder _buffer;
	boolean hcParaStarted;
	boolean hcSpanStarted;
	boolean hcSup;
	boolean hcSupChanged;
	boolean hcSub;
	boolean hcSubChanged;
	boolean linkStarted;
	boolean buttonStarted;
	boolean ethDefaultExpanded;
	int hcPwCounter;
	int hcNtCounter;
	int hcTableRows;
	int hcTableColumns;
	int catchPwLevel;
	int catchPwCounter;
	int catchNtCounter;
	String ethStyle;
	String ethListImage;
	FlatFileContentItem ethDict;
	Stack<FlatFileContentItem> ethStack;
	String dataObjectName;
	HtmlStyle paraStyleRead;
	Object validator = null;

	public FlatFileString() {
		_buffer = new StringBuilder();
		ethStack = new Stack<FlatFileContentItem>();
		ethDict = new FlatFileContentItem();
		ethDefaultExpanded = false;
	}

	public void reset() {
		hcParaStarted = false;
		hcSpanStarted = false;
		hcSub = false;
		hcSup = false;
		hcSupChanged = false;
		hcSubChanged = false;
		hcPwCounter = 0;
		hcNtCounter = 0;
		hcTableRows = 0;
		hcTableColumns = 0;
		catchPwLevel = 0;
		catchPwCounter = 0;
		catchNtCounter = 0;
		linkStarted = false;
		buttonStarted = false;
	}

	public boolean isHcParaStarted() {
		return hcParaStarted;
	}

	public void setHcParaStarted(boolean hcParaStarted) {
		this.hcParaStarted = hcParaStarted;
	}

	public boolean isHcSpanStarted() {
		return hcSpanStarted;
	}

	public void setHcSpanStarted(boolean hcSpanStarted) {
		this.hcSpanStarted = hcSpanStarted;
	}

	public boolean isHcSup() {
		return hcSup;
	}

	public void setHcSup(boolean hcSup) {
		this.hcSup = hcSup;
	}

	public boolean isHcSupChanged() {
		return hcSupChanged;
	}

	public void setHcSupChanged(boolean hcSupChanged) {
		this.hcSupChanged = hcSupChanged;
	}

	public boolean isHcSub() {
		return hcSub;
	}

	public void setHcSub(boolean hcSub) {
		this.hcSub = hcSub;
	}

	public boolean isHcSubChanged() {
		return hcSubChanged;
	}

	public void setHcSubChanged(boolean hcSubChanged) {
		this.hcSubChanged = hcSubChanged;
	}

	public boolean isLinkStarted() {
		return linkStarted;
	}

	public void setLinkStarted(boolean linkStarted) {
		this.linkStarted = linkStarted;
	}

	public boolean isButtonStarted() {
		return buttonStarted;
	}

	public void setButtonStarted(boolean buttonStarted) {
		this.buttonStarted = buttonStarted;
	}

	public boolean isEthDefaultExpanded() {
		return ethDefaultExpanded;
	}

	public void setEthDefaultExpanded(boolean ethDefaultExpanded) {
		this.ethDefaultExpanded = ethDefaultExpanded;
	}

	public int getHcPwCounter() {
		return hcPwCounter;
	}

	public void setHcPwCounter(int hcPwCounter) {
		this.hcPwCounter = hcPwCounter;
	}

	public int getHcNtCounter() {
		return hcNtCounter;
	}

	public void setHcNtCounter(int hcNtCounter) {
		this.hcNtCounter = hcNtCounter;
	}

	public int getHcTableRows() {
		return hcTableRows;
	}

	public void setHcTableRows(int hcTableRows) {
		this.hcTableRows = hcTableRows;
	}

	public int getHcTableColumns() {
		return hcTableColumns;
	}

	public void setHcTableColumns(int hcTableColumns) {
		this.hcTableColumns = hcTableColumns;
	}

	public int getCatchPwLevel() {
		return catchPwLevel;
	}

	public void setCatchPwLevel(int catchPwLevel) {
		this.catchPwLevel = catchPwLevel;
	}

	public int getCatchPwCounter() {
		return catchPwCounter;
	}

	public void setCatchPwCounter(int catchPwCounter) {
		this.catchPwCounter = catchPwCounter;
	}

	public int getCatchNtCounter() {
		return catchNtCounter;
	}

	public void setCatchNtCounter(int catchNtCounter) {
		this.catchNtCounter = catchNtCounter;
	}

	public String getEthStyle() {
		return ethStyle;
	}

	public void setEthStyle(String ethStyle) {
		this.ethStyle = ethStyle;
	}

	public String getEthListImage() {
		return ethListImage;
	}

	public void setEthListImage(String ethListImage) {
		this.ethListImage = ethListImage;
	}

	public String getDataObjectName() {
		return dataObjectName;
	}

	public void setDataObjectName(String dataObjectName) {
		this.dataObjectName = dataObjectName;
	}

	public HtmlStyle getParaStyleRead() {
		return paraStyleRead;
	}

	public void setParaStyleRead(HtmlStyle paraStyleRead) {
		this.paraStyleRead = paraStyleRead;
	}

	public Object getValidator() {
		return validator;
	}

	public void setValidator(Object validator) {
		this.validator = validator;
	}

	public static boolean isDataLinkAsButton() {
		return dataLinkAsButton;
	}

	public static void setDataLinkAsButton(boolean dataLinkAsButton) {
		FlatFileString.dataLinkAsButton = dataLinkAsButton;
	}

	public FlatFileString setString(String str) {
		_buffer.delete(0, _buffer.length());
		_buffer.append(str);
		return this;
	}

	@Override
	public String toString() {
		return _buffer.toString();
	}

	public void checkParagraphStart(HtmlStringBuilder target,
			HtmlStyle paraStyle) {
		if (hcParaStarted == false) {
			hcParaStarted = true;

			target.appendString("\n");
			target.appendString(paraStyle.htmlTextForTag("p"));
		}
	}

	public void processChar(char chr, HtmlStringBuilder target,
			HtmlStyle paraStyle, HtmlStyleTracker charStyle) {
		checkParagraphStart(target, paraStyle);

		// finishing previous formating tags
		if (hcSubChanged && !hcSub) {
			target.appendString("</sub>");
		}
		if (hcSupChanged && !hcSup) {
			target.appendString("</sup>");
		}
		if (charStyle.hasChanges()) {
			if (hcSpanStarted) {
				target.appendString("</span>");
				hcSpanStarted = false;
			}
		}

		// starting new formating tags
		if (charStyle.hasChanges()) {
			String spanText = charStyle.htmlTextForTag("span");
			if (!spanText.equals("<span>")) {
				target.appendString(spanText);
				hcSpanStarted = true;
			}
			charStyle.clearChanges();
		}
		if (hcSupChanged && hcSup) {
			target.appendString("<sup>");
		}
		if (hcSubChanged && hcSub) {
			target.appendString("<sub>");
		}
		hcSubChanged = false;
		hcSupChanged = false;
		target.addCharacter(chr);
	}

	public void finishHtmlFormating(HtmlStringBuilder target,
			HtmlStyle paraStyle, HtmlStyleTracker charStyle) {
		if (hcSub) {
			target.appendString("</sub>");
			hcSub = false;
		}
		if (hcSup) {
			target.appendString("</sup>");
			hcSup = false;
		}
		if (hcSpanStarted) {
			target.appendString("</span>");
			hcSpanStarted = false;
		}

		target.appendString("</p>");
		hcParaStarted = false;
	}

	public String[] sideTextFromAbbr(String side) {
		if (side.equals("AL"))
			return new String[] { "", "" };
		if (side.equals("LF"))
			return new String[] { "-left", "" };
		if (side.equals("RT"))
			return new String[] { "-right", "" };
		if (side.equals("BT"))
			return new String[] { "-bottom", "" };
		if (side.equals("TP"))
			return new String[] { "-top", "" };
		if (side.equals("VT"))
			return new String[] { "-top", "-bottom", "" };
		if (side.equals("HZ"))
			return new String[] { "-right", "-left", "" };
		return null;
	}

	@SuppressLint("DefaultLocale")
	public String inchToPoints(String value) {
		if (value.endsWith("pt"))
			return value;
		try {
			double d = Double.parseDouble(value);
			return String.format("%dpt", (int) (d * 72.0));
		} catch (Exception e) {
			return null;
		}
	}

	public int startIndex = 0;

	private static int gEthCounter;

	public String readColor(ArrayList<String> tagArr) {
		int vr, vg, vb;
		String str;
		String strColor = "";

		if (startIndex < tagArr.size()) {
			str = tagArr.get(startIndex);
			if (str.equals("DC") || str.equals("NO")) {
				startIndex += 1;
				return "";
			}
			vr = Integer.parseInt(str);
			startIndex += 2;
			vg = Integer.parseInt(tagArr.get(startIndex));
			startIndex += 2;
			vb = Integer.parseInt(tagArr.get(startIndex));

			strColor = String.format("#%02x%02x%02x", vr, vg, vb);
			startIndex += 2;
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

	public void readBorders(ArrayList<String> arrTag, HtmlStyle obj) {
		String side;
		String[] postfix = null;
		String value;

		String strWidth = "";
		String strStyle = "";
		String strColor = "";

		while (startIndex < arrTag.size()) {
			strWidth = "0";
			strStyle = "solid";
			strColor = "";
			side = arrTag.get(startIndex);
			if (side.equals(";"))
				return;
			postfix = sideTextFromAbbr(side);
			if (postfix == null) {
				startIndex -= 1;
				return;
			}
			// NSLog("postifx: %@\n", postfix);
			startIndex += 2;
			strWidth = inchToPoints(arrTag.get(startIndex));
			// if (value) {
			// strWidth = value;
			// [obj setObject:value forKey:String.format("border%@-width",
			// postfix]];
			// }
			startIndex += 2;
			value = inchToPoints(arrTag.get(startIndex));
			if (value != null) {
				for (String postfixitem : postfix) {
					obj.setValueForKey(String.format("padding%", postfixitem),
							value);
				}
			}
			startIndex += 1;
			if (startIndex >= arrTag.size())
				return;
			startIndex += 1;
			value = arrTag.get(startIndex);
			if (value.equals("FC")) {
				startIndex += 2;
				strColor = readColor(arrTag);
				startIndex += 1;
			} else {
				strColor = "";
			}
			// String temp1 = String.format("%@ %@ %", strWidth, strStyle,
			// strColor];
			// String temp2 = String.format("border%", postfix];
			// NSLog("key: (%@) value:(%@)", temp2, temp1);
			for (String item : postfix) {
				obj.setValueForKey(String.format("border%s-width", item),
						strWidth);
				obj.setValueForKey(String.format("border%s-style", item),
						strStyle);
				obj.setValueForKey(String.format("border%s-color", item),
						strColor);
			}
		}

	}

	public String alignFromString(String str) {
		String a = "left";
		if (str.equals("CN"))
			a = "center";
		if (str.equals("RT"))
			a = "right";
		if (str.equals("FL"))
			a = "justify";
		if (str.equals("CA"))
			a = "left";
		return a;
	}

	public void readIndentFormating(ArrayList<String> arrTag, HtmlStyle obj) {
		String str;
		String paramName = "margin-left";

		str = inchToPoints(arrTag.get(startIndex));
		if (str == null) {
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			while (arrTag.size() > startIndex) {
				str = arrTag.get(startIndex);
				if (str.equals("LF"))
					paramName = "margin-left";
				else if (str.equals("RT"))
					paramName = "margin-right";
				else if (str.equals("FI"))
					paramName = "text-indent";
				else {
					startIndex -= 1;
					return;
				}
				;
				startIndex += 2;
				if (arrTag.size() <= startIndex
						|| arrTag.get(startIndex).equals(";")) {
					return;
				}
				str = inchToPoints(arrTag.get(startIndex));
				obj.setValueForKey(paramName, str);
				startIndex += 1;
				if (arrTag.size() <= startIndex
						|| arrTag.get(startIndex).equals(";")) {
					return;
				}
				startIndex += 1;
			}
		} else {
			obj.setValueForKey("margin-left", str);
			startIndex += 1;
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			startIndex += 1;
			str = inchToPoints(arrTag.get(startIndex));
			obj.setValueForKey("margin-right", str);
			startIndex += 1;
			if (arrTag.size() <= startIndex
					|| arrTag.get(startIndex).equals(";")) {
				return;
			}
			startIndex += 1;
			str = inchToPoints(arrTag.get(startIndex));
			obj.setValueForKey("text-indent", str);
			return;
		}

		return;
	}

	public void readColor(ArrayList<String> tagArr, String prefix, HtmlStyle obj) {
		int vr, vg, vb;
		String str;

		if (startIndex < tagArr.size()) {
			str = tagArr.get(startIndex);
			if (str.equals("DC") || str.equals("NO")) {
				startIndex += 1;
				return;
			}
			vr = Integer.parseInt(str);
			startIndex += 2;
			vg = Integer.parseInt(tagArr.get(startIndex));
			startIndex += 2;
			vb = Integer.parseInt(tagArr.get(startIndex));

			obj.setValueForKey(prefix,
					String.format("#%02x%02x%02x", vr, vg, vb));
			startIndex += 2;
			if (startIndex >= tagArr.size())
				return;
			if (tagArr.get(startIndex).equals("DC")) {
				startIndex += 1;
			} else {
				startIndex -= 1;
			}
		}
	}

	//
	// converts string to safe form which can be used as name of CSS style
	// this function is primary used for conversion of non-uniform names of
	// styles
	// in FFF file, into their canonical safe form
	// all non-letters are replaced by _{d} string, where {d} is their ASCII
	// code value
	//
	public static String stringToSafe(String str, String tag) {
		byte[] bytes = str.getBytes();
		StringBuilder result = new StringBuilder();

		result.append(tag);
		result.append("_");

		for (byte bi : bytes) {
			if (Character.isLetter(bi)) {
				result.append(String.format("%c", bi));
			} else if (bi == ' ') {
				result.append("_");
			} else {
				result.append(String.format("_%d", bi));
			}
		}

		return result.toString();
	}

	//
	// conversion of number from range 0.0 .. 1.0
	// into percentage from range 0% - 100%
	// both numbers (input / output) are in the form of NSString-s
	//
	@SuppressLint("DefaultLocale")
	public String percentValue(String value) {
		double d;
		try {
			d = Double.parseDouble(value);
			if (d > 0.3) {
				return String.format("%d%%", (int) (d * 100.0));
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	public void readParaFormating(ArrayList<String> arrTag, HtmlStyle obj) {
		String value = "";
		for (int i = startIndex; i < arrTag.size(); i++) {
			String tag = arrTag.get(i);
			if (tag.equals("AP")) {
				value = inchToPoints(arrTag.get(i + 2));
				obj.setValueForKey("margin-bottom", value);
				i += 2;
			} else if (tag.equals("BP")) {
				value = inchToPoints(arrTag.get(i + 2));
				obj.setValueForKey("margin-top", value);
				i += 2;
			} else if (tag.equals("JU")) {
				value = alignFromString(arrTag.get(i + 2));
				obj.setValueForKey("text-align", value);
				i += 2;
			} else if (tag.equals("SD")) {
				i += 2;
				readColor(arrTag, "background-color", obj);
			} else if (tag.equals("LH")) {
				value = inchToPoints(arrTag.get(i + 2));
				obj.setValueForKey("line-height", value);
				i += 2;
			} else if (tag.equals("LS")) {
				value = percentValue(arrTag.get(i + 2));
				obj.setValueForKey("line-height", value);
				i += 2;
			} else if (tag.equals("IN")) {
				i += 2;
				readIndentFormating(arrTag, obj);
			} else if (tag.equals("BR")) {
				i += 2;
				readBorders(arrTag, obj);
			} else {
				while (i < arrTag.size() && arrTag.get(i).equals(";") == false) {
					i++;
				}
			}

		}
	}

	public void finishEtlStarted(HtmlStringBuilder target) {
		if (ethDict != null && ethDict.isEtlStarted()) {
			target.appendString("\n</table>\n");
			ethDict.setEtlStarted(false);
		}
	}

	public String fullPathStylistImage(String file) {
		return String.format("vbase://assets/%s", file);
	}

	public String getObjectMIMEType(String ob_type, String ob_name) {
		return "image/png";
	}

	public void processTag(FlatFileTagString tag, HtmlStringBuilder target,
			HtmlStyleCollection styles, HtmlStyle paraStyle,
			HtmlStyleTracker charStyle, FolioTextRecord recordDict,
			Stack<Integer> pwLevel, Stack<Boolean> pwParaStart,
			Stack<String> pwLinkStyle) {
		ArrayList<String> tagArr = tag.createArray();
		String str = tagArr.get(0);

		//
		// first processing is for taga, which can influence levels of text
		//
		if (str.equals("PW")) {
			hcPwCounter++;
			pwLevel.add(hcPwCounter);
			pwParaStart.add(hcParaStarted);
			hcParaStarted = false;
			target.setAcceptText(hcPwCounter == catchPwCounter);
			pwLinkStyle.add(tagArr.size() > 2 ? tagArr.get(2) : "");
		} else if (str.equals("LT")) {
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
			target.setAcceptText(restCount == catchPwCounter);
			checkParagraphStart(target, paraStyle);

			if (recordDict.getNamedPopup() != null) {
				target.appendString(String.format(Locale.getDefault(),
						"<a class=\"LK_%s\" href=\"vbase://inlinepopup/DP/%s/%d\">",
						classFormat,
						FlatFileUtils.encodeLinkSafeString(recordDict.getNamedPopup()), hcPwCounter));
				linkStarted = true;
			} else {
				target.appendString(String.format(Locale.getDefault(),
								"<a class=\"LK_%s\" href=\"vbase://inlinepopup/RD/%d/%d\">",
								classFormat, recordDict.getRecord(),
								hcPwCounter));
				// NSLog("record id = %", [recordDict valueForKey:"record"));
				linkStarted = true;
			}
			if (!pwLinkStyle.empty()) {
				pwLinkStyle.pop();
			}
		} else if (str.equals("NT")) {
			hcNtCounter++;
			target.setAcceptText(hcNtCounter == catchNtCounter);
		} else if (str.equals("/NT")) {
			hcNtCounter--;
			target.setAcceptText(hcNtCounter == catchNtCounter);
		}

		//
		// if text is not accepted, then also tags are rejected to write
		//
		if (!target.isAcceptText()) {
			return;
		}

		if (str.equals("ETH")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			paraStyle.clear();
			charStyle.clearChanges();
			String ethArg = "";
			ethDict = new FlatFileContentItem();
			ethStack.push(ethDict);
			ethArg = (tagArr.size() >= 3) ? tagArr.get(2)
					: "cont_book_open.png";
			ethDict.setA(ethArg);
			ethArg = (tagArr.size() >= 5) ? tagArr.get(4)
					: "cont_book_closed.png";
			ethDict.setB(ethArg);
			gEthCounter++;
			ethDict.setC(String.format("ethimg%d", gEthCounter));
			ethDict.setD(String.format("eth_%d", gEthCounter));
			target.appendString("<table style='font-family:Helvetica;font-size:14pt;text-align:left'>");
			target.appendString(String
					.format("<tr><td><img id='%s' src='vbase://assets/%s' style='cursor:pointer;' onclick=\"eth_show_hide('%s');eth_expand('%s', '%s', '%s');\"></td><td>",
							ethDict.getC(),
							(ethDefaultExpanded ? ethDict.getA() : ethDict
									.getB()), ethDict.getD(), ethDict.getC(),
							ethDict.getA(), ethDict.getB()));

		} else if (str.equals("ETB")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			finishEtlStarted(target);
			target.appendString(String.format(
					"</td></tr><tr><td></td><td id='%s' style='display:%s;'>",
					ethDict.getD(), (ethDefaultExpanded ? "block" : "none")));
		} else if (str.equals("/ETH")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			paraStyle.clear();
			charStyle.clearChanges();
			finishEtlStarted(target);
			if (!ethStack.empty()) {
				if (ethStack.size() > 0)
					ethStack.pop();
				if (ethStack.size() > 0) {
					ethDict = ethStack.peek();
				} else {
					ethDict = null;
				}
			}
			target.appendString("</td></tr></table>");
			ethStyle = "";

		} else if (str.equals("ETL")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			paraStyle.clear();
			charStyle.clearChanges();
			ethListImage = (tagArr.size() >= 3) ? tagArr.get(2)
					: "cont_text.png";
			if (ethDict.isEtlStarted()) {
				target.appendString("</td></tr>");
			} else {
				target.appendString("<table style='font-size:14pt;' cellpadding=4>");
			}
			target.appendString("<tr>");
			target.appendString(String
					.format("<td width=20 valign=top><img src='vbase://assets/%s'></td><td>",
							ethListImage));
			ethDict.setEtlStarted(true);
		} else if (str.equals("ETX")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			paraStyle.clear();
			charStyle.clearChanges();
			if (ethDict.isEtlStarted()) {
				target.appendString("</td></tr>");
			} else {
				target.appendString("<table style='font-size:14pt;'>");
			}
			target.appendString("<tr>");
			target.appendString("<td valign=top colspan=2>");
			ethDict.setEtlStarted(true);
		} else if (str.equals("/ETL")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			paraStyle.clear();
			charStyle.clearChanges();
			finishEtlStarted(target);
		} else if (str.equals("ETS")) {
			if (tagArr.size() >= 3) {
				ethStyle = tagArr.get(2);
			} else {
				ethStyle = "";
			}
		}

		// extended para styles
		if (str.equals("PS")) {
			String safeString = stringToSafe(tagArr.get(2), "PA");
			paraStyle.setStyleName(safeString);
		} else if (str.equals("LV")) {
			String safeString = stringToSafe(tagArr.get(2), "LE");
			paraStyle.setStyleName(safeString);
		}

		// reading paragraph styles
		if (str.equals("AP")) {
			paraStyle.setValueForKey("margin-bottom",
					String.format("%sin", tagArr.get(2)));
		} else if (str.equals("BP")) {
			paraStyle.setValueForKey("margin-top",
					String.format("%sin", tagArr.get(2)));
		} else if (str.equals("BR")) {
			startIndex = 2;
			readBorders(tagArr, paraStyle);
		} else if (str.equals("JU")) {
			paraStyle.setValueForKey("text-align",
					alignFromString(tagArr.get(2)));
		} else if (str.equals("LH")) {
			try {
				double v = Double.parseDouble(tagArr.get(2));
				paraStyle.setValueForKey("line-height",
						String.format(":%f%%", v * 100.0));
			} catch (Exception e) {
			}
		} else if (str.equals("IN")) {
			startIndex = 2;
			readIndentFormating(tagArr, paraStyle);
		} else if (str.equals("SD")) {
			if (tagArr.size() == 1 || tagArr.get(2).equals("false")) {
				paraStyle.setValueForKey("background-color-x", "");
			} else {
				startIndex = 2;
				readColor(tagArr, "background-color-x", paraStyle);
			}
		} else if (str.equals("TS")) {
		}

		if (str.equals("BC")) {
			if (tagArr.size() == 1 || tagArr.get(2).equals("DC")) {
				charStyle.setValueForKey("background-color-x", "");
			} else {
				startIndex = 2;
				readColor(tagArr, "background-color-x", charStyle);
			}
		} else if (str.equals("BD-")) {
			charStyle.setValueForKey("font-weight", "normal");
		} else if (str.equals("BD")) {
			charStyle.setValueForKey("font-weight", "");
		} else if (str.equals("BD+")) {
			charStyle.setValueForKey("font-weight", "bold");
		} else if (str.equals("UN-")) {
			charStyle.setValueForKey("text-decoration", "none");
		} else if (str.equals("UN")) {
			charStyle.setValueForKey("text-decoration", "");
		} else if (str.equals("UN+")) {
			charStyle.setValueForKey("text-decoration", "underline");
		} else if (str.equals("SO-")) {
			charStyle.setValueForKey("text-decoration", "none");
		} else if (str.equals("SO")) {
			charStyle.setValueForKey("text-decoration", "");
		} else if (str.equals("SO+")) {
			charStyle.setValueForKey("text-decoration", "line-through");
		} else if (str.equals("HD-")) {
			charStyle.setValueForKey("visibility", "visible");
		} else if (str.equals("HD")) {
			charStyle.setValueForKey("visibility", "");
		} else if (str.equals("HD+")) {
			charStyle.setValueForKey("visibility", "hidden");
		} else if (str.equals("CS")) {
			charStyle.setStyleName(stringToSafe(tagArr.get(2), "CS"));
		} else if (str.equals("/CS")) {
			charStyle.setStyleName("");
		} else if (str.equals("FC")) {
			if (tagArr.size() == 1 || tagArr.get(2).equals("DC")) {
				charStyle.setValueForKey("color", "");
			} else {
				startIndex = 2;
				readColor(tagArr, "color", charStyle);
			}
		} else if (str.equals("FT")) {
			if (tagArr.size() == 1) {
				charStyle.setValueForKey("font-family", "");
			} else {
				String fontName = tagArr.get(2);
				if (styles != null) {
					charStyle.setValueForKey("font-family",
							styles.substitutionFontName(fontName));
				} else {
					charStyle.setValueForKey("font-family", fontName);
				}
			}
		} else if (str.equals("IT-")) {
			charStyle.setValueForKey("font-style", "normal");
		} else if (str.equals("IT")) {
			charStyle.setValueForKey("font-style", "");
		} else if (str.equals("IT+")) {
			charStyle.setValueForKey("font-style", "italic");
		} else if (str.equals("PN")) {
			charStyle.setStyleName(stringToSafe(tagArr.get(2), "PD"));
		} else if (str.equals("/PN")) {
			charStyle.setStyleName("");
		} else if (str.equals("PT")) {
			if (tagArr.size() == 1) {
				charStyle.setValueForKey("font-size", "");
			} else {
				String ptSizeDescr = tagArr.get(2);
				if (ptSizeDescr.endsWith("pt"))
					ptSizeDescr = ptSizeDescr.substring(0,
							ptSizeDescr.length() - 2);
				if (!ptSizeDescr.equals("14")) {
					int ptSize = Integer.parseInt(ptSizeDescr);
					charStyle.setValueForKey("font-size",
							String.format("%d%%", ptSize * 100 / 14));
					String lineHeight = charStyle.valueForKey("line-height"); 
					if (lineHeight == null || lineHeight.isEmpty()) {
						charStyle.setValueForKey("line-height", "120%");
					}
				}
			}
		} else if (str.equals("SP")) {
			hcSup = true;
			hcSupChanged = true;
		} else if (str.equals("SB")) {
			hcSub = true;
			hcSubChanged = true;
		} else if (str.equals("/SS")) {
			if (hcSub) {
				hcSub = false;
				hcSubChanged = true;
			}
			if (hcSup) {
				hcSup = false;
				hcSupChanged = true;
			}
		}

		//
		// tag for controlling
		//

		if (str.equals("CR")) {
			target.appendString("<br>");
		} else if (str.equals("HR")) {
			hcParaStarted = false;
		} else if (str.equals("HS")) {
			target.appendString("&nbsp;");
		} else if (str.equals("OB")) {
			String ob_type = tagArr.get(2);
			String ob_name = tagArr.get(4);
			String ob_width = "";
			String ob_height = "";
			if (tagArr.size() > 6)
				ob_width = tagArr.get(6);
			if (tagArr.size() > 8)
				ob_height = tagArr.get(8);
			// NSMutableDictionary * form = [[NSMutableDictionary alloc]
			// initWithCapacity:10];
			StringBuilder s = new StringBuilder();
			String objectExtension = (ob_name.lastIndexOf('.') >= 0 ? ob_name
					.substring(ob_name.lastIndexOf('.') + 1) : "");
			checkParagraphStart(target, paraStyle);
			if (FlatFileUtils.isImageFileExtension(objectExtension)) {
				s.append(String.format("<img src=\"http://objects/%s\"",
						FlatFileUtils.encodeLinkSafeString(ob_name)));
				if (ob_width != " && ob_height != ") {
					s.append(String.format(" width=\"%s\" height=\"%s\"",
							inchToPoints(ob_width), inchToPoints(ob_height)));
				}
				s.append(">");
			} else {
				s.append(String.format("<object data=\"http://objects/%s\"",
						FlatFileUtils.encodeLinkSafeString(ob_name)));
				if (!ob_type.isEmpty()) {
					s.append(String.format(" type=\"%s\"",
							getObjectMIMEType(ob_type, ob_name)));
				}
				if (ob_width != " && ob_height != ") {
					s.append(String.format(" width=%s height=%s",
							inchToPoints(ob_width), inchToPoints(ob_height)));
				}

				s.append("></object>");
			}
			target.appendString(s.toString());
			return;
		} else if (str.equals("QL") || str.equals("EN")) {
			String query = tagArr.get(4);
			target.appendString(String.format(
					"<a class=\"%s\" href=\"vbase://links/%s/%s\">",
					stringToSafe(tagArr.get(2), "LK"), str,
					FlatFileUtils.encodeLinkSafeString(query)));
			linkStarted = true;
			// NSLog("ORIGINAL QUERY: %@\nNEW QUERY: %@\n-------------------",
			// query, [FlatFileUtils encodeLinkSafeString:query]);
		} else if (str.equals("PX")) {
			checkParagraphStart(target, paraStyle);
			target.appendString(String.format(
					"<a class=\"%s\" href=\"vbase://popup/%s\">",
					stringToSafe(tagArr.get(2), "LK"),
					FlatFileUtils.encodeLinkSafeString(tagArr.get(4))));
			linkStarted = true;
		} else if (str.equals("DL") || str.equals("ML") || str.equals("PL")) {
			checkParagraphStart(target, paraStyle);
			dataObjectName = tagArr.get(4);
			if (dataLinkAsButton) {
				target.appendString(String
						.format("<input style=\"font-size:100%%\" type=\"button\" name=\"b1\" onclick=\"location.href='vbase://links/%s/%s'\" value=\"",
								str, FlatFileUtils
										.encodeLinkSafeString(dataObjectName)));
				buttonStarted = true;
			} else {
				target.appendString(String.format(
						"<a class=\"%s\" href=\"vbase://links/%s/%s\">",
						stringToSafe(tagArr.get(2), "LK"), str,
						FlatFileUtils.encodeLinkSafeString(dataObjectName)));
				linkStarted = true;
			}
		} else if (str.equals("WW")) {
			checkParagraphStart(target, paraStyle);
			target.appendString(String.format("<a class=\"%s\" href=\"%s\">",
					stringToSafe(tagArr.get(2), "LK"), tagArr.get(4)));
			linkStarted = true;
		} else if (str.equals("/DL") || str.equals("/ML") || str.equals("EL")
				|| str.equals("/EN") || str.equals("/JL") || str.equals("/PX")
				|| str.equals("/OL") || str.equals("/PL") || str.equals("/QL")
				|| str.equals("/PW") || str.equals("/WW")) {
			if (linkStarted) {
				target.appendString("</a>");
				linkStarted = false;
			} else if (buttonStarted) {
				target.appendString("\">");
				buttonStarted = false;
			}
		} else if (str.equals("JL")) {
			if (tagArr.size() > 4) {
				String s2 = tagArr.get(4);
				if (!s2.isEmpty()) {
					if (validator != null
							&& ((FolioObjectValidator) validator)
									.jumpExists(s2)) {
						target.appendString("<a href=\"vbase://links/JL/");
						target.appendString(FlatFileUtils
								.encodeLinkSafeString(s2));
						target.appendString("\">");
						linkStarted = true;
					} else {
						charStyle.setValueForKey("color", "#909090");
					}
				}
			}
		} else if (str.equals("RO")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			target.appendString("<tr>");
			hcTableRows++;
			hcTableColumns = 0;
		} else if (str.equals("TB")) {
			target.appendString("  &nbsp;&nbsp;&nbsp; ");
		} else if (str.equals("TA")) {
			finishHtmlFormating(target, paraStyle, charStyle);
			StringBuilder tableTag = new StringBuilder();
			tableTag.append("<table");
			if (tagArr.size() > 2) {
				HtmlStyle dict = new HtmlStyle();
				int counts = Integer.parseInt(tagArr.get(2));
				if (counts > 0) {
					startIndex = (4 + counts * 2);
					readParaFormating(tagArr, dict);
				} else {
					startIndex = 2;
					readParaFormating(tagArr, dict);
				}
				tableTag.append(" style='");
				tableTag.append(dict.styleCssText());
				tableTag.append("'>");
			} else {
				tableTag.append(">");
			}

			target.appendString(tableTag.toString());
			hcTableRows = 0;
			hcTableColumns = 0;
		} else if (str.equals("CE")) {
			hcTableColumns++;
			target.appendString("<td>");
		} else if (str.equals("/CE")) {
			target.appendString("</td>");
		} else if (str.equals("/TA")) {
			target.appendString("</table>");
		}

	}

	public HtmlStringBuilder htmlStringWithStyles(HtmlStyleCollection styles,
			FolioTextRecord recDict) {
		HtmlStringBuilder target = new HtmlStringBuilder();
		return this.htmlStringWithStyles(styles, recDict, target);
	}

	@SuppressLint("DefaultLocale")
	public HtmlStringBuilder htmlStringWithStyles(HtmlStyleCollection styles,
			FolioTextRecord recDict, HtmlStringBuilder target) {
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

		String text = _buffer.toString();
		int status = 0;
		int start = 0;
		int end = 0;
		int tagIdentified = 0;
		dataObjectName = "";
		FlatFileTagString tag = new FlatFileTagString();
		HtmlStyle paraStyles = new HtmlStyle();
		paraStyleRead = paraStyles;
		HtmlStyleTracker charStyles = new HtmlStyleTracker();
		Stack<Integer> pwLevel = new Stack<Integer>();
		Stack<Boolean> pwParaStart = new Stack<Boolean>();
		Stack<String> pwLinkStyle = new Stack<String>();

		target.clear();

		if (catchPwCounter == 0 && catchNtCounter == 0)
			target.setAcceptText(true);

		if (recNotes != null && recNotes.hasText() && globalRecordId >= 0) {
			target.appendString(String
					.format("<a href=\"vbase://note/%d\"><img src=\"vbase://assets/note_icon.png\" style='float:left;margin:8pt' height=40 width=40 border=0></a>",
							globalRecordId));
		}

		if (recDict.getLevelName() != null
				&& recDict.getLevelName().length() > 0 && catchNtCounter == 0 && catchNtCounter == 0) {
			paraStyles.setStyleName(recDict.getLevelName());
		}

		char[] charArray = text.toCharArray();

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
				processTag(tag, target, styles, paraStyles, charStyles,
						recDict, pwLevel, pwParaStart, pwLinkStyle);
			} else if (status == 0) {
				while (highAnch != null && highAnch.startChar == charCounter) {
					charStyles.setValueForKey("background",
							VBHighlighterAnchor.colors[highAnch.highlighterId]);
					recHighlightersIndex++;
					highAnch = recHighs.anchorAtIndex(recHighlightersIndex);
				}

				processChar(charArray[i], target, paraStyles, charStyles);
				charCounter++;
			}
		}

		finishHtmlFormating(target, paraStyles, charStyles);
		finishEtlStarted(target);

		// NSLog("=============== record %@ ============================\n%@\n-----------------------------------------",
		// [recDict valueForKey:"record"), [target string]);

		Integer firstPara = target.findTag("p");
		if (firstPara >= 0) {
			firstPara += 2;
			int recordNumber = recDict.getRecord();
			if (recordNumber != 0) {
				String strId = String.format(" id=\"rec%d\" ", recordNumber);
				target.insertString(strId, firstPara);
			}
		}
		/*
		 * } catch (Exception exception) { Log.e("exception",
		 * exception.getMessage()); target.clear();
		 * target.appendString(String.format("<p>Fail to load record %d",
		 * recDict.getRecord())); }
		 */

		return target;
	}

}
