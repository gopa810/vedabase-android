package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorQuote extends  VBFolioQueryOperator {

	
	ArrayList<VBFolioQueryOperator> items = new ArrayList<VBFolioQueryOperator>();
	private int maxRecordValidate = 0;

	@Override
	public int getCurrentRecord()
	{
	    if (!isValid())
	        validate();
	    if (!isValid() || isEOF())
	        return 0;
	    
	    return items.get(0).getCurrentRecord();
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "QUOTE operator");
		for (VBFolioQueryOperator OP : items) {
			OP.printTree(level+1);
		}
	}


	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("group Quote (order is important)       " + recordHits + " hits <CR>");
		for (VBFolioQueryOperator OP : items) {
			OP.printTreeEx(level+1, sb);
		}
	}

	@Override
	public void flushRecordHits() {
		super.flushRecordHits();
		for (VBFolioQueryOperator OP : items) {
			OP.flushRecordHits();
		}
	}

	@Override
	public boolean gotoNextRecord()
	{
	    if (!isValid())
	        validate();
	    if (!isValid() || isEOF())
	        return false;
	    
	    
	    if (items.get(0).gotoNextRecord() == false)
	    {
	        setEOF(true);
	        return false;
	    }
	    
	    setValid(false);
	    
	    validate();
	    if (!isValid() || isEOF())
	        return false;

		recordHits++;
	    return true;
	}

	@Override
	public void validate()
	{
	    if (isValid())
	        return;
	    
	    if (items.size() == 0)
	    {
	        setValid(false);
	        return;
	    }
	    for(VBFolioQueryOperator oper : items)
	    {
	        oper.validate();
	    }
	    
	    maxRecordValidate = items.get(0).getCurrentRecord();
	    boolean bChange = true;
	    
	    while (bChange)
	    {
	        bChange = false;
	        
	        bChange = alignStreams(bChange);
	        
	        if (isEOF())
	        	return;
	        
	        if (bChange)
	        {
	            // not all streams has the same record id
	            // so trying to align all streams by moving them
	            // to the highest record id retrieved
	            if (!alignStreamsToRecord(maxRecordValidate))
	            	return;            
	        }
	        else 
	        {
	            bChange = compareProximities(bChange);
	        }
	    }
	    
	    setValid(true);
	}

	public boolean alignStreams(boolean bChange) {
		int rec;
		for(VBFolioQueryOperator oper : items) 
		{
		    rec = oper.getCurrentRecord();
		    if (rec != maxRecordValidate)
		        bChange = true;
		    if (rec > maxRecordValidate)
		        maxRecordValidate = rec;
		    if (oper.isEOF())
		    {
		        setEOF(true);
		        break;
		    }
		}
		return bChange;
	}

	public boolean compareProximities(boolean bChange) {
		// here all streams points to the same record id
		// so we have to check proximity
		short prox = 0;
		short npro;
		boolean bInit = false;
		int indexToMove = 0;
		
		// check proximity
		// go through all streams
		// and check if current stream is +1 toward previous stream
		for (VBFolioQueryOperator stream : items) 
		{
		    if (bInit == false)
		    {
		        prox = stream.getCurrentProximity();
		        bInit = true;
		    }
		    else 
		    {
		        // not in the line with previous stream
		        // change is: goto next promity in previous
		        // stream
		        // we break the loop here, so the index of previous stream
		        // is stored in indexToMove variable
		        npro = stream.getCurrentProximity();
		        if (prox + 1 != npro)
		        {
		            bChange = true;
		            break;
		        }
		        prox = npro;
		    }
		    indexToMove++;
		    
		}
		
		// we have found item which does not corresponds with
		// previous proximity
		// so we have to move that previous proximity
		// a go the whole loop again
		if (bChange)
		{
		    items.get(indexToMove).gotoNextProximity();
		}
		return bChange;
	}

	public boolean alignStreamsToRecord(int maxRec) {
		for(VBFolioQueryOperator oper : items) 
		{
		    if (oper.moveToRecord(maxRec) == false)
		    {
		        setEOF(true);
		        return false;
		    }
		}
		
		return true;
	}

	@Override
	public boolean isEOF()
	{
	    EOF = false;
	    
	    for (VBFolioQueryOperator op : items) {
	        if (op.isEOF() == true)
	        {
	            setEOF(true);
	            break;
	        }
	    }
	    
	    return EOF;
	}


}
