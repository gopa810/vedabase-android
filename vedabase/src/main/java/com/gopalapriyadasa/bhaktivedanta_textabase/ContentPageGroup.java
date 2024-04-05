package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.gopalapriyadasa.html_doc.HtmlElement;
import com.gopalapriyadasa.html_doc.HtmlStyle;
import com.gopalapriyadasa.html_doc.HtmlStyleCollection;
import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.VBBookmark;
import com.gopalapriyadasa.textabase_engine.VBContentRow;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.VBCustomHighlights;
import com.gopalapriyadasa.textabase_engine.VBCustomNotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ListView;

public class ContentPageGroup {

	MainActivity main = null;
	View parentView = null;
	ListView contentListView = null;
	//WebView contentWebView = null;
	public int i = 0;

	// information about current page
	private int currentPageType = 0;
	private int currentPageId = 0;
	private String lastPageCode = "contents";
	private ArrayList<String> contentHistory = new ArrayList<String>();
	private ArrayList<VBContentRow> currentRows = new ArrayList<VBContentRow>();
	public ContentListAdapter contentListAdapter;




	// static initialization
	{
	}
	
	
	public int getCountOfCheckedItems() {
		int count = 0;
		for(VBContentRow item : currentRows) {
			count += (item.getSelected() == VBContentRow.SELECTION_YES ? 1 : 0);
		}
		return count;
	}


	public ContentPageGroup(MainActivity ma) {
		
		main = ma;
		parentView = ma.findViewById(R.id.contentPane);

		
		// initialize content view
		/*contentWebView = (WebView) parent.findViewById(R.id.webView);
		contentWebView.setWebViewClient(main.webClient);
		contentWebView.setWebChromeClient(new WebChromeClient());
		WebSettings webSettings = contentWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setDomStorageEnabled(true);
		contentWebView.addJavascriptInterface(main.javascriptInterface, "Textabase");*/


		contentListAdapter = new ContentListAdapter(ma, R.layout.ci_item, currentRows);
		contentListView = (ListView) parentView.findViewById(R.id.listViewContent);
		contentListView.setAdapter(contentListAdapter);

	}

	public boolean isVisible() {
		return parentView.getVisibility() == View.VISIBLE;
	}

	private void saveToHistory(String cmd) {
		contentHistory.add(0, cmd);
	}

	public boolean canGoBack() {
		return contentHistory.size() > 1;
	}

	public String getLastCommand() {
		if (contentHistory.size() > 0) {
			return contentHistory.get(0);
		}
		return "";
	}

	public void goBack() {
		if (contentHistory.size() > 1) {
			contentHistory.remove(0);
			executeCommand(contentHistory.get(0));
		}
	}

	public void executeCommand(String command) {
		int value;
		Log.i("contents", "ExecutingCommand: " + command);
		TextCommand cmd = new TextCommand(command);
		cmd.parseTo(" ");

		if (cmd.hasToken("load")) {
			cmd.parseTo(" ");
			lastPageCode = cmd.getToken();
			if (cmd.hasToken("contents")) {
				saveToHistory(command);
				cmd.parseTo(" ");
				value = cmd.getTokenInt();
				loadContentPageForRecord(VBContentRow.TYPE_ITEM, value);
			} else if (cmd.hasToken("bookmark")) {
				saveToHistory(command);
				cmd.parseTo(" ");
				value = cmd.getTokenInt();
				loadContentPageForRecord(VBContentRow.TYPE_BOOKMARK, value);
			} else if (cmd.hasToken("note")) {
				saveToHistory(command);
				cmd.parseTo(" ");
				value = cmd.getTokenInt();
				loadContentPageForRecord(VBContentRow.TYPE_NOTE, value);
			} else if (cmd.hasToken("hightext")) {
				saveToHistory(command);
				cmd.parseTo(" ");
				value = cmd.getTokenInt();
				loadContentPageForRecord(VBContentRow.TYPE_HIGHLIGHTER, value);
			} else if (cmd.hasToken("appmap")) {
				saveToHistory(command);
				loadApplicationMap();
			} else {
				Log.e("err", "Uknown type " + cmd.getToken() + " in command " + command);
			}
		} else if (cmd.hasToken("show")) {
			cmd.parseTo(" ");
			if (cmd.hasToken("text")) {
				cmd.parseTo(" ");
				if (cmd.getTokenInt() > 0) {
					main.loadRecordSilently(cmd.getTokenInt(), true);
				}
				main.setCurrentTabCode("text");
			} else if (cmd.hasToken("search")) {
				main.setCurrentTabCode("search");
			} else if (cmd.hasToken("appmap")) {
				saveToHistory(command);
				lastPageCode = "appmap";
				loadApplicationMap();
			} else if (cmd.hasToken("intro")) {
				loadIntroText();
			} else if (cmd.hasToken("dbinfo")) {
				main.setCurrentTabCode("dbinfo");
			} else if (cmd.hasToken("settings")) {
				Runnable action = new Runnable() {
					@Override
					public void run() {
						Context mContext = main;
						Intent settings = new Intent(mContext, SettingsActivity.class);
						mContext.startActivity(settings);
					}
				};
				main.post(action);
			} else if (cmd.hasToken("www")) {
				cmd.parseTo(" ");
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cmd.getToken()));
				main.startActivity(browserIntent);
			} else {
				Log.e("err", "Uknown type " + cmd.getToken() + " in command " + command);
			}
		}  else {
			Log.e("err", "Uknown type " + cmd.getToken() + " in command " + command);
		}
	}

	public void loadApplicationMap() {

		contentListAdapter.clear();

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_TITLE, "Navigation"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_HR));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Contents", R.drawable.content_icon_dir , "load contents 0"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Bookmarks", R.drawable.content_bkmk, "load bookmark 0"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Notes", R.drawable.content_notes, "load note 0"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Highlighted Texts", R.drawable.content_hightext, "load hightext 0"));

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_TITLE, "Text Read"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_HR));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Text View", R.drawable.text_pages_icon , "show text -1"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Full Text Search", R.drawable.search_icon, "show search"));

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_TITLE, "Extras"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_HR));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Settings", R.drawable.settings_icon , "show settings"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_ITEM, "Database Maintenance", R.drawable.content_hightext, "show dbinfo"));

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_SPACE));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_SPACE));
		contentListAdapter.notifyDataSetChanged();
	}

	public void loadIntroText() {
		VBContentRow row;

		contentListAdapter.clear();
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_TITLE, "Welcome"));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_HR));

		row = new VBContentRow(VBContentRow.TYPE_TEXT);
		row.setMainTitle("This is version 2016 of Vedabase. In case you are running this application for the first time, your need to download database. Downloading process is initiated amd can be seen in Database screen.");
		contentListAdapter.add(row);

		row = new VBContentRow(VBContentRow.TYPE_ACTION);
		row.setMainTitle("Database Overview");
		row.setAction("show dbinfo");
		contentListAdapter.add(row);

		row = new VBContentRow(VBContentRow.TYPE_ACTION);
		row.setMainTitle("Table of Contents");
		row.setAction("load contents 0");
		contentListAdapter.add(row);


		row = new VBContentRow(VBContentRow.TYPE_TEXT);
		row.setMainTitle("If downloading does not work from some reason, please visit page with further instructions:");
		contentListAdapter.add(row);

		row = new VBContentRow(VBContentRow.TYPE_ACTION);
		row.setMainTitle("Troubleshoot Downloading");
		row.setAction("show www http://vedabase.home.sk/android/fallout.html");
		contentListAdapter.add(row);

		row = new VBContentRow(VBContentRow.TYPE_TEXT);
		row.setMainTitle("Complete set of functions is accessed through 'Application Map' choice in the option menu. Just click on three dots in top right corner and choose 'Application Map'. This app has three main pages: Contents, Text and Search page and there are 3 small icons in the action bar in the top right corner of your screen. Another important, but infrequently used, is Database Page, accessible through Application Map.");
		contentListAdapter.add(row);

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_HR));

		row = new VBContentRow(VBContentRow.TYPE_TEXT);
		row.setMainTitle("Find us on Facebook page for further assistance.");
		contentListAdapter.add(row);

		row = new VBContentRow(VBContentRow.TYPE_ACTION);
		row.setMainTitle("Bhaktivedanta Vedabase for Android");
		row.setAction("show www https://www.facebook.com/vedabaseandroid");
		contentListAdapter.add(row);

		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_SPACE));
		contentListAdapter.add(new VBContentRow(VBContentRow.TYPE_SPACE));

		contentListAdapter.notifyDataSetChanged();

	}

	public void loadContentPageForRecord(int itemType, int recordId) {

		String mainTitle;
		Folio currentFolio = FolioLibraryService.getCurrentFolio();

		if (currentFolio == null) {
			return;
		}

		contentListAdapter.clear();
		setCurrentPageType(itemType);
		setCurrentPageId(recordId);


		ArrayList<VBContentRow> itemList = null;
		if (itemType == VBContentRow.TYPE_ITEM ||
				itemType == VBContentRow.TYPE_BACK) {
			itemList = currentFolio.findContentItemsByParent(recordId);
		} else if (itemType == VBContentRow.TYPE_BOOKMARK) {
			itemList = new ArrayList<VBContentRow>();
			//insertSectionTitle(itemsList, VBContentRow.TYPE_BOOKMARK);
			currentFolio.getBookmarksContentItems(itemList, recordId);
			//insertRowText(contentListAdapter, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
		} else if (itemType == VBContentRow.TYPE_NOTE) {
			//insertSectionTitle(itemsList, VBContentRow.TYPE_NOTE);
			itemList = new ArrayList<VBContentRow>();
			currentFolio.getNotesContentItems(itemList, recordId);
			//insertRowText(contentListAdapter, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
		} else if (itemType == VBContentRow.TYPE_HIGHLIGHTER) {
			//insertSectionTitle(itemsList, VBContentRow.TYPE_NOTE);
			itemList = new ArrayList<VBContentRow>();
			currentFolio.getHighlightsContentItems(itemList, recordId);
			//insertRowText(contentListAdapter, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
		}

		if (itemList != null) {
			contentListAdapter.addAll(itemList);
		}


		String clickAction;

		//---------
		// inserting main title
		//
		if ((mainTitle = getItemTitle(currentFolio, itemType, recordId)) != null) {
			Log.i("javascript", "MainTITLE= " + mainTitle);
			int parentId = getItemParent(currentFolio, itemType, recordId);

			if (parentId != recordId) {

				VBContentRow row = new VBContentRow();
				row.setID(parentId);
				row.setType(VBContentRow.TYPE_BACK);
				row.setMainTitle("[PARENT]");
				row.setAction("load " + getItemTypeText(itemType) + " " + parentId);
				Log.i("content", "Goto Parent is: " + row.getAction());
				contentListAdapter.insert(row, 0);
			}

			//contentListAdapter.insert(new VBContentRow(VBContentRow.TYPE_HR), 0);

			VBContentRow row = new VBContentRow();
			row.setType(VBContentRow.TYPE_TITLE);
			row.setMainTitle(mainTitle);
			contentListAdapter.insert(new VBContentRow(VBContentRow.TYPE_HR), 0);
			contentListAdapter.insert(row, 0);
		}



		// inserting quick links
		VBContentRow row = new VBContentRow(VBContentRow.TYPE_QUICKLINKS);
		row.setMainTitle("quicktop:" + Integer.toString(itemType));
		contentListAdapter.insert(new VBContentRow(VBContentRow.TYPE_HR), 0);
		contentListAdapter.insert(row, 0);

		contentListAdapter.notifyDataSetChanged();
	}

	public VBContentRow insertRowText(String text, int rowType) {
		VBContentRow row = new VBContentRow();
		row.setType(rowType);
		row.setMainTitle(text);
		row.setParent(0);
		row.setRecord(0);
		row.setNavigable(false);
		row.setExpandable(false);

		contentListAdapter.add(row);

		return row;
	}

	public String getItemTitle(Folio currentFolio, int itemType, int recordId) {
		Log.i("javascript", "getItemTitle   itemType:" + itemType + " recordId:" + recordId);
		if (recordId <= 0) {
			switch(itemType) {
				case VBContentRow.TYPE_BOOKMARK:
					return "Bookmarks";
				case VBContentRow.TYPE_NOTE:
					return "Notes";
				case VBContentRow.TYPE_HIGHLIGHTER:
					return "Highlights";
				default:
					return null;
			}
		} else {
			if (itemType == VBContentRow.TYPE_ITEM) {
				VBContentRow item = currentFolio.findContentItemByRecord(recordId);
				return item == null ? null : item.getMainTitle();
			} else if (itemType == VBContentRow.TYPE_BOOKMARK) {
				VBBookmark bkmk = currentFolio.findBookmarkById(recordId);
				return bkmk == null ? null : bkmk.getName();
			} else if (itemType == VBContentRow.TYPE_NOTE) {
				VBCustomNotes rn = currentFolio.findRecordNoteById(recordId);
				return rn == null ? null : rn.getNoteText();
			} else if (itemType == VBContentRow.TYPE_HIGHLIGHTER) {
				VBCustomHighlights rn = currentFolio.findCustomHighlightsById(recordId);
				return rn == null ? null : rn.getHighlightedText();
			}
			return null;
		}
	}

	public String getItemTypeText(int itemType) {
		switch(itemType) {
			case VBContentRow.TYPE_BOOKMARK:
				return "bookmark";
			case VBContentRow.TYPE_NOTE:
				return "note";
			case VBContentRow.TYPE_HIGHLIGHTER:
				return "hightext";
			case VBContentRow.TYPE_ITEM:
				return "contents";
			default:
				return "item";
		}
	}
	public int getItemParent(Folio currentFolio, int itemType, int recordId) {

		int parentId = 0;
		switch(itemType) {
			case VBContentRow.TYPE_BOOKMARK:
				parentId = currentFolio.getParentIdForBookmark(recordId);
				break;
			case VBContentRow.TYPE_NOTE:
				parentId = currentFolio.getParentIdForNote(recordId);
				break;
			case VBContentRow.TYPE_HIGHLIGHTER:
				parentId = currentFolio.getParentIdForHighlighter(recordId);
				break;
			case VBContentRow.TYPE_ITEM:
				parentId = currentFolio.getParentIdForContentItem(recordId);
				break;
		}
		return parentId;
	}

    /*public void navigate(final String url) {
        contentWebView.post(new Runnable() {
            @Override
            public void run() {
                contentWebView.loadUrl(url);
            }
        });
    }*/

	private void appendTitleItem(ArrayList<VBContentRow> list,
			String string) {
		VBContentRow row = new VBContentRow();
		row.setType(VBContentRow.TYPE_TITLE);
		row.setMainTitle(string);
		list.add(row);		
	}

	public void appendBackItem(ArrayList<VBContentRow> list, String string, int page) {
		VBContentRow row;
		row = new VBContentRow();
		row.setType(VBContentRow.TYPE_BACK);
		row.setMainTitle(string);
		row.setRecord(page);
		list.add(row);
	}


	public void reloadPage() {

		loadContentPageForRecord(getCurrentPageType(), getCurrentPageId());
	}

	public void onBookmarksChanged() {

		if (getCurrentPageType() == VBContentRow.TYPE_BOOKMARK) {
			reloadPage();
		}
	}

    public void onNotesChanged() {

        if (getCurrentPageType() == VBContentRow.TYPE_NOTE) {
            reloadPage();
        }
    }

    public void onHighlightsChanged() {

        if (getCurrentPageType() == VBContentRow.TYPE_HIGHLIGHTER) {
            reloadPage();
        }
    }

	public int getCurrentPageType() {
		return currentPageType;
	}

	public void setCurrentPageType(int currentPageType) {
		this.currentPageType = currentPageType;
	}

	public int getCurrentPageId() {
		return currentPageId;
	}

	public void setCurrentPageId(int currentPageId) {
		this.currentPageId = currentPageId;
	}

	public ArrayList<VBContentRow> getCurrentRows() {
		return currentRows;
	}

	public void setCurrentRows(ArrayList<VBContentRow> currentRows) {
		this.currentRows = currentRows;
	}

	public String getLastPageCode() {
		return lastPageCode;
	}
}
