package com.gopalapriyadasa.textabase_engine;

import java.io.InputStream;

public class FolioObjectRecord {

	public static int DATA_NONE = 0;
	public static int DATA_BYTES = 1;
	public static int DATA_STREAM = 2;
	
	public String objectName = null;
	public String objectType = null;
	public InputStream objectStream = null;
	public byte[] objectData = null;
}
