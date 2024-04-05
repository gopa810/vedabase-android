package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class VBUnicodeWordMatcher {

	ArrayList<VBUnicodeWordMatchThread> threads = new ArrayList<VBUnicodeWordMatchThread> ();

	public String word;
	public int startIndex = -1;
	public int lastIndex;
	public int startFindRange;
	public int lastFindIndex;
	
	public boolean sendChar(char chr, int index) {
	    
		boolean retValue = false;
	    lastIndex = index;

	    if (startIndex < 0 && chr != 32)
	    {
	        startIndex = index;
	    }
	    
	    if (threads.size() == 0)
	    {
	        VBUnicodeWordMatchThread  thread = new VBUnicodeWordMatchThread(word);
	        threads.add(thread);
	        thread.compareMode = thread.checkWildCard();
	    }
	    
	    ArrayList<VBUnicodeWordMatchThread> arr = new ArrayList<VBUnicodeWordMatchThread>();
	    boolean breakLoop = false;
	    boolean clearStart = false;
	    for (VBUnicodeWordMatchThread  thread : threads)
	    {
	        if (thread.activeThread == false)
	            continue;
	        char cc = thread.currentChar();
	        switch (thread.compareMode)
	        {
	            case VBUnicodeWordMatchThread.CompareModeRequiredStart:
	                if (chr == 32)
	                {
	                    thread.compareMode = VBUnicodeWordMatchThread.CompareModeWaitStartWord;
	                    clearStart = true;
	                }
	                break;
	            case VBUnicodeWordMatchThread.CompareModeWaitStartWord:
	                if (chr == 32)
	                {
	                }
	                else if (chr == cc || cc=='?')
	                {
	                    thread.currIndex = thread.currIndex + 1;
	                    thread.compareMode = thread.checkWildCard();
	                    thread.compareMode = VBUnicodeWordMatchThread.CompareModeNormal;
	                }
	                else
	                {
	                    thread.compareMode = VBUnicodeWordMatchThread.CompareModeRequiredStart;
	                }
	                break;
	            case VBUnicodeWordMatchThread.CompareModeNormal:
	            case VBUnicodeWordMatchThread.CompareModeWild:
	                if (chr == 32)
	                {
	                    if (thread.matchingIsOver())
	                    {
	                        startFindRange = startIndex;
	                        lastFindIndex = lastIndex;
	                        retValue = true;
	                        breakLoop = true;
	                        break;
	                    }
	                    else
	                    {
	                        if (thread.compareMode == VBUnicodeWordMatchThread.CompareModeWild)
	                        {
	                            thread.activeThread = false;
	                        }
	                        else
	                        {
	                            thread.currIndex = 0;
	                            thread.compareMode = thread.checkWildCard();
	                        }
	                    }
	                    clearStart = true;
	                }
	                else if (chr == cc || cc=='?')
	                {
	                    if (thread.compareMode == VBUnicodeWordMatchThread.CompareModeWild)
	                    {
	                        VBUnicodeWordMatchThread  nt = new VBUnicodeWordMatchThread(thread.word);
	                        arr.add(nt);
	                        nt.currIndex = thread.currIndex;
	                        nt.partIndex = thread.partIndex;
	                        nt.compareMode = thread.compareMode;
	                    }
	                    thread.currIndex = thread.currIndex + 1;
	                    thread.compareMode = thread.checkWildCard();
	                }
	                else
	                {
	                    thread.currIndex = thread.partIndex;
	                }
	                break;
	            case VBUnicodeWordMatchThread.CompareModeWaitForEnd:
	                if (chr == 32)
	                {
	                    startFindRange = startIndex;
	                    lastFindIndex = lastIndex;
	                    retValue = true;
	                    breakLoop = true;
	                    break;
	                }
	                break;
	            case VBUnicodeWordMatchThread.CompareModeRequiredEnd:
	                if (chr == 32)
	                {
	                    startFindRange = startIndex;
	                    lastFindIndex = lastIndex;
	                    retValue = true;
	                    breakLoop = true;
	                    break;
	                }
	                else
	                {
	                    thread.currIndex = thread.partIndex;
	                    thread.compareMode = VBUnicodeWordMatchThread.CompareModeRequiredStart;
	                }
	                break;
	        }
	        if (breakLoop)
	            break;
	    }
	    
	    if (clearStart)
	        startIndex = -1;

	    threads.addAll(arr);
	    arr = new ArrayList<VBUnicodeWordMatchThread>();


	    for(VBUnicodeWordMatchThread  thr : threads)
	    {
	        if (thr.activeThread)
	        {
	            arr.add(thr);
	        }
	    }
	    threads = new ArrayList<VBUnicodeWordMatchThread>();
	    threads.addAll(arr);
	    arr = null;

	    return retValue;
		
	}
	
	public NSRange getRange() {
		return NSRange.makeRange(startIndex, lastIndex);
	}

}
