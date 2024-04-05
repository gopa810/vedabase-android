package com.gopalapriyadasa.textabase_engine;

import java.util.HashMap;

import com.gopalapriyadasa.bhaktivedanta_textabase.MainActivity;

import android.graphics.Typeface;
import android.util.Log;

public class FDTypeface {
	
	public final static String TIMES_FONT = "Times";
	public final static String ARIAL_FONT = "Arial"; 
	
	private static String defaultFontName = TIMES_FONT;
	
	public static HashMap<String,Typeface> publicTypefaces = new HashMap<String,Typeface>();
	
	public static Typeface getTypeface(String fontName, boolean bold, boolean italic) {
		Log.i("typf", "fontname: " + fontName);
		String path = fontNameToPath(fontName, bold, italic);
		Log.i("typf", "path: " + path);
		if (publicTypefaces.containsKey(path))
			return publicTypefaces.get(path);
		
		Typeface tf = Typeface.createFromAsset(MainActivity.getInstance().getAssets(), path);
		publicTypefaces.put(path, tf);
		
		return tf;
	}

	private static String fontNameToPath(String fontName, boolean bold, boolean italic) {
		
		fontName = correctFontName(fontName);
		
		if (fontName.equals("RM Devanagari")) {
			return "assets/RMDEVA.TTF";
		} else if (fontName.equals("Inbenb")) {
			return "assets/inbenb11.ttf";
		} else if (fontName.equals("Inbenr")) {
			return "assets/inbenr11.ttf";
		} else if (fontName.equals("Indevr")) {
			return "assets/INDEVR20.TTF";
		} else if (fontName.equals("Inbeni")) {
			return "assets/inbeni11.ttf";
		} else if (fontName.equals("Inbeno")) {
			return "assets/inbeno11.ttf";
		} else if (fontName.equals(ARIAL_FONT)) {
			if (bold) {
				if (italic) {
					return "assets/vuArialPlusBoldItalic.ttf";
				}
				else {
					return "assets/vuArialPlusBold.ttf";
				}
			} else if (italic) {
				return "assets/vuArialPlusItalic.ttf";
			} else {
				return "assets/vuArialPlus.ttf";
			}
		} else if (fontName.equals(TIMES_FONT)) {
			if (bold) {
				if (italic) {
					return "assets/vuTimesPlusBoldItalic.ttf";
				}
				else {
					return "assets/vuTimesPlusBold.ttf";
				}
			} else if (italic) {
				return "assets/vuTimesPlusItalic.ttf";
			} else {
				return "assets/vuTimesPlus.ttf";
			}
		}
		
		return "assets/vuTimesPlus.ttf";
	}
	
	private static String correctFontName(String fontName) {
		if (fontName == null) {
			return getDefaultFontName();
		}
		if (fontName.equals("RM Devanagari")) {
			return fontName;
		} else if (fontName.equals("Inbenb")) {
			return fontName;
		} else if (fontName.equals("Inbeno")) {
			return fontName;
		} else if (fontName.equals("Inbeni")) {
			return fontName;
		} else if (fontName.equals("Inbenr")) {
			return fontName;
		} else if (fontName.equals("Indevr")) {
			return fontName;
		} else if (fontName.equals("Inbenb")) {
			return fontName;
		} else if (fontName.equals("Helv")) {
			return ARIAL_FONT;
		} else if (fontName.equals(ARIAL_FONT)) { 
			return ARIAL_FONT;
		} else if (fontName.equals(TIMES_FONT)) {
			return TIMES_FONT;
		} else {
			return getDefaultFontName();
		}
	}

	public static boolean isGeneralFontName(String fontName) {

		if (fontName.equals("RM Devanagari")) {
			return false;
		} else if (fontName.equals("Inbenb")) {
			return false;
		} else if (fontName.equals("Inbeni")) {
			return false;
		} else if (fontName.equals("Inbeno")) {
			return false;
		} else if (fontName.equals("Inbenr")) {
			return false;
		} else if (fontName.equals("Indevr")) {
			return false;
		} else if (fontName.equals("Inbenb")) {
			return false;
		}
		
		return true;
	}

	public static Typeface getDefaultTypeface(boolean bold, boolean italic) {
		return getTypeface(getDefaultFontName(), bold, italic);
	}

	public static String getDefaultFontName() {
		return defaultFontName;
	}

	public static void setDefaultFontName(String defaultFontName) {
		FDTypeface.defaultFontName = defaultFontName;
	}
}
