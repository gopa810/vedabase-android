package com.gopalapriyadasa.textabase_engine;

public class FolioQueryInstance extends FolioQueryTemplate {

	public String data = "";

    // scope of search
    // 0 - whole database
    // 1 - current book
    // 2 - current article
	public int scopeIndex = 0;

	public String getFinalQuery() {
		
		if (pattern.contains("$1")) {
			return pattern.replace("$1", data);
		} else {
			return pattern.length() > 0 ? pattern : data;
		}
	}
}
