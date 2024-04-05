package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.VBBookmark;
import com.gopalapriyadasa.textabase_engine.VBContentRow;
import com.gopalapriyadasa.textabase_engine.VBCustomHighlights;
import com.gopalapriyadasa.textabase_engine.VBCustomNotes;

/**
 * Created by Gopa702 on 6/10/2016.
 */

public class MainJavascriptInterface {

    // main activity
    MainActivity mContext;

    // historical records
    private String mLastContentPage = "0";
    private String mLastBookmarksPage = "0";
    private String mLastNotesPage = "0";
    private String mLastHighsPage = "0";

    MainJavascriptInterface(MainActivity ctx) {
        mContext = ctx;
    }

    
    @JavascriptInterface
    public void showContentPage(final String recId) {

        if (recId != null)
            mLastContentPage = recId;

        Runnable action = new Runnable() {
            @Override
            public void run() {
                //getWebView().loadUrl("about:blank");
                getWebView().loadUrl("http://texts/content.vbp?r=" + mLastContentPage);
            }
        };

        getWebView().post(action);
    }

    private WebView getWebView() {
        //return getWebView();
        return null;
    }

    @JavascriptInterface
    public String getDefaultBackgroundColor() {
        return UIManager.getHtmlHexColor(R.color.general_background);
    }

    @JavascriptInterface
    public void showBookmarkPage(final String recId) {

        if (recId != null)
            mLastBookmarksPage = recId;

        Runnable action = new Runnable() {
            @Override
            public void run() {
                //getWebView().loadUrl("about:blank");
                getWebView().loadUrl("http://texts/bookmarks.vbp?r=" + mLastBookmarksPage);
            }
        };

        getWebView().post(action);
    }

    @JavascriptInterface
    public void selectCurrentTab(final String tabName) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                mContext.setCurrentTabCode(tabName);
            }
        };

        getWebView().post(action);
    }

    @JavascriptInterface
    public void navigate(final String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(browserIntent);
    }

    @JavascriptInterface
    public void showDialog(final String dialogName) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                if (dialogName.equals("settings")) {
                    Intent settings = new Intent(mContext, SettingsActivity.class);
                    mContext.startActivity(settings);
                }
            }
        };

        getWebView().post(action);
    }

    @JavascriptInterface
    public void showNotesPage(final String recId) {

        if (recId != null)
            mLastNotesPage = recId;

        Runnable action = new Runnable() {
            @Override
            public void run() {
                //getWebView().loadUrl("about:blank");
                getWebView().loadUrl("http://texts/notes.vbp?r=" + mLastNotesPage);
            }
        };

        getWebView().post(action);
    }

    @JavascriptInterface
    public void showHighsPage(final String recId) {
        if (recId != null)
            mLastHighsPage = recId;

        Runnable action = new Runnable() {
            @Override
            public void run() {
                getWebView().loadUrl("http://texts/hightext.vbp?r=" + mLastHighsPage);
            }
        };

        getWebView().post(action);
    }

    public Folio getCurrentFolio() {
        //return mContext.webClient.currentFolio;
        return null;
    }

    @JavascriptInterface
    public void deleteContentItem(final String itemType, final String itemId) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                int itemTypeA = Integer.parseInt(itemType);
                int itemIdA = Integer.parseInt(itemId);

                Folio currentFolio = getCurrentFolio();

                if (itemTypeA == VBContentRow.TYPE_BOOKMARK) {
                    currentFolio.removeBookmarkWithId(itemIdA);
                } else if (itemTypeA == VBContentRow.TYPE_NOTE) {
                    currentFolio.removeRecordNoteWithId(itemIdA);
                }

                getWebView().reload();
            }
        };

        getWebView().post(action);
    }

    @JavascriptInterface
    public void moveCustomItem(final String sItemId, final String itemType, final String sTargetId) {
        int itemId = Integer.parseInt(sItemId);
        int targetId = Integer.parseInt(sTargetId);

        Folio cf = getCurrentFolio();
        if (itemType.equals("note")) {
            VBCustomNotes nt = cf.findRecordNoteById(itemId);
            if (nt != null) {
                nt.setParentId(targetId);
            }
        } else if (itemType.equals("bookmark")) {
            VBBookmark bk = cf.findBookmarkById(itemId);
            if (bk != null) {
                bk.setParentId(targetId);
            }
        } else if (itemType.equals("hightext")) {
            VBCustomHighlights ch = cf.findCustomHighlightsById(itemId);
            if (ch != null) {
                ch.setParentId(targetId);
            }
        }

        FolioLibraryService.getInstance().customReferencesDidChange();
    }

    @JavascriptInterface
    public void createNewDirectory(final String pageType, final String pageId, final String text) {
        Log.i("javascript", "createNewDirectory for page " + pageType + ", " + pageId + ", " + text);
        // create dir
        // reload page
        Runnable action = new Runnable() {
            @Override
            public void run() {
                Folio currentFolio = getCurrentFolio();
                int nPageType = Integer.parseInt(pageType);
                if (nPageType == VBContentRow.TYPE_BOOKMARK) {
                    VBBookmark bm = new VBBookmark();
                    bm.setRecordId(-1);
                    bm.setParentId(Integer.parseInt(pageId));
                    bm.setName(text);
                    currentFolio.addBookmark(bm);
                } else if (nPageType == VBContentRow.TYPE_NOTE) {
                    VBCustomNotes rn = new VBCustomNotes();
                    rn.setRecordId(-1);
                    rn.setParentId(Integer.parseInt(pageId));
                    rn.setNoteText(text);
                    currentFolio.addNote(rn);
                } else if (nPageType == VBContentRow.TYPE_HIGHLIGHTER) {
                    VBCustomHighlights rn = new VBCustomHighlights();
                    rn.setRecordId(-1);
                    rn.setParentId(Integer.parseInt(pageId));
                    rn.setHighlightedText(text);
                    currentFolio.addCustomHighlight(rn);
                }
                getWebView().reload();
            }
        };

        getWebView().post(action);
        FolioLibraryService.getInstance().customReferencesDidChange();

    }

    @JavascriptInterface
    public void showTextPage(final String recIdStart, final String recIdStop) {

        // loading texts into webview in range recIdStart and recIdStop
        final int rs = Integer.parseInt(recIdStart);
        final int re = Integer.parseInt(recIdStop);

        Runnable action = new Runnable() {
            @Override
            public void run() {
                mContext.SetCurrentTab(R.id.textPane);
                mContext.LoadFolioTextRecords(rs, true);
            }
        };

        getWebView().post(action);

    }

    @JavascriptInterface
    public void showTextRecord(final String recId) {

        final int recIdStart = Integer.parseInt(recId);

        Runnable action = new Runnable() {
            @Override
            public void run() {
                mContext.SetCurrentTab(R.id.textPane);
                mContext.LoadFolioTextRecords(recIdStart, true);
            }
        };

        getWebView().post(action);

    }


}
