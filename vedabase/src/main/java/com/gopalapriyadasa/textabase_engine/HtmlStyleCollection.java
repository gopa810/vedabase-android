package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;
import java.util.List;

public class HtmlStyleCollection {

	protected List<HtmlStyle> styles;
	
	public HtmlStyleCollection() {
		styles = new ArrayList<HtmlStyle>();
	}
	
	public HtmlStyleCollection add(HtmlStyle style) {
	
		styles.add(style);
		return this;
	}
	
	public String substitutionFontName(String fname) {
		if (fname.equals("Sanskrit-Helvetica"))
			return "Helvetica";
		if (fname.startsWith("Sanskrit-"))
			return "Times";
		
		// this is when converting to Unicode Vedabase
		if (fname.equals("ScaHelvetica") || fname.equals("ScaOptima"))
			return "Helvetica";
		if (fname.startsWith("Sca"))
			return "Times";
		if (fname.equals("Balaram") || fname.equals("Dravida"))
			return "Times";
		if (fname.equals("scagoudy"))
			return "Times";
		
		return fname;
		
	}
	
	public String getMIMEType(String str) {
		if (str.equals("mp3file"))
			return "audio/mpeg";
		if (str.equals("AcroExch.Document"))
			return "application/pdf";
		return str;
	}
}
