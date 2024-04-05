package com.gopalapriyadasa.textabase_engine;

import android.annotation.SuppressLint;
import android.graphics.Color;


public class FDCharFormat {

	public static final int PM_TEXTSIZE = 0x0001;
	public static final int PM_FONTNAME = 0x0002;
	public static final int PM_BACKGROUNDCOLOR = 0x0004;
	public static final int PM_BOLD = 0x0008;
	public static final int PM_HIDDEN = 0x0010;
	public static final int PM_UNDERLINE = 0x0020;
	public static final int PM_STRIKEOUT = 0x0040;
	public static final int PM_FOREGROUNDCOLOR = 0x0080;
	public static final int PM_ITALIC = 0x0100;
	public static final int PM_SUPER = 0x0200;
	public static final int PM_SUB = 0x0400;
	

	private int changed = 0x0;
	private boolean forcedFontName = false;
	private float textSize = 14;
	private String fontName = "Times";
	private int backgroundColor = 0;
	private boolean bold = false;
	private boolean hidden = false;
	private boolean underline = false;
	private boolean strikeOut = false;
	private int foregroundColor = Color.BLACK;
	private boolean italic = false;
	private boolean superScript = false;
	private boolean subScript = false;
	
	private static float multiplyFontSize = 1.5f;
	private static float multiplySpaces = 1.2f;

	public static float getMultiplyFontSize() {
		return multiplyFontSize;
	}

	public static void setMultiplyFontSize(float multiplyFontSize) {
		FDCharFormat.multiplyFontSize = multiplyFontSize;
	}

	public static float getMultiplySpaces() {
		return multiplySpaces;
	}

	public static void setMultiplySpaces(float multiplySpaces) {
		FDCharFormat.multiplySpaces = multiplySpaces;
	}


	public int getChanged() {
		return changed;
	}


	public void setChanged(int changed) {
		this.changed = changed;
	}


	public float getTextSize() {
		return textSize;
	}


	public void setTextSize(float textSize) {
		changed |= PM_TEXTSIZE;
		this.textSize = textSize;
	}


	public String getFontName() {
		return fontName;
	}


	public void setFontName(String fontName) {
		changed |= PM_FONTNAME;
		this.fontName = fontName;
	}


	public int getBackgroundColor() {
		return backgroundColor;
	}


	public void setBackgroundColor(int backgroundColor) {
		changed |= PM_BACKGROUNDCOLOR;
		this.backgroundColor = backgroundColor;
	}


	public boolean isBold() {
		return bold;
	}


	public void setBold(boolean bold) {
		changed |= PM_BOLD;
		this.bold = bold;
	}


	public boolean isHidden() {
		return hidden;
	}


	public void setHidden(boolean hidden) {
		changed |= PM_HIDDEN;
		this.hidden = hidden;
	}


	public boolean isUnderline() {
		return underline;
	}


	public void setUnderline(boolean underline) {
		changed |= PM_UNDERLINE;
		this.underline = underline;
	}


	public boolean isStrikeOut() {
		return strikeOut;
	}


	public void setStrikeOut(boolean strikeOut) {
		changed |= PM_STRIKEOUT;
		this.strikeOut = strikeOut;
	}


	public int getForegroundColor() {
		return foregroundColor;
	}


	public void setForegroundColor(int foregroundColor) {
		changed |= PM_FOREGROUNDCOLOR;
		this.foregroundColor = foregroundColor;
	}


	public boolean isItalic() {
		return italic;
	}


	public void setItalic(boolean italic) {
		changed |= PM_ITALIC;
		this.italic = italic;
	}


	public boolean isSuperScript() {
		return superScript;
	}


	public void setSuperScript(boolean superScript) {
		changed |= PM_SUPER;
		this.superScript = superScript;
	}


	public boolean isSubScript() {
		return subScript;
	}


	public void setSubScript(boolean subScript) {
		changed |= PM_SUB;
		this.subScript = subScript;
	}


	public FDCharFormat() {
		changed = 0;
	}


	@SuppressLint("DefaultLocale")
	public String getHash() {
		return String.format("%f_%s_%d_%c%c%c%c%c%c%c", getTextSize(), getFontName(), getForegroundColor(),
				isBold() ? 'Y' : 'N', isItalic() ? 'Y' : 'N', isHidden() ? 'Y' : 'N', isStrikeOut() ? 'Y' : 'N', 
				isUnderline() ? 'Y' : 'N', isSubScript() ? 'Y' : 'N', isSuperScript() ? 'Y' : 'N');
	}


	public FDPaint getPaint() {

		FDPaint paint = new FDPaint();


		paint.setColor(getForegroundColor());
		paint.setStrikeThruText(isStrikeOut());
		if (forcedFontName)
			paint.generalizedFontFace = false;
		else
			paint.generalizedFontFace = FDTypeface.isGeneralFontName(fontName);
		paint.bold = bold;
		paint.italic = italic;
		if (paint.generalizedFontFace)
			paint.setDefaultTypeface();
		else
			paint.setTypeface(FDTypeface.getTypeface(fontName, bold, italic));
		paint.setUnderlineText(isUnderline());
		paint.setTextSize(textSize* getMultiplyFontSize());
		paint.originalFontSize = textSize;
		paint.bkgColor = getBackgroundColor();
		
		return paint;
	}

	public FDCharFormat copyFrom(FDCharFormat fce) {
		
		this.textSize = fce.textSize;
		this.fontName = fce.fontName;
		this.backgroundColor = fce.backgroundColor;
		this.bold = fce.bold;
		this.hidden = fce.hidden;
		this.underline = fce.underline;
		this.strikeOut = fce.strikeOut;
		this.foregroundColor = fce.foregroundColor;
		this.italic = fce.italic;
		this.superScript = fce.superScript;
		this.subScript = fce.subScript;
		this.forcedFontName = fce.forcedFontName;
		
		return this;
	}
	
	public boolean checkChange(int property)
	{
		return (changed & property) > 0;
	}
	
	public void overloadFrom(FDCharFormat cf) {
		if (cf.checkChange(PM_TEXTSIZE)) {
			setTextSize(cf.getTextSize());
		}
		if (cf.checkChange(PM_BACKGROUNDCOLOR)) {
			setBackgroundColor(cf.getBackgroundColor());
		}
		if (cf.checkChange(PM_BOLD)) {
			setBold(cf.isBold());
		}
		if (cf.checkChange(PM_FONTNAME)) {
			setFontName(cf.getFontName());
		}
		if (cf.checkChange(PM_FOREGROUNDCOLOR)) {
			setForegroundColor(cf.getForegroundColor());
		}
		if (cf.checkChange(PM_HIDDEN)) {
			setHidden(cf.isHidden());
		}
		if (cf.checkChange(PM_ITALIC)) {
			setItalic(cf.isItalic());
		}
		if (cf.checkChange(PM_STRIKEOUT)) {
			setStrikeOut(cf.isStrikeOut());
		}
		if (cf.checkChange(PM_SUB)) {
			setSubScript(cf.isSubScript());
		}
		if (cf.checkChange(PM_SUPER)) {
			setSuperScript(cf.isSuperScript());
		}
		if (cf.checkChange(PM_TEXTSIZE)) {
			setTextSize(cf.getTextSize());
		}
		if (cf.checkChange(PM_UNDERLINE)) {
			setUnderline(cf.isUnderline());
		}
	}
	
	public FDCharFormat clone() {
		FDCharFormat fce = new FDCharFormat();
		return fce.copyFrom(this);
	}


	public void setFontName(String string, boolean b) {
		setFontName(string);
		forcedFontName = b;
	}
}
