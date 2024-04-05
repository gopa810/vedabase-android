package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorAnd extends VBFolioQueryOperator {

	ArrayList<VBFolioQueryOperator> items = new ArrayList<VBFolioQueryOperator>();

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
		Log.i("query", printLevel(level) + "AND operator");
		for (VBFolioQueryOperator OP : items) {
			OP.printTree(level+1);
		}
	}

	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("operator AND      " + recordHits + " hits<CR>");
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
	    
	    //get maximum record
	    int maxRec = items.get(0).getCurrentRecord();
	    int rec = 0;
	    boolean bChange = true;
	    
	    while (bChange)
	    {
	        bChange = false;
            int si = 0;
			//Log.i("query", "Loop for record ids ---------------");
	        for(VBFolioQueryOperator oper : items) 
	        {
	            rec = oper.getCurrentRecord();
                //Log.i("query", "Stream " + (si++) + " record " + rec);
	            if (rec != maxRec)
	                bChange = true;
	            if (rec > maxRec)
	            {
	                maxRec = rec;
	            }
	            if (oper.isEOF())
	            {
	                setEOF(true);
	                return;
	            }
	        }
	        
	        if (bChange)
	        {
                //Log.i("query", "Align stream to record " + maxRec);
	            for(VBFolioQueryOperator oper : items) 
	            {
	                if (oper.moveToRecord(maxRec) == false)
	                {
	                    setEOF(true);
	                    return;
	                }
	            }            
	        }
            else {
                //Log.i("query", "ACCEPTED record " + maxRec);
            }
	    }
	    
	    setValid(true);
	}

	@Override
	public boolean isEOF()
	{
	    super.setEOF(false);
	    
	    for (VBFolioQueryOperator op : items) {
	        if (op.isEOF() == true)
	        {
	            super.setEOF(true);
	            break;
	        }
	    }
	    
	    return super.isEOF();
	}

	
}
