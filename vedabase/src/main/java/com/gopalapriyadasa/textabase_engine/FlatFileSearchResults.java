package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import com.gopalapriyadasa.bhaktivedanta_textabase.MainActivity;

public class FlatFileSearchResults implements FlatFileSourceInterface{

	
	public ArrayList<SearchResultItem> results = new ArrayList<SearchResultItem>();
	public ArrayList<FDRecordBase> text = new ArrayList<FDRecordBase>();
	public static HashMap<String,FDTextFormat> formats = null;
	public int currentResultIndex = -1;
	public static int COLOR_PREVIOUS_SELECTED = 0x7fffffdd;
	public static int COLOR_LAST_SELECTED = 0x9fffffff;
	public boolean emptyResults = false;
	public String flatTextAlternative = "";
	
	static {
		
		formats = new HashMap<String, FDTextFormat>();
		
		FDTextFormat tf = new FDTextFormat();
		tf.name = "PA_Result";
		tf.paraFormat.margins[FDParaFormat.SIDE_BOTTOM] = 20;
		tf.paraFormat.align = FDParaFormat.ALIGN_LEFT;
		tf.textFormat.setFontName("Arial", true);
		tf.textFormat.setTextSize(13);
		formats.put(tf.name, tf);
		
		tf = new FDTextFormat();
		tf.name = "LK_Link";
		tf.textFormat.setTextSize(13);
		tf.textFormat.setForegroundColor(0xff007f00);
		tf.textFormat.setUnderline(true);
		tf.textFormat.setBold(true);
		formats.put(tf.name, tf);
		
		tf = new FDTextFormat();
		tf.name = "CS_FoundText";
		tf.textFormat.setFontName("Arial", true);
		tf.textFormat.setBackgroundColor(0x7fffff00);
		tf.textFormat.setForegroundColor(0xffff0000);
		formats.put(tf.name, tf);

		tf = new FDTextFormat();
		tf.name = "PA_Normal";
		tf.textFormat.setFontName("Arial");
		tf.textFormat.setTextSize(13);
		tf.paraFormat.margins[FDParaFormat.SIDE_TOP] = 6;
		formats.put(tf.name, tf);

		tf = new FDTextFormat();
		tf.name = "PA_HeadA";
		tf.textFormat.setTextSize(16);
		tf.textFormat.setBold(true);
		tf.paraFormat.margins[FDParaFormat.SIDE_TOP] = 30;
		tf.paraFormat.lineHeight = 1.15f;
		tf.textFormat.setForegroundColor(0xff984806);
		formats.put(tf.name, tf);

		tf = new FDTextFormat();
		tf.name = "PA_HeadB";
		tf.textFormat.setTextSize(14);
		tf.textFormat.setBold(true);
		tf.paraFormat.margins[FDParaFormat.SIDE_TOP] = 20;
		tf.textFormat.setForegroundColor(0xff984806);
		formats.put(tf.name, tf);

		tf = new FDTextFormat();
		tf.name = "PA_List";
		tf.textFormat.setFontName("Arial", true);
		tf.textFormat.setTextSize(13);
		tf.textFormat.setItalic(true);
		tf.textFormat.setBold(true);
		tf.paraFormat.margins[FDParaFormat.SIDE_TOP] = 16;
		tf.paraFormat.margins[FDParaFormat.SIDE_LEFT] = 30;
		formats.put(tf.name, tf);

		tf = new FDTextFormat();
		tf.name = "PA_ListB";
		tf.textFormat.setFontName("Arial", true);
		tf.textFormat.setTextSize(13);
		tf.textFormat.setItalic(true);
		tf.paraFormat.margins[FDParaFormat.SIDE_LEFT] = 60;
		formats.put(tf.name, tf);

	}
	
	
	@Override
	public int getRecordCount() {

		if (results != null)
			return results.size();
		return 0;
	}

	@Override
	public FDRecordBase getRecord(int recordId, FlatFileDestination dest,
			int direction) {
		
		
		if (text.size() > 0) {
			if (recordId < 0 || recordId >= text.size())
			{
				return null;
			}
			return text.get(recordId);
		}

		if (recordId < 0 || recordId >= results.size())
		{
			return null;
		}
		
		
		SearchResultItem res = results.get(recordId);
		Folio currentFolio = FolioLibraryService.getCurrentFolio();
		if (res.record == null && currentFolio != null) {
			
			processFoundRecord(res, currentFolio, MainActivity.getInstance().textPageGroup.getPhrases());
			
			FolioTextRecord ftx = new FolioTextRecord();
			ftx.setRecord(recordId);
            if (res.path == null || res.path.length() == 0) {
                ftx.setPlainText("<PS:Result>" + res.text);
            } else {
                ftx.setPlainText("<PS:Result><REL:Link," + res.recordId + ">" + res.path + "<EL><CR>" + res.text);
            }

			FlatParagraph para = new FlatParagraph(null);
			para.alternativeFormats = formats;
			res.record = para.convertToRaw(ftx);
			res.record.isFullParaLink = true;
			res.record.relatedRecordId = res.recordId;
			//res.record.fullParaCommand = String.format(Locale.getDefault(), "text %d", res.recordId);
		}
		
		return res.record;
	}

	@Override
	public String getRecordPath(int record) {
		return String.format("%d/%d", record + 1, getRecordCount());
	}

	@Override
	public VBCustomNotes recordNotesForRecord(int rec) {
		return null;
	}

	@Override
	public boolean canHaveNotes() {
		return false;
	}

	@Override
    public VBCustomHighlights highlightersForRecord(int rec) {
        return null;
    }

    @Override
    public VBCustomHighlights safeHighlightersForRecord(int rec) {
        return null;
    }

    @Override
	public VBCustomNotes safeRecordNotesForRecord(int rec) {
		return null;
	}

	private boolean processFoundRecord(SearchResultItem numb, Folio currentFolio, VBHighlightedPhraseSet phrases) {
		
		   
		StringBuilder strPage = new StringBuilder();
		int recordId = numb.getRecordId();

		if (recordId < 0) {
			// if text is already initialized, then we take it as correct item
			// if text is empty, then this is incorrect item
			if (numb.getText().length() > 0)
				return true;
			else
				return false;
		}
		VBContentNode item = currentFolio.findRecordPath(numb.getRecordId());
		numb.path = currentFolio.getDocumentPath(item);
		if (numb.path == null)
			return false;
		
		List<FolioTextRecord> textRecord = currentFolio.findTextRecords(numb.getRecordId(), 1);
		if (textRecord.size() == 0)
			return false;
		
		String  text = FlatFileUtils.removeTags(textRecord.get(0).getPlainText());
		
		VBFindRangeArray ranges = new VBFindRangeArray();
		
		NSRange fran;
	    
		//VBHighlightedPhraseSet phrases = main.textPageGroup.getPhrases(); 
		if (phrases == null || phrases.items == null) {
			return false;
		}
		//Log.i("prepareSR", "Prepare Search Results -----------");
	    for (VBHighlightedPhrase phrase : phrases.items)
	    {
	        boolean bFindRange = true;
	        NSRange quoteRange = new NSRange();
	        quoteRange.setLocation(-1);
	        int scope = 0;
	        while(bFindRange)
	        {
	            for(VBFindRangeAd frad : phrase.getRanges())
	            {
	                String  str = frad.getWord();
	                fran = findRangeOfMatchForWord(str, text, scope);
	                if (fran.getLocation() < 0)
	                {
	                    bFindRange = false;
	                    break;
	                }
	                
	                if (quoteRange.getLocation()  < 0)
	                {
	                    quoteRange = fran;
	                }
	                else 
	                {
	                    if (quoteRange.getLocationEnd() + 4 > fran.getLocation())
	                    {
	                        quoteRange.setLocationEnd( fran.getLocationEnd() );
	                    }
	                    else
	                    {
	                        quoteRange.setLocation( -1 );
	                        break;
	                    }
	                }

	                scope = fran.getLocationEnd();

	            }
	            
	            if (bFindRange && quoteRange.getLocation() >= 0)
	            {
	                ranges.insertRange((quoteRange.getLocation() - 35), (quoteRange.getLocation() + quoteRange.getLength() + 70), quoteRange);
	                quoteRange.setLocation(-1);
	            }
	        }
	    }
		
	    //NSLog(@"ranges = %@", [ranges debugDescription]);
		ranges.sortArray();
		


	    //NSLog(@"source = %@", text);
	    //NSLog(@"ranges = %@", [ranges debugDescription]);
	    
		for(int a = 0; a < ranges.size(); a++)
		{
			ranges.applyRange(a, text, strPage);
		}
		
		Log.i("searchPage", strPage.toString());
		numb.setText(strPage.toString());
		
		return true;
	}

	public NSRange findRangeOfMatchForWord(String word, String text, int stIdx)
	{
	    if (word.length() == 0)
	        return NSRange.makeRange(-1, -1);
	    
	    char A;
	    
	    VBUnicodeWordMatcher matcher = new VBUnicodeWordMatcher();

	    matcher.word = word.toLowerCase();
	    int i;
	    boolean lastWasNumber = false;
	    for (i = stIdx; i < text.length(); i++)
	    {
	        A = VBUnicodeToAsciiConverter.unicodeToAscii(text.charAt(i));
	        if (A == '.' && !lastWasNumber)
	            A = ' ';
	        if (matcher.sendChar(A, i))
	        {
	            return NSRange.makeRange(matcher.startFindRange, matcher.lastFindIndex);
	        }
	        lastWasNumber = (Character.isDigit(A) ? true : false);
	    }
	    if (matcher.sendChar(' ', i))
	    {
	        return  NSRange.makeRange(matcher.startFindRange, matcher.lastFindIndex);
	    }
	    
	    return NSRange.makeRange(-1, -1);
	}

	public void selectItemWithReference(int i) {

		setBackgroundForItemIndex(currentResultIndex, COLOR_PREVIOUS_SELECTED);
		currentResultIndex = getIndexOfItemWithReference(i);
		setBackgroundForItemIndex(currentResultIndex, COLOR_LAST_SELECTED);
	}
	
	public int getIndexOfItemWithReference(int rec) {
		int i = 0;
		for(SearchResultItem item : results) {
			if (item.recordId == rec) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public void setBackgroundForItemIndex(int index, int color) {
		if (index >= 0 && index < results.size()) {
			SearchResultItem item = results.get(index);
			if (item.record != null && item.record.parts != null) {
				for(FDRecordPart part : item.record.parts) {
					part.paraFormat.backgroundColor = color;
				}
			}
		}
	}

}
