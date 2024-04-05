package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.*;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class TextPageGroup implements EndlessTextViewCallback {

	ImageButton buttonHistBack;
	ImageButton buttonHistFwd;
	TextView textViewLoading;
    Handler messageHandler;

	// uncomment this in case we want WebView instead of EndlessTextView
	//private WebView webViewText;
	MainActivity main;
	View parentView;
	EndlessTextView textView;


	public static int currentRecordId = 0;
	private static Typeface sanskritTypeface = null;
	private static VBHighlightedPhraseSet phrases = new VBHighlightedPhraseSet();
	public int recordIdForContextMenu = 0;

	/*public static final String URL_SCHEMA = "vbase://";
	public static final String URL_TEXTS = URL_SCHEMA + "texts/";
	public static final String URL_HISTORY = URL_SCHEMA + "history/";
	public static final String URL_SEARCH = URL_SCHEMA + "search/";
	public static final String URL_RESOURCES = URL_SCHEMA + "resources/";
	public static final String URL_ASSETS = URL_SCHEMA + "assets/";
	public static final String URL_POPUP = URL_SCHEMA + "popup/";
	public static final String URL_NOTE = URL_SCHEMA + "note/";
	public static final String URL_INLINEPOPUP = URL_SCHEMA + "inlinepopup/";
	public static final String URL_LINKS = URL_SCHEMA + "links/";*/

	public VBHighlightedPhraseSet getPhrases() {
		return phrases;
	}
	
	public Typeface getSanskritTypeface() {
		return sanskritTypeface;
	}


	public void setSanskritTypeface(Typeface sanskritTypeface) {
		TextPageGroup.sanskritTypeface = sanskritTypeface;
	}


	public EndlessTextView getTextView() {
		return textView;
	}

	public void setTextView(EndlessTextView textView) {
		this.textView = textView;
	}

	public ImageButton getButtonHistBack() {
		return buttonHistBack;
	}


	public void setButtonHistBack(ImageButton buttonHistBack) {
		this.buttonHistBack = buttonHistBack;
	}


	public ImageButton getButtonHistFwd() {
		return buttonHistFwd;
	}


	public void setButtonHistFwd(ImageButton buttonHistFwd) {
		this.buttonHistFwd = buttonHistFwd;
	}

	public TextPageGroup(MainActivity ma) {
		
		main = ma;
		parentView = ma.findViewById(R.id.textPane);
        messageHandler = new Handler();

/*		setWebViewText((WebView)parent.findViewById(R.id.webView2));
        webViewText.setWebViewClient(ma.webClient);
        webViewText.getSettings().setJavaScriptEnabled(true);
        webViewText.addJavascriptInterface(ma.javascriptInterface, "Textabase");*/

		setButtonHistBack((ImageButton)ma.findViewById(R.id.buttonHistBack));
		setButtonHistFwd((ImageButton)ma.findViewById(R.id.buttonHistFwd));
		setTextView((EndlessTextView)parentView.findViewById(R.id.endlessTextView1));
		this.textView.source = FolioLibraryService.getMainSource();
		this.textView.delegate = this;
		Log.i("currerce", "curr rec : " + currentRecordId);
		//this.textView.setRecord(currentRecordId);
		this.textView.invalidate();
        /*this.textView.leftEdgeBitmap = BitmapFactory.decodeResource(ma.getResources(), R.drawable.edge_left);
        this.textView.rightEdgeBitmap = BitmapFactory.decodeResource(ma.getResources(), R.drawable.edge_right);
        this.textView.edgeWidth = this.textView.leftEdgeBitmap.getWidth();
        this.textView.edgeHeight = this.textView.rightEdgeBitmap.getHeight();*/

		//Log.i("currerce", "after: " + this.textView.getCurrentRecord());
		
		main.registerForContextMenu(textView);
		
		textViewLoading = (TextView)parentView.findViewById(R.id.statusTitle);
		textViewLoading.setVisibility(View.GONE);
		
		//parent.findViewById(R.id.toolbarFormat).setVisibility(View.GONE);
	
		onCreateListeners();
		
		if (TextPageGroup.sanskritTypeface == null) {
			TextPageGroup.sanskritTypeface = Typeface.createFromAsset(main.getAssets(), "assets/vuArialPlus.ttf");
		}
		
		validateHistoryButtons();
		
	}

	public boolean isVisible() {
		return parentView.getVisibility() == View.VISIBLE;
	}

	public void onSourceChanged() {
		this.textView.source = FolioLibraryService.getMainSource();		
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void onCreateListeners() {

		setHistListener();
	}

	public void showLoadingLabel(final boolean flag) {
		
        messageHandler.post(new Runnable() {

            @Override
            public void run() {
				if (flag && textViewLoading.getVisibility() == View.GONE) {
					textViewLoading.setVisibility(View.VISIBLE);
					lastLoadingLabelVisibility = flag;
				}
				else if (!flag && textViewLoading.getVisibility() == View.VISIBLE) {
					textViewLoading.setVisibility(View.GONE);
					lastLoadingLabelVisibility = flag;
				}
            }

        });

		if (flag) {
			messageHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (textViewLoading.getVisibility() == View.VISIBLE) {
						textViewLoading.setVisibility(View.GONE);
						lastLoadingLabelVisibility = false;
					}
				}

			}, 2000);
		}
	}

    public void loadRecords(final int start, final int end, final boolean addToHistory) {
/*
        Runnable action = new Runnable() {
            @Override
            public void run() {
				webViewText.loadUrl("http://texts/text.vbp?s=" + start + "&e=" + end);
//                webViewText.loadUrl("http://textabase.app/assets/HomaManual.htm");
            }
        };

        webViewText.post(action);
*/
    }
	
	public String getCurrentLocation() {
		Folio folio = FolioLibraryService.getCurrentFolio();
		String title = "1";
		if (folio != null) {
			title = "2";
			VBContentNode item = folio.findRecordPath(this.textView.getCurrentRecord());
			if (item != null) {
				title = folio.getDocumentPath(item);
			}
		}
		return title;
	}
	


	public void showTextStylesDialog() {
		DialogFragment dlg = new DialogTextFormat();
		dlg.show(main.getFragmentManager(), "format");
	}

    public void textStyleSettingsDidChange() {
        /*						currentFolio.setBodyFontFamily(convertRadioIdToFontFace(radioGroup.getCheckedRadioButtonId()));
						currentFolio.setBodyFontSize((int)(convertProgressToBodySize(seekBar1.getProgress())*14));
						currentFolio.setBodyLineSpacing((int)(convertProgressToLineSpacing(seekBar2.getProgress())*100));
						//mainActivity.textPageGroup.getWebViewText().reload();
						FlatParagraph.setFontMultiplyer(convertProgressToBodySize(seekBar1.getProgress()));
						FlatParagraph.setDefaultFont(convertRadioIdToFontFace(radioGroup.getCheckedRadioButtonId()));
						FDCharFormat.multiplySpaces = convertProgressToLineSpacing(seekBar2.getProgress());
*/
        Folio currentFolio = FolioLibraryService.getCurrentFolio();

		if (currentFolio != null) {
			currentFolio.setBodyFontFamily(FDTypeface.getDefaultFontName());
			currentFolio.setBodyFontSize((int) FDCharFormat.getMultiplyFontSize() * 14);
			currentFolio.setBodyLineSpacing((int) FDCharFormat.getMultiplySpaces() * 100);
		}

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(main);
        SharedPreferences.Editor ed = sp.edit();
        ed.putFloat("textFontSize", FDCharFormat.getMultiplyFontSize());
        ed.putFloat("textLineSpace", FDCharFormat.getMultiplySpaces());
        ed.putString("textFontName", FDTypeface.getDefaultFontName());
        ed.commit();

        textView.invalidate();

    }

	public void setHistListener() {
		buttonHistFwd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				textView.goForward();
			}
		});

		buttonHistBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				textView.goBack();
			}
		});
	}
	
	public void validateHistoryButtons() {
		if (textView.canGoBack()) {
			buttonHistBack.setVisibility(View.VISIBLE);
		} else {
			buttonHistBack.setVisibility(View.GONE);
		}
		
		if (textView.canGoForward()) {
			buttonHistFwd.setVisibility(View.VISIBLE);
		} else {
			buttonHistFwd.setVisibility(View.GONE);
		}
	}


	public int getCurrentRecordId() {
		
		return currentRecordId;
	}

	@Override
	public void endlessTextViewLeftAreaClicked(int nRecordId) {

		recordIdForContextMenu = nRecordId;
		main.openContextMenu(textView);
		//main.showNoteEditor(nRecordId);
	}

    @Override
    public void endlessBookmarksChanged() {
        FolioLibraryService service = FolioLibraryService.getInstance();
        service.customReferencesDidChange();
        main.contentPageGroup.onBookmarksChanged();
    }

    @Override
    public void endlessHighlightsChanged() {
        FolioLibraryService service = FolioLibraryService.getInstance();
        service.customReferencesDidChange();
        main.contentPageGroup.onHighlightsChanged();
    }

    @Override
	public void endlessTextNotesChanged() {
        FolioLibraryService service = FolioLibraryService.getInstance();
        service.customReferencesDidChange();
        main.contentPageGroup.onNotesChanged();
	}

	@Override
	public void endlessTextLoading(boolean flag) {
		if (lastLoadingLabelVisibility != flag) {
			showLoadingLabel(flag);
		}
	}


	private int recordChangeCountdown = 0;
	private boolean lastLoadingLabelVisibility = false;
	private boolean recordChangeCountdownRunning = false;
	private Runnable delayedRecordChangeWriter = new Runnable() {
		@Override
		public void run() {
			recordChangeCountdownWrite();
		}
	};

	@Override
	public void endlessTextViewRecordChanged(int nRecordId) {
		currentRecordId = nRecordId;
		recordChangeCountdown = 10;
		if (!recordChangeCountdownRunning) {
			recordChangeCountdownRunning = true;
			messageHandler.postDelayed(delayedRecordChangeWriter, 1000);
		}
	}

	private void recordChangeCountdownWrite() {
		if (recordChangeCountdown == 0) {
			// write last record to preferences
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(main);
			SharedPreferences.Editor ed = sp.edit();
			ed.putInt("lastTextRecord", currentRecordId);
			ed.commit();
			Log.i("timer", "Writing current (last) record := " + currentRecordId);

			recordChangeCountdownRunning = false;
		} else {
			Log.i("timer", "countdown for writing current (last) record, " + currentRecordId);
			messageHandler.postDelayed(delayedRecordChangeWriter, 1000);
			recordChangeCountdown --;
		}
	}

	@Override
	public void endlessTextHistoryChanged() {
		validateHistoryButtons();
	}
	
	@Override
	public void endlessTextViewLinkActivated(String type, String link) {

		Folio current = FolioLibraryService.getCurrentFolio();
		int record = 0;
		Log.i("link", "LINK " + type + ", " + link);
		if (type.equalsIgnoreCase("WW")) {
			showWebPage(link);
		} else if (type.equals("JL")) {
			showLoadingLabel(true);
			asyncSearchJumpLinkAndShow(link);
		} else if (type.equals("ML")) {
			recordIdForContextMenu = textView.getCurrentRecord();
			main.showPopupMenu(link);
		} else if (type.equals("PL")) {
			
		} else if (type.equals("QL")) {
			showLoadingLabel(true);
			asyncSearchRecordAndShow(link);
		} else if (type.equals("EN")) {
			showLoadingLabel(true);
			asyncSearchRecordAndShow(link);
		} else if (type.equals("PX")) {
			String uri = TextPageWebClient.URL_POPUP + FlatFileUtils.encodeLinkSafeString(link);
			main.showPopupWithUrl(uri, -1);
		} else if (type.equals("DL")) {
			
			try {
				if (current != null) {
					FolioObjectRecord fob = current.findObject(link, FolioObjectRecord.DATA_BYTES);
					if (fob.objectData != null) {
						main.playFile(fob.objectData);
					}
				}
			} catch (Exception e) {
				
			}
			
		} else if (type.equals("PW")) {
			String uri = TextPageWebClient.URL_INLINEPOPUP + "RD/" + link;
			main.showPopupWithUrl(uri, -1);
		}
		
	}

	private void asyncSearchJumpLinkAndShow(final String link) {
		messageHandler.post(new Runnable() {
            @Override
            public void run() {
                Folio current = FolioLibraryService.getCurrentFolio();
				if (current != null) {
					int record = current.findJumpDestination(link);
					main.SetCurrentTab(R.id.textPane);
					main.LoadFolioTextRecords(record, true);
				}
            }
        });
	}

	private void asyncSearchRecordAndShow(final String link) {
		messageHandler.post(new Runnable() {
            @Override
            public void run() {
                Folio current = FolioLibraryService.getCurrentFolio();
				if (current != null) {
					int record = current.searchFirstRecord(link);
					if (record > 0) {
						main.SetCurrentTab(R.id.textPane);
						main.LoadFolioTextRecords(record, true);
					} else {
						main.onErrorUnreachableDestination(link);
					}
				}
            }
        });
	}

	@Override
    public void endlessTextViewRecordClicked(FDRecordBase recordId) {

    }

    /**
	 * Shows web page in external application
	 * @param link
	 */
	public void showWebPage(String link) {
		Uri uri;
		if (link.startsWith("http"))
			uri = Uri.parse(link);
		else
			uri = Uri.parse("http://" + link);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		main.startActivity(intent);
	}

	public int getCurrentContentPage(int recordIdForContextMenu2) {

		int rec = recordIdForContextMenu2;
		//VBContentRow value;
		Folio folio = FolioLibraryService.getCurrentFolio();
		//String title = "1";
		if (folio != null) {
			//title = "2";
			VBContentNode item = folio.findRecordPath(this.textView.getCurrentRecord());
			if (item != null) {
				if (item.getParentRecordId() >= 0) {
					rec = item.getParentRecordId();
				}
			}
		}
		return rec;
	}

	public View getParentView() {
		return parentView;
	}

/*	public WebView getWebViewText() {
		return webViewText;
	}

	public void setWebViewText(WebView webViewText) {
		this.webViewText = webViewText;
	}*/
}
