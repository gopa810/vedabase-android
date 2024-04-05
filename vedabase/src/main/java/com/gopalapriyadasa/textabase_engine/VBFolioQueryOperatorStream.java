package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.nio.ByteOrder;
import java.util.ArrayList;

public class VBFolioQueryOperatorStream extends VBFolioQueryOperator {

	private ArrayList<VBFolioRecordStream> streams = null;
	VBFolioRecordStream currentStream = null;
	int streamsPosition = 0;
	int position = 0;
	String word = "";
	
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	

	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "WORD stream (" + word + ")");
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level,sb);
		sb.append("word <BD+>" + word + "<BD->     " + recordHits + " hits<CR>");
	}

	@Override
	public int getCurrentRecord()
	{
	    if (streams == null || streamsPosition >= streams.size() || position >= currentStream.buffer.capacity()) {
	        setEOF(true);
	    	return INVALID_RECORD;
	    }
	    
	    return currentStream.baseRecordId + currentStream.buffer.getInt(position);
	}

	@Override
	public short getCurrentProximity()
	{
	    if (streams == null || streamsPosition >= streams.size() || position >= currentStream.buffer.capacity()) {
	        setEOF(true);
	    	return 0;
	    }
		
	    return currentStream.buffer.getShort(position + 4);
	}

	@Override
	public boolean gotoNextRecord()
	{
		if (isEOF() || currentStream == null)
			return false;

	    int rec = getCurrentRecord();
	    
	    while (rec == getCurrentRecord())
	    {
	        if(!gotoNextProximity()) {
	        	return false;
	        }
	    }

		recordHits++;
	    return true;
	}

	@Override
	public boolean gotoNextProximity()
	{
		if (isEOF() || currentStream == null)
			return false;
		
	    position += 6;
	    
	    while (position >= currentStream.buffer.capacity()) {
	    	streamsPosition++;
		    if (streamsPosition >= streams.size()) {
		    	setEOF(true);
		    	return false;
		    }
		    currentStream = streams.get(streamsPosition);
		    position = 0;
	    }
	    
	    return true;
	}

	public ArrayList<VBFolioRecordStream> getStreams() {
		return streams;
	}

	public void setStreams(ArrayList<VBFolioRecordStream> streams) {
		this.streams = streams;
		streamsPosition = 0;
	    if (streamsPosition >= streams.size()) {
	    	currentStream = null;
	    	setEOF(true);
	    } else {
			currentStream = streams.get(streamsPosition);
			position = 0;
		}
	}


	
}
