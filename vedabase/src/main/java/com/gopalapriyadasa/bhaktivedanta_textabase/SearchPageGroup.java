package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.gopalapriyadasa.textabase_engine.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;

@SuppressLint("DefaultLocale")
public class SearchPageGroup implements EndlessTextViewCallback {

	public static int currentRecordId = 0;
	public static FlatFileSearchResults searchResults = new FlatFileSearchResults();
	//private Folio currentFolio = FolioLibraryService.getCurrentFolio();

	Button newQueryButton = null;
	TextView resultTitleView = null;
	TextView searchingMessage = null;
	EndlessTextView resView = null;
	View parentView = null;


    //
    // query panel variables
    //
	LinearLayout queryPanel = null;
	EditText editQueryText = null;
    Button buttonHideQueryPanel = null;
    Button buttonSearch = null;
    Button buttonPreviousQuery = null;
    Button buttonNextQuery = null;
    Button buttonClearQuery = null;
    Spinner spinnerTemplate = null;
    Spinner spinnerScope = null;
    private int currentTemplateIndex = 0;
    private String defaultTemplateName = "All Records";
    private String templateName = "";
    private ArrayList<CharSequence> spinnerItems = new ArrayList<CharSequence>();
    private String defaultScope = "Whole Database";
    private String currentScope;
    private ArrayList<CharSequence> scopeLevels = new ArrayList<CharSequence>();



	public Button getNewQueryButton() {
		return newQueryButton;
	}

	public void setNewQueryButton(Button newQueryButton) {
		this.newQueryButton = newQueryButton;
	}

	public TextView getResultTitleView() {
		return resultTitleView;
	}

	public void setResultTitleView(TextView resultTitleView) {
		this.resultTitleView = resultTitleView;
	}

	MainActivity mainActivity = null;
	
	public SearchPageGroup(MainActivity ma) {
		
		mainActivity = ma;
		parentView = ma.findViewById(R.id.searchPane);
        templateName = defaultTemplateName;
		
		resView = (EndlessTextView)parentView.findViewById(R.id.endlessTextView1);
		resView.setSource(searchResults);
		resView.drawLineBeforeRecord = true;
		resView.drawRecordNumber = true;
		resView.delegate = this;

        //
        // query panel
        //
		queryPanel = (LinearLayout) parentView.findViewById(R.id.queryPanel);
		editQueryText = (EditText) queryPanel.findViewById(R.id.editQueryText);
        buttonHideQueryPanel = (Button) queryPanel.findViewById(R.id.buttonHideQueryPanel);
        buttonNextQuery = (Button) queryPanel.findViewById(R.id.buttonNextQuery);
        buttonPreviousQuery = (Button) queryPanel.findViewById(R.id.buttonPreviousQuery);
        buttonSearch = (Button) queryPanel.findViewById(R.id.buttonSearch);
        buttonClearQuery = (Button) queryPanel.findViewById(R.id.buttonClearQuery);
        spinnerTemplate = (Spinner) queryPanel.findViewById(R.id.spinnerTemplate);
        spinnerScope = (Spinner) queryPanel.findViewById(R.id.spinnerScope);
        currentScope = defaultScope;
        scopeLevels.add(defaultScope);
        scopeLevels.add("Current Book");
        scopeLevels.add("Current Article");

        editQueryText.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION)
                {
                    // code to hide the soft keyboard
                    InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editQueryText.getApplicationWindowToken(), 0);
                }
                return false;
            }
        });

        editQueryText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        spinnerTemplate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = (String) adapterView.getItemAtPosition(i);
                templateName = str;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Folio folio = FolioLibraryService.getCurrentFolio();
                if (folio != null) {
                    FolioQueryInstance fqi = new FolioQueryInstance();
                    fqi.name = templateName;
                    fqi.data = editQueryText.getText().toString();
                    fqi.scopeIndex = getSelectedScopeIndex();

                    FolioQueryTemplate fqt = folio.findQueryTemplate(templateName);
                    if (fqt != null) {
                        Log.i("search", "Starting search using template: " + templateName);
                        fqi.pattern = fqt.pattern;
                    } else {
                        Log.i("search", "Starting search without template.");
                        fqi.name = "";
                    }
                    folio.addQueryHistory(fqi);
                    queryPanel.setVisibility(View.GONE);
                    startQuerySearch(fqi);
                }
            }
        });
        buttonHideQueryPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryPanel.setVisibility(View.GONE);
            }
        });

        buttonPreviousQuery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentTemplateIndex > 0) {
                    selectTemplateByIndex(currentTemplateIndex - 1);
                    validateHistoryButtons();
                }
            }


        });

        buttonNextQuery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				Folio currentFolio = FolioLibraryService.getCurrentFolio();
				if (currentFolio != null) {
					int count = currentFolio.queryHistory.size();
					if (currentTemplateIndex < (count - 1)) {
						selectTemplateByIndex(currentTemplateIndex + 1);
						validateHistoryButtons();
					}
				}
            }
        });

        buttonClearQuery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Folio folio = FolioLibraryService.getCurrentFolio();
                if (folio != null) {
                    selectTemplateByIndex(folio.queryHistory.size());
                    validateHistoryButtons();
                }
            }
        });

        if (FolioLibraryService.getCurrentFolio() != null) {
            currentTemplateIndex = FolioLibraryService.getCurrentFolio().queryHistory.size();
        }

        validateHistoryButtons();

        //
        // END OF QUERY PANEL INITIALIZATION
        //

		//resultsView = (WebView)searchPage.findViewById(R.id.webViewSearch);
		setResultTitleView((TextView)parentView.findViewById(R.id.statusTitle));
		setNewQueryButton((Button)parentView.findViewById(R.id.buttonShowMenu));
		searchingMessage = (TextView)parentView.findViewById(R.id.textView2);
		
		hideSearchMessage();
		
		newQueryButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//DialogSimpleQuery dialog = new DialogSimpleQuery();
				//dialog.show(mainActivity.getFragmentManager(), "dlg_search");
				queryPanel.setVisibility(View.VISIBLE);
			}
		});
		
		Button helpButton = (Button)parentView.findViewById(R.id.buttonClear);
		helpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadHelpPage();
				searchResults.results = new ArrayList<SearchResultItem>();
				queryPanel.setVisibility(View.GONE);
			}
		});
		
		Button closeButton = (Button)parentView.findViewById(R.id.buttonClosePane);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCloseButton();
			}
		});
		
		//SearchPageWebClient client = new SearchPageWebClient();
		//client.setContext(mainActivity);
		//resultsView.setWebViewClient(client);
		
		if (searchResults.results.size() == 0) {
			loadHelpPage();
			queryPanel.setVisibility(View.VISIBLE);
		} else {
			loadResultsPage(0);
			queryPanel.setVisibility(View.GONE);
		}

	}

	public boolean isVisible() {
		return parentView.getVisibility() == View.VISIBLE;
	}

    private int getSelectedScopeIndex() {
        try {
            return scopeLevels.indexOf(spinnerScope.getSelectedItem());
        } catch(Exception ex) {
            return 0;
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setCurrentFolio(Folio folio) {

        spinnerItems.clear();
        spinnerItems.add(defaultTemplateName);
        if (folio != null) {
            for (FolioQueryTemplate tmp : folio.queryTemplates) {
                spinnerItems.add(tmp.name);
            }
        }
        ArrayAdapter<CharSequence> templatesListAdapter = new ArrayAdapter<CharSequence>(mainActivity, android.R.layout.simple_spinner_item, spinnerItems);
        templatesListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemplate.setAdapter(templatesListAdapter);
        spinnerTemplate.setSelection(spinnerItems.indexOf(templateName));

        ArrayAdapter<CharSequence> scopesListAdapter = new ArrayAdapter<CharSequence>(mainActivity, android.R.layout.simple_spinner_item, scopeLevels);
        scopesListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScope.setAdapter(scopesListAdapter);
        spinnerScope.setSelection(0);
    }

    //
    // Selects template by given index from history
    // and initializes edit query text and spinner
    //
    // if templae index is out of the bounds in query history
    // then empty query and default template is initialized
    //
    public void selectTemplateByIndex(int newTemplateIndex) {

        currentTemplateIndex = newTemplateIndex;

        boolean initialized = false;
        Folio folio = FolioLibraryService.getCurrentFolio();
        if (folio != null) {
            if (folio.queryHistory.size() > currentTemplateIndex) {
                FolioQueryInstance qi = folio.queryHistory.get(currentTemplateIndex);
                setTemplateName(qi.name);
                editQueryText.setText(qi.data);
                initialized = true;
            }
        }

        if (!initialized) {
            editQueryText.setText("");
            setTemplateName(defaultTemplateName);
        }

    }

    public void setTemplateName(String tn) {
        templateName = tn;
        int index = spinnerItems.indexOf(tn);
        if (index >= 0)
            spinnerTemplate.setSelection(index);
        else
            spinnerTemplate.setSelection(0);
    }

    private void validateHistoryButtons() {

        Folio folio = FolioLibraryService.getCurrentFolio();
        if (folio != null) {
            int count = folio.queryHistory.size();
            buttonPreviousQuery.setEnabled(count > 0 && currentTemplateIndex > 0);
            buttonNextQuery.setEnabled(count > 0 && currentTemplateIndex < (count - 1));
        } else {
            buttonPreviousQuery.setEnabled(false);
            buttonNextQuery.setEnabled(false);
        }
    }

	public void onCloseButton() {
		if (mainActivity != null) {
			mainActivity.SetCurrentTab(R.id.textPane);
		}
	}
	
	public void hideSearchMessage() {
		
		mainActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				searchingMessage.setVisibility(View.GONE);
			}
			
		});
		
	}
	
	public void showSearchMessage() {
		mainActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				searchingMessage.setVisibility(View.VISIBLE);
			}
			
		});
	}
	
	public void startQuerySearch(FolioQueryInstance fqi) {

        String text = fqi.getFinalQuery();
        Log.i("search", "Start search with query: " + text);
		SearchPageAsyncTask task = new SearchPageAsyncTask();
		
		showSearchMessage();

        task.setScopeIndex(fqi.scopeIndex);
        task.setUserInterfacePage(this);
		task.execute(text);
//		Folio currentFolio = FolioLibraryService.getCurrentFolio();
//		boolean ignoreSel = false;
//		if (currentFolio.getContentSelected() == 0)
//			ignoreSel = true;
//		currentFolio.search(text, results, mainActivity.textPageGroup.getPhrases(), ignoreSel);
		
		//querySearchDidFinish();
	}

	public void querySearchDidFinish() {

		Log.i("search", "Results count: " + searchResults.results.size());

		searchingMessage.setVisibility(View.GONE);

		String format = mainActivity.getString(R.string.search_result_text);

		if (searchResults.emptyResults || searchResults.results.size() == 0) {
			resultTitleView.setText("No Results - "  + spinnerScope.getSelectedItem().toString());
		} else {
			resultTitleView.setText(String.format(Locale.getDefault(), format, totalCount())
					+ " - " + spinnerScope.getSelectedItem().toString());
		}

        // for all scopes narrower than Whole Database
        // a help text should be displayed for notifying the user
        // that he/she selected a limited scope
        if (getSelectedScopeIndex() != 0) {
            SearchResultItem resultItem = new SearchResultItem();
            resultItem.setRecordId(-1);
            resultItem.setText(String.format(Locale.getDefault(), "<CR>Too few results?<CR><CR>Currently you have selected scope <FC:0,128,0>%s<FC> "
                    + "in the query panel. Try search with broader range.", spinnerScope.getSelectedItem().toString()));
            searchResults.results.add(resultItem);
        }

		loadResultsPage(0);
		
	}
	
	public int totalCount() {
		
		return searchResults.getRecordCount();
	}
	
	public void loadHelpPage() {

		resultTitleView.setText("User Guide");
		resView.setRecord(0);
		loadUrl("assets/SearchExamples.htm");
	}

	private void loadUrl(String string) {
		resView.drawLineBeforeRecord = false;
		resView.drawRecordNumber = false;
		resView.setRecord(0);

		try {
			InputStream is = mainActivity.getAssets().open(string);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			int line = 0;
			String mLine = reader.readLine();
			searchResults.results.clear();
			searchResults.text.clear();
			while(mLine != null) {
				FolioTextRecord ftx = new FolioTextRecord();
				ftx.setRecord(line);
				ftx.setPlainText(mLine);

				FlatParagraph para = new FlatParagraph(null);
				para.alternativeFormats = FlatFileSearchResults.formats;
				searchResults.text.add(para.convertToRaw(ftx));

				mLine = reader.readLine();
				line++;
			}
			reader.close();
			
		} catch (Exception E) {
			Log.i("Ex", "exception: " + E.getMessage());
		}
		
		resView.invalidate();
	}

	public void loadResultsPage(int nPage) {

		if (searchResults.results == null || searchResults.results.size() == 0 ) {
            Log.i("search", "Showing no-results page");
			searchResults.text.clear();
			resView.drawLineBeforeRecord = false;
			resView.drawRecordNumber = false;
			resView.setRecord(0);

			FolioTextRecord ftx = new FolioTextRecord();
			ftx.setRecord(0);
			ftx.setPlainText(searchResults.flatTextAlternative);

			FlatParagraph para = new FlatParagraph(null);
			para.alternativeFormats = FlatFileSearchResults.formats;
			searchResults.text.add(para.convertToRaw(ftx));
//			loadUrl("assets/NoResults.htm");
			resView.invalidate();
		} else {
            Log.i("search", "Showing results of search");
			searchResults.text.clear();
			resView.setRecord(SearchPageGroup.currentRecordId);
			resView.drawLineBeforeRecord = true;
			resView.drawRecordNumber = true;
			resView.invalidate();
		}
	}

	public String getResultsPageHtmlText(int resultsPage) {

		Folio currentFolio = FolioLibraryService.getCurrentFolio();
		StringBuilder str = new StringBuilder();
        str.append("<html><head><title>results</title>");
		str.append(String.format(
				"<link href=\"%s\" type=text/css rel=stylesheet>\n",
				Folio.URL_STYLE_FONTS));
        str.append("\n<style>");
        str.append("\n<!--");
        str.append(String.format("\n.FoundText { background-color:yellow;}\ntable,tr,td { font-size:%dpt; }\n", currentFolio.getBodyFontSize()));
        str.append("-->\n");
        str.append("</style>\n");
        str.append("</head>\n");
        str.append(String.format("<body style='font-family:Arial;font-size:%dpt;' background=\"vbase://assets/ylw_bkg.png\">", currentFolio.getBodyFontSize()));

        // navigation bar
        //[str appendString:strNavig];
        
        // results
        int j = 1;
		for(SearchResultItem numb : searchResults.results)
		{
            str.append("<table border=0><tr><td valign=top>");
            str.append(String.format("<p style=\"font-size:170%%\"><span style=\"font-size:10pt\">Page&nbsp%d</span><br>%d&nbsp;</p>", resultsPage + 1, j + resultsPage*Folio.RESULTS_IN_PAGE));
            str.append("</td><td valign=top>");
            str.append(processFoundRecord(numb, currentFolio));
            str.append("</td></tr></table><hr>\n");
            j++;
		}
		
        str.append("<p align=center>&nbsp;<br>&nbsp;");

        /*if (resultsPage < (searchResults.results.size() - 1))
        {
            str.append(String.format("\n<a href='vbase://results/%d'\" style=\"font-size:120%%;text-decoration:none;color:#7f7f7f;font-family:Helvetica;\">More results...</a>", resultsPage + 1));
        }*/
        str.append("</p>");

        str.append("</body></html>");

		
		return str.toString();
	}

	private String processFoundRecord(SearchResultItem numb, Folio currentFolio) {
		
   
		StringBuilder strPage = new StringBuilder();
		VBContentNode item = currentFolio.findRecordPath(numb.getRecordId());
		String  path = currentFolio.getDocumentPath(item);
		if (path == null)
			return "";
		
		List<FolioTextRecord> textRecord = currentFolio.findTextRecords(numb.getRecordId(), 1);
		if (textRecord.size() == 0)
			return "";
		
		String  text = FlatFileUtils.removeTags(textRecord.get(0).getPlainText());
		
		VBFindRangeArray ranges = new VBFindRangeArray();
		
		NSRange fran;
	    
		VBHighlightedPhraseSet phrases = mainActivity.textPageGroup.getPhrases(); 
		if (phrases == null || phrases.items == null) {
			return "<p>no phrases for " + numb.getRecordId();
		}
		
	    for (VBHighlightedPhrase phrase : mainActivity.textPageGroup.getPhrases().items)
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
	                ranges.insertRange((quoteRange.getLocation() - 35), (quoteRange.getLength() + 70), quoteRange);
	                quoteRange.setLocation(-1);
	            }
	        }
	    }
		
	    //NSLog(@"ranges = %@", [ranges debugDescription]);
		ranges.sortArray();
		

		// header
		strPage.append(String.format("<p style='font-size:%dpt'><a style='color:darkgreen;font-weight:bold;' href=\"vbase://search/%d\">%s</a><br>", currentFolio.getBodyFontSize(), numb.getRecordId(), path));

		

	    //NSLog(@"source = %@", text);
	    //NSLog(@"ranges = %@", [ranges debugDescription]);
	    
		for(int a = 0; a < ranges.size(); a++)
		{
			ranges.applyRange(a, text, strPage);
		}
		
		strPage.append("</p>");
		
		numb.setPath(path);
		numb.setText(strPage.toString());
		
		return numb.getText();
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

	public void newQueryUsingTemplate(CharSequence title) {
		DialogSimpleQuery dialog = new DialogSimpleQuery();
		dialog.setTemplateName(title);
		dialog.show(mainActivity.getFragmentManager(), "dlg_search");
		
	}

	@Override
	public void endlessTextViewLeftAreaClicked(int nRecordId) {
		
	}

	@Override
	public void endlessBookmarksChanged() {

	}

	@Override
	public void endlessHighlightsChanged() {

	}

	@Override
	public void endlessTextNotesChanged() {
		
	}

	@Override
	public void endlessTextViewRecordChanged(int nRecordId) {
		currentRecordId = nRecordId;
	}

	@Override
	public void endlessTextViewLinkActivated(String type, String link) {

		Log.i("type", type + ": " + link);
		if (type.equals("REL")) {
			int i = Integer.parseInt(link);
			if (i > 0) {
				mainActivity.SetCurrentTab(R.id.textPane);
				mainActivity.LoadFolioTextRecords(i, true);
				searchResults.selectItemWithReference(i);
			}
		}
	}

	@Override
	public void endlessTextViewRecordClicked(FDRecordBase record) {
		mainActivity.SetCurrentTab(R.id.textPane);
		mainActivity.LoadFolioTextRecords(record.relatedRecordId, true);
		searchResults.selectItemWithReference(record.relatedRecordId);
		record.visited = true;
	}

	@Override
	public void endlessTextHistoryChanged() {
	
	}

	public View getParentView() {
		return parentView;
	}

	@Override
	public void endlessTextLoading(boolean flag) {

	}
}
