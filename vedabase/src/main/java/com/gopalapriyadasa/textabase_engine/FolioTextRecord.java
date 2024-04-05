package com.gopalapriyadasa.textabase_engine;

/**
 * @author Gopa702
 *
 */
public class FolioTextRecord {

	private int record;
	private String plainText;
	private String levelName;
	protected String namedPopup = null;
	
	
	public final int getRecord() {
		return record;
	}
	public final void setRecord(int record) {
		this.record = record;
	}
	public final String getPlainText() {
		return plainText;
	}
	public final void setPlainText(String plainText) {
		this.plainText = plainText;
	}
	public final String getLevelName() {
		return levelName;
	}
	public final void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	
	public String getNamedPopup() {
		return namedPopup;
	}
	public void setNamedPopup(String namedPopup) {
		this.namedPopup = namedPopup;
	}
	
	/**
	 * Returns HTML string as a result of conversion from plain flat format
	 * to HTML format.
	 * This is wrapper function to generateHtmlText()
	 * 
	 * @return
	 */
	public String getHtmlText() {
		HtmlStringBuilder builder = new HtmlStringBuilder();
		
		generateHtmlText(builder, null);
		
		return builder.toString();
	}

	/**
	 * Performs conversion from plan flat format
	 * to HTML format.
	 * 
	 * @return
	 */
	private void generateHtmlText(HtmlStringBuilder builder, HtmlStyleCollection styles) {

		FlatFileString flat = new FlatFileString();
		flat.reset();
		flat.setString(getPlainText());
		flat.htmlStringWithStyles(styles, this, builder);
	}
}
