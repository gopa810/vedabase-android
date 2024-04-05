package com.gopalapriyadasa.textabase_engine;

import java.nio.ByteBuffer;

public class VBFolioRecordStream {

	public int baseRecordId = 0;
	public ByteBuffer buffer = null;
	
	
	public void setBytes(byte [] bytes) {
		
		buffer = ByteBuffer.wrap(bytes);
		buffer.order(VBFolioQueryOperatorStream.byteOrder);
	}
	
}
