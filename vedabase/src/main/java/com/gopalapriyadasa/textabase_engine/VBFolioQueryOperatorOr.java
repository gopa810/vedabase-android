package com.gopalapriyadasa.textabase_engine;

import android.util.Log;

import java.util.ArrayList;

public class VBFolioQueryOperatorOr extends VBFolioQueryOperator {

	ArrayList<VBFolioQueryOperator> items = new ArrayList<VBFolioQueryOperator>();
	private int smallestProximityIndex;

	@Override
	public int getCurrentRecord()
	{
	    if (!isValid())
	        validate();
	    if (!isValid() || isEOF())
	        return 0;
	   
	    return getSmallestRecord();
	}

	@Override
	public void printTree(int level) {
		Log.i("query", printLevel(level) + "OR operator");
		for (VBFolioQueryOperator OP : items) {
			OP.printTree(level+1);
		}
	}


	@Override
	public void printTreeEx(int level, StringBuilder sb) {
		printLevel(level, sb);
		sb.append("operator OR        " + recordHits + "hits<CR>");
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
	    
	    int rec = getSmallestRecord();
	    
	    for(VBFolioQueryOperator oper : items) 
	    {
	        if (oper.getCurrentRecord() == rec)
	        {
	            oper.gotoNextRecord();
	        }
	    }

		recordHits++;
	    return true;
	}

	public int getSmallestRecord()
	{
	    int minRec = 0;
	    int rec = 0;
	    boolean bInit = false;
	    
	    for(VBFolioQueryOperator oper : items) 
	    {
	        if (oper.isEOF() == false)
	        {
	            rec = oper.getCurrentRecord();
	            if (bInit==false || rec<minRec)
	            {
	                minRec = rec;
	                bInit = true;
	            }
	        }
	    }
	    
	    if (bInit == false)
	    {
	        setEOF(true);
	    }
	    
	    return minRec;
	}

	public short getSmallestProximity()
	{
	    int minRec = 0;
	    int rec = 0;
	    short minProx = 0;
	    short prox = 0;
	    boolean bInit = false;
	    int idx = 0;
	    
	    for(VBFolioQueryOperator oper : items) 
	    {
	        if (oper.isEOF() == false)
	        {
	            rec = oper.getCurrentRecord();
	            if (!bInit || rec < minRec)
	            {
	                minRec = rec;
	                minProx = oper.getCurrentProximity();
	                bInit = true;
	                smallestProximityIndex = idx;
	            }
	            else if (rec == minRec)
	            {
	                prox = oper.getCurrentProximity();
	                if (prox < minProx)
	                {
	                    minProx = prox;
	                    smallestProximityIndex = idx;
	                }
	            }
	        }
	        idx ++;
	    }
	    
	    if (bInit == false)
	    {
	        setEOF(true);
	    }
	    
	    return minProx;
	}

	@Override
	public void validate()
	{
	    if (isValid())
	        return;
	    
	    if (items.size() == 0)
	    {
	        setValid(false);
	        setEOF(true);
	        return;
	    }
	    int count = 0;
	    for(VBFolioQueryOperator oper : items)
	    {
	        oper.validate();
	        if (oper.isValid() == true && oper.isEOF() == false)
	            count++;        
	    }
	    
	    if (count == 0)
	    {
	        setValid(false);
	        setEOF(true);
	        for (VBFolioQueryOperator oper : items)
	        {
	            oper.setEOF(true);
	        }
	        return;
	    }
	    
	    setValid(true);
	}


	@Override
	public short getCurrentProximity()
	{
	    return getSmallestProximity();
	}

	@Override
	public boolean gotoNextProximity()
	{
	    int index = 0;
	    
	    getSmallestProximity();
	    
	    index = smallestProximityIndex;
	    
	    if (index >= 0 && index < items.size())
	    {
	        items.get(index).gotoNextProximity();
	    }
	    return true;
	}

	@Override
	public boolean isEOF()
	{
	    super.setEOF(true);
	    
	    for (VBFolioQueryOperator op : items) {
	        if (op.isEOF() == false)
	        {
	            super.setEOF(false);
	            break;
	        }
	    }
	    
	    return super.isEOF();
	}

}
