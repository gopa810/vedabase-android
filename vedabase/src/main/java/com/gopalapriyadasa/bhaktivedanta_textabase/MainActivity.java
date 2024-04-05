package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.gopalapriyadasa.textabase_engine.*;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import org.apache.http.client.utils.URIUtils;


public class MainActivity extends Activity implements
		OnSharedPreferenceChangeListener, MessageBoxDelegate {

	public static final String LOG_TAG = "VedabaseDownloader";
	public static int TAB_CONTENT = 0;
	public static int TAB_TEXT = 1;
	public static int TAB_SEARCH = 2;
	
	public static int DFS_UNKNOWN = 0;
	public static int DFS_DOWNLOADING = 1;
	public static int DFS_UNZIPPING = 2;
	public static int DFS_VALID = 3;

	public static Typeface generalFont = null;
	public static int currentTab = R.id.textPane;
	//private Folio currentFolio;
	public boolean didShowIntro = false;

	private static MainActivity mainActivityInstance = null;
	public TextPageGroup textPageGroup = null;
	public SearchPageGroup searchPageGroup = null;
	public ContentPageGroup contentPageGroup = null;
	public DatabasePageGroup databasePageGroup = null;
	public SharedPreferences preferences = null;
	//public MainWebViewClient webClient = null;
	//public static MainJavascriptInterface javascriptInterface = null;
	
	private int previousPane = -1;
    private boolean firstResume = true;

	private Handler mHandler;
	private HashMap<String, String> mStringMap = new HashMap<String, String>();
	private ArrayList<String> currentBottomMenu = new ArrayList<String>();
	private SparseArray<String> currentBottomMenuCommands = new SparseArray<String>();


	VBDownloader vbDownloader = new VBDownloader();


	/*
	 * Creating main window of application
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainActivityInstance = this;

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

		/**
		 * Before we do anything, are the files we expect already here and
		 * delivered (presumably by Market) For free titles, this is probably
		 * worth doing. (so no Market request is necessary)
		 */

		mHandler = new Handler();



// when initialize
		registerReceiver(vbDownloader, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		VBDownloader.onStart();

		// creating and linking user interface
		initializeUserInterface();

		//
		// get list of files available
		//
        FolioLibraryService service = FolioLibraryService.getInstance();
        service.onCreate(this);

    }

    public void post(Runnable action) {
        mHandler.post(action);
    }

    public void postDelayed(Runnable action, long millis) {
        mHandler.postDelayed(action, millis);
    }

	public void initializeUserInterface() {

		setContentView(R.layout.activity_main);

		if (generalFont == null) {
			generalFont = Typeface.createFromAsset(getAssets(),
					"assets/vuArialPlus.ttf");
		}

		/*if (javascriptInterface == null) {
			javascriptInterface = new MainJavascriptInterface(this);
		}*/

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);

        FlatParagraph.setFontMultiplyer(preferences.getFloat("textFontSize", 1.5f));
        FDCharFormat.setMultiplySpaces(preferences.getFloat("textLineSpace", 1.2f));
        FlatParagraph.setDefaultFont(preferences.getString("textFontName","Times"));


        contentPageGroup = new ContentPageGroup(this);
		textPageGroup = new TextPageGroup(this);
		searchPageGroup = new SearchPageGroup(this);
        databasePageGroup = new DatabasePageGroup(this);

		ImageButton button = (ImageButton) findViewById(R.id.buttonShowMenu);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//showBottomMenu();
                textPageGroup.showTextStylesDialog();
			}
		});


    }

	public void showBottomMenu() {
		showPopupMenu("BottomMenu");
	}

	public void showPopupMenu(String menuName) {

		DialogTextViewActions dialog = new DialogTextViewActions();
		dialog.setDelegate(this);

		currentBottomMenu.clear();
		currentBottomMenuCommands.clear();

		if (menuName.equals("BottomMenu")) {
			currentBottomMenuCommands.put(currentBottomMenu.size(), "search");
			currentBottomMenu.add("Search");

			currentBottomMenuCommands.put(currentBottomMenu.size(), "contents");
			currentBottomMenu.add("Show Contents");

			currentBottomMenuCommands.put(currentBottomMenu.size(), "location");
			currentBottomMenu.add("Show Location");

			currentBottomMenuCommands
					.put(currentBottomMenu.size(), "bookmarks");
			currentBottomMenu.add("Show Bookmarks");

			currentBottomMenuCommands
					.put(currentBottomMenu.size(), "textstyle");
			currentBottomMenu.add("Text Styles");
		} else if (menuName.equals("EditMenuContents")) {
			currentBottomMenuCommands.put(currentBottomMenu.size(),
					"gotocontents");
			currentBottomMenu.add("Go To Contents");
		}

		CharSequence[] items = currentBottomMenu
				.toArray(new CharSequence[currentBottomMenu.size()]);
		dialog.setItems(items);
		dialog.show(getFragmentManager(), "textviewactions");

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

		if (key.equals("cont_high")) {
			contentPageGroup.onHighlightsChanged();
		} else if (key.equals("cont_notes")) {
			contentPageGroup.onNotesChanged();
		} else if (key.equals("cont_bkmk")) {
			contentPageGroup.onBookmarksChanged();

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String startScreen = sp.getString("startscr", "Text");

		SharedPreferences.Editor ed = sp.edit();
		ed.putInt("lastTextRecord", textPageGroup.getCurrentRecordId());
		if (startScreen.equals("Last Used")) {
			ed.putString("lastTabCode", getCurrentTabCode());
		}
		ed.commit();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();


		if (player != null) {
			player.release();
		}

		FolioLibraryService.onDestroy();

		unregisterReceiver(vbDownloader);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		Log.i("main activity", "menu inflated");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		/*if (currentTab == R.id.contentPane) {
			menu.setGroupVisible(R.id.group_content_items_oper, true);
			MenuItem itemDeleteItems = menu.findItem(R.id.item_delete_items);

			int type = ContentPageGroup.currentContentPage.getType();
			if (type == VBContentRow.TYPE_BOOKMARK
					|| type == VBContentRow.TYPE_NOTE) {
				itemDeleteItems.setEnabled(this.contentPageGroup.getCountOfCheckedItems() > 0);
			} else {
				itemDeleteItems.setEnabled(false);
			}
		} else {
			menu.setGroupVisible(R.id.group_content_items_oper, false);
		}*/

		Log.i("ctxmenu", "onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.action_tab_content) {
			setCurrentTabCode("content");
			return true;
		} else if (item.getItemId() == R.id.action_tab_text) {
			setCurrentTabCode("text");
			return true;
		} else if (item.getItemId() == R.id.action_tab_search) {
			setCurrentTabCode("search");
			return true;
        } else if (item.getItemId() == R.id.action_show_content) {
			setCurrentTabCode("content");
            return true;
        } else if (item.getItemId() == R.id.action_show_bookmarks) {
			setCurrentTabCode("bookmark");
            return true;
        } else if (item.getItemId() == R.id.action_show_notes) {
			setCurrentTabCode("note");
            return true;
        } else if (item.getItemId() == R.id.action_show_highs) {
			setCurrentTabCode("hightext");
            return true;
		} else if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        } else if (item.getItemId() == R.id.app_map) {
			setCurrentTabCode("appmap");
		}

		return false;
	}

	//
	// returns instance of main activity
	//
	public static MainActivity getInstance() {
		return mainActivityInstance;
	}

	public void SetCurrentTab(int viewId) {

        if (viewId == R.id.textPane && FolioLibraryService.getCurrentFolio() == null)
            viewId = R.id.contentPane;

		if (previousPane == viewId)
			return;

		if (previousPane >= 0) {
			View previous = findViewById(previousPane);
			if (previous != null) {
				previous.setVisibility(View.INVISIBLE);
			}
		}

		View next = findViewById(viewId);
		if (next != null) {
			next.setVisibility(View.VISIBLE);
		}

        if (viewId == R.id.databaseFragment)
        {
            if (databasePageGroup != null)
                databasePageGroup.ViewDidAppear();
        }

		currentTab = viewId;
		previousPane = viewId;
	}

	@Override
	public void onBackPressed() {
		if (contentPageGroup.isVisible()) {
			if (contentPageGroup.canGoBack()) {
				contentPageGroup.goBack();
			}
		} else {
			super.onBackPressed();
		}
	}

	public void SetFolio(Folio folio) {
        // sets items for content view
		setCurrentFolio(folio);
		//webClient.currentFolio = folio;
		if (!didShowIntro) {
			contentPageGroup.executeCommand("load contents 0");
		}
		textPageGroup.onSourceChanged();
        searchPageGroup.setCurrentFolio(folio);

        if (folio != null) {
            loadRecordSilently(TextPageGroup.currentRecordId, true);
        }
	}

	public void LoadFolioTextRecords(final int record, final boolean saveToHistory) {

        textPageGroup.messageHandler.post(new Runnable() {
            @Override
            public void run() {
                loadRecordSilently(record, saveToHistory);
            }
        });

	}

	public void loadRecordSilently(int record, boolean saveToHistory) {
		TextPageGroup.currentRecordId = record;
		textPageGroup.getTextView().setRecord(record);
	}

	public void showNoteEditor(int record) {
		DialogEditNote dialog = new DialogEditNote();
		dialog.show(getFragmentManager(), "editnote");
		dialog.setRecord(record);
	}

	public void onErrorUnreachableDestination(String siksa) {
		Context context = this;
		String title = "Unreachable destination";
		String message = "Destination \"" + siksa + "\" is not reachable.";
		String button1String = "OK";

		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(message);
		ad.setPositiveButton(button1String,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
					}
				});

		ad.show();
		textPageGroup.showLoadingLabel(false);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.i("ctxmenu", "onCreateContextMenu");
		MenuInflater inflater = getMenuInflater();
		if (v == textPageGroup.textView) {
			inflater.inflate(R.menu.endless_record_operation, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.el_bookmark) {
			DialogAddBookmark addBkmkDlg = new DialogAddBookmark();
			addBkmkDlg.showMainBookmarkDialogAfter = false;
			addBkmkDlg.currentRecordId = textPageGroup.recordIdForContextMenu;
			addBkmkDlg.show(getFragmentManager(), "addBkmk");
			return true;
		} else if (item.getItemId() == R.id.el_note) {
			showNoteEditor(textPageGroup.recordIdForContextMenu);
			return true;
		} else if (item.getItemId() == R.id.search_template_item) {
            searchPageGroup.newQueryUsingTemplate(item.getTitle());
            return true;
        } else if (item.getItemId() == R.id.el_gotobk) {
            setCurrentTabCode("bookmark");
            return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	public void showPopupWithUrl(String url, int i) {
		DialogPopupText dialog = new DialogPopupText();
		TextPageWebClient webClient = new TextPageWebClient();

		webClient.setDialogFragment(dialog);
		webClient.setContext(this);

		dialog.setWebClient(webClient);
		dialog.setRecord(i);
		dialog.show(getFragmentManager(), "popup");
		dialog.loadUrl(url);

	}

	public void showBookmarkDialog() {
		if (FolioLibraryService.getCurrentFolio() != null) {
			DialogFragment dialog = new DialogBookmarks();
			dialog.show(getFragmentManager(), "bookmarks");
		}
	}

	public void textViewActionSelected(int which) {
		String cmd = currentBottomMenuCommands.get(which);
		if (cmd != null) {
			Log.i("menu", "item selected : " + which + ", command: " + cmd);
			if (cmd.equalsIgnoreCase("contents")) {
				setCurrentTabCode("contents");
			} else if (cmd.equalsIgnoreCase("search")) {
				setCurrentTabCode("search");
			} else if (cmd.equalsIgnoreCase("bookmarks")) {
				showBookmarkDialog();
			} else if (cmd.equalsIgnoreCase("textstyle")) {
				this.textPageGroup.showTextStylesDialog();
			} else if (cmd.equalsIgnoreCase("location")) {
				String message = this.textPageGroup.getCurrentLocation();
				DialogShowLocation dlg = new DialogShowLocation();
				dlg.message = message;
				dlg.show(getFragmentManager(), "location_info");
			} else if (cmd.equalsIgnoreCase("gotocontents")) {
				int currentPageId = textPageGroup.getCurrentContentPage(textPageGroup.recordIdForContextMenu);
				SetCurrentTab(R.id.contentPane);
				contentPageGroup.loadContentPageForRecord(VBContentRow.TYPE_ITEM, currentPageId);
			}
		}
	}

	private static MediaPlayer player = null;

	public void playFile(byte[] data) {

		String string = null;
		if (player == null) {
			player = new MediaPlayer();
		} else {
			player.reset();
		}

		try {
			Log.i("audio", "Length of data: " + data.length);
			FileOutputStream fos = openFileOutput("audio.mp3", 0);
			fos.write(data);
			fos.close();

			File f = getFileStreamPath("audio.mp3");
			if (f.exists()) {
				Log.i("audio", "File at " + f.getAbsolutePath() + " exists");
			}
			string = "file://" + f.getAbsolutePath();
			Log.i("audio", "File input path: " + string);
			player.setDataSource(this, Uri.parse(string));
			player.prepare();
			player.start();
			Log.i("audio", "Player started");

		} catch (Exception ex) {
			// Log.i("exception", ex.getMessage());
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

        // pausing folio database
        FolioLibraryService service = FolioLibraryService.getInstance();
		service.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

        if (firstResume) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            // resuming folio database
            FolioLibraryService service = FolioLibraryService.getInstance();
            if (sp.getInt("lastIntro", 0) == 0) {
                SetCurrentTab(R.id.contentPane);
                SharedPreferences.Editor ed = sp.edit();
                ed.putInt("lastIntro", 1);
                ed.commit();
                contentPageGroup.executeCommand("show intro");
                didShowIntro = true;
            }

			if (!didShowIntro) {
				if (!service.onResume()) {
					SetCurrentTab(R.id.databaseFragment);
					// try to resume prveiously started downloading task
					databasePageGroup.UpdateDownloadingTask(1);
				} else {
					String startScreen = sp.getString("startscr", "Text");
					if (startScreen.equals("Text")) {
						SetCurrentTab(R.id.textPane);
					} else if (startScreen.equals("Contents")) {
						SetCurrentTab(R.id.contentPane);
					} else if (startScreen.equals("Application Map")) {
						setCurrentTabCode("appmap");
					} else if (startScreen.equals("Last Used")) {
						String lastTabCode = sp.getString("lastTabCode", "text");
						setCurrentTabCode(lastTabCode);
					} else {
						SetCurrentTab(R.id.textPane);
					}

					int lastTextRecord = sp.getInt("lastTextRecord", 0);
					LoadFolioTextRecords(lastTextRecord, true);
				}
			}

            firstResume = false;
        }
	}

    public void setCurrentTabCode(String code) {

        if (code.equals("content")) {
            SetCurrentTab(R.id.contentPane);
			contentPageGroup.executeCommand("load contents 0");
            //contentPageGroup.navigate("http://texts/content.vbp?r=0");
        } else if (code.equals("text")) {
            SetCurrentTab(R.id.textPane);
        } else if (code.equals("search")) {
            SetCurrentTab(R.id.searchPane);
        } else if (code.equals("dbinfo")) {
            SetCurrentTab(R.id.databaseFragment);
        } else if (code.equals("appmap")) {
            SetCurrentTab(R.id.contentPane);
			contentPageGroup.executeCommand("show appmap");
            //contentPageGroup.navigate("http://assets/AppMap.htm");
        } else if (code.equals("bookmark")) {
            SetCurrentTab(R.id.contentPane);
			contentPageGroup.executeCommand("load bookmark 0");
            //contentPageGroup.navigate("http://texts/bookmarks.vbp?r=0");
        } else if (code.equals("note")) {
            SetCurrentTab(R.id.contentPane);
			contentPageGroup.executeCommand("load note 0");
            //contentPageGroup.navigate("http://texts/notes.vbp?r=0");
        } else if (code.equals("hightext")) {
            SetCurrentTab(R.id.contentPane);
			contentPageGroup.executeCommand("load hightext 0");
            //contentPageGroup.navigate("http://texts/hightext.vbp?r=0");
        }
    }

    public String getCurrentTabCode() {
        if (contentPageGroup.isVisible()) {
            return contentPageGroup.getLastPageCode();
        } else if (textPageGroup.isVisible()) {
            return "text";
        } else if (searchPageGroup.isVisible()) {
            return "search";
        } else if (databasePageGroup.isVisible()) {
            return "dbinfo";
        }

        return "text";
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==123 && resultCode == RESULT_OK) {
			String str = data.getStringExtra("filePath");
			File f = new File(str);
			if (f.isFile()) {
				if (f.exists()) {
					String absPath = f.getAbsolutePath();
					if (absPath.endsWith(".ivd")) {
						FolioLibraryService service = FolioLibraryService.getInstance();
						service.saveDatabaseFilePath(absPath);
						service.ReloadLibraryIntoViewer(true);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageBoxAnswerOK(HashMap<String, String> mb) {
		Folio folio = FolioLibraryService.getCurrentFolio();
        FolioLibraryService service = FolioLibraryService.getInstance();
		int changed = 0;
		List<VBContentRow> list = null;
		Object data = null;

		if (mb.containsKey("remove_bookmark")) {
			data = mb.get("remove_bookmark");
			list = (List<VBContentRow>) data;
			Log.i("tag", "rembk");
			if (list != null) {
				for (VBContentRow item : list) {
					VBBookmark bkmk = (VBBookmark) item.getTag();
					if (bkmk != null && folio != null) {
						folio.removeBookmark(bkmk);
						changed++;
					}
				}
			}
			if (changed > 0 && service != null) {
				Log.i("tag", "rembk 2");
                service.customReferencesDidChange();
				this.contentPageGroup.reloadPage();
			}

		} else if (mb.containsKey("remove_note")) {
			data = mb.get("remove_note");
			list = (List<VBContentRow>) data;
			if (list != null) {
				for (VBContentRow item : list) {
					VBCustomNotes note = (VBCustomNotes) item.getTag();
					if (note != null) {
						note.setNoteText("");
						changed++;
					}
				}
			}
			if (changed > 0) {
                service.customReferencesDidChange();
				this.contentPageGroup.reloadPage();
			}
		} else if (mb.containsKey("remove_highlighter")) {
			data = mb.get("remove_highlighter");
			list = (List<VBContentRow>) data;
			if (list != null) {
				for (VBContentRow item : list) {
					VBCustomHighlights note = (VBCustomHighlights) item.getTag();
					if (note != null) {
						note.anchors.clear();
						changed++;
					}
				}
			}
			if (changed > 0) {
                service.customReferencesDidChange();
				this.contentPageGroup.reloadPage();
			}
		}
	}

	@Override
	public void messageBoxAnswerCancel(HashMap<String, String> mb) {

	}

	public void setCurrentFolio(Folio currentFolio) {

	}
}
