package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gopalapriyadasa.html_doc.HtmlElement;
import com.gopalapriyadasa.html_doc.HtmlNode;
import com.gopalapriyadasa.html_doc.HtmlStyle;
import com.gopalapriyadasa.html_doc.HtmlStyleCollection;
import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.FolioObjectRecord;
import com.gopalapriyadasa.textabase_engine.VBBookmark;
import com.gopalapriyadasa.textabase_engine.VBContentRow;
import com.gopalapriyadasa.textabase_engine.VBCustomHighlights;
import com.gopalapriyadasa.textabase_engine.VBCustomNotes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by Gopa702 on 6/8/2016.
 */
public class MainWebViewClient extends WebViewClient {

    // unique id counter for html pages
    private int pageCounter = 1;

    // current FOLIO database
    //public Folio currentFolio = FolioLibraryService.getCurrentFolio();

    // user interface content group page
    public ContentPageGroup contentPage = null;


    private String lastPageCode = "content";

    @Override
    public boolean shouldOverrideUrlLoading(WebView wv, String url) {
        //wv.loadUrl("");
        return true;
    }

    //
    // Intercepting request for some resource
    //
    @Override
    public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {

        try {
            //
            // if we have FOLIO database opened
            // then read from it
            // if we don't, we return default implementation
            //
            Log.i("trackurl", url);
            URL tracked = new URL(url);

            String urlQuery = tracked.getQuery();
            String urlHost = tracked.getHost();
            String urlPath = tracked.getPath().substring(1);
            Folio currentFolio = FolioLibraryService.getCurrentFolio();

            if (urlHost.equals("texts")) {
                if (currentFolio != null) {
                    if (urlPath.equalsIgnoreCase("content.vbp")) {
                        //
                        // this is related to content
                        //
                        setLastPageCode("content");
                        String record = getQueryValue(urlQuery, "r");
                        return buildContentPageForRecord(VBContentRow.TYPE_ITEM, Integer.parseInt(record));

                    } else if (urlPath.equals("bookmarks.vbp")) {
                        //
                        // this is related to content
                        //
                        setLastPageCode("bookmark");
                        String record = getQueryValue(urlQuery, "r");
                        return buildContentPageForRecord(VBContentRow.TYPE_BOOKMARK, Integer.parseInt(record));

                    } else if (urlPath.equals("notes.vbp")) {
                        //
                        // this is related to notes
                        //
                        setLastPageCode("note");
                        String record = getQueryValue(urlQuery, "r");
                        return buildContentPageForRecord(VBContentRow.TYPE_NOTE, Integer.parseInt(record));

                    } else if (urlPath.equals("hightext.vbp")) {
                        //
                        // this is related to highlighted text
                        //
                        setLastPageCode("hightext");
                        String record = getQueryValue(urlQuery, "r");
                        return buildContentPageForRecord(VBContentRow.TYPE_HIGHLIGHTER, Integer.parseInt(record));

                    }
                } else {
                    // default page with notice about empty DB
                    setLastPageCode("empty");
                    InputStream asset = UIManager.getAssetStream("assets/EmptyDb.htm");
                    return new WebResourceResponse("text/html", "UTF-8", asset);
                }
            } else if (urlHost.equals("assets")) {

                // asset name starts with asset/<filename>
                InputStream asset = UIManager.getAssetStream("assets/" + urlPath);

                if (urlPath.equals("AppMap.htm")) {
                    setLastPageCode("appmap");
                }

                return new WebResourceResponse(getMimeType(urlPath), "UTF-8", asset);
            } else if (urlHost.equals("resources")) {

                if (urlPath.equals("styles.css")) {
                    return createWebResponseFromString("text/css", "UTF-8", currentFolio.getStyles());
                }

            } else if (urlHost.equals("objects")) {
                String objectName = URLDecoder.decode(urlPath, "UTF-8");
                FolioObjectRecord obj = currentFolio.findObject(objectName, FolioObjectRecord.DATA_STREAM);
                if (obj != null) {
                    return new WebResourceResponse(obj.objectType, "UTF-8", obj.objectStream);
                }
            }

            return createWebResponseFromString("text/html", "UTF-8", "No Page");

        } catch (Exception ex) {
            return super.shouldInterceptRequest(view, url);
        }
    }

    //
    // Create WebResponse from string
    //
    public WebResourceResponse createWebResponseFromString(String type, String encoded, String str) throws UnsupportedEncodingException {

        InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
        return new WebResourceResponse(type, encoded, is);
    }

    //
    // returns value of given parameter in URL query part
    //
    public String getQueryValue(String query, String paramName) {
        String [] params = query.split("&");
        for (String paramNV : params) {
            String [] paramPair = paramNV.split("=");
            if (paramPair.length > 0) {
                if (paramPair[0].equals(paramName)) {
                    if (paramPair.length > 1)
                        return paramPair[1];
                    else
                        return "1";
                }
            }
        }
        return "";
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getItemTitle(int itemType, int recordId) {
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
            Folio currentFolio = FolioLibraryService.getCurrentFolio();
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

    public int getItemParent(int itemType, int recordId) {

        int parentId = 0;
        Folio currentFolio = FolioLibraryService.getCurrentFolio();
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

    public String getItemPage(int itemType) {

        String str = "";

        switch(itemType) {
            case VBContentRow.TYPE_BOOKMARK:
                str = "bookmarks.vbp";
                break;
            case VBContentRow.TYPE_NOTE:
                str = "notes.vbp";
                break;
            case VBContentRow.TYPE_HIGHLIGHTER:
                str = "hightext.vbp";
                break;
            case VBContentRow.TYPE_ITEM:
                str = "content.vbp";
                break;
        }

        return str;
    }

    //
    // Inserting single TITLE item into array of items
    //
    public void insertSectionTitle(ArrayList<VBContentRow> list, int itemType) {

        insertRowText(list, getItemTitle(itemType, 0), VBContentRow.TYPE_TITLE);
    }

    public void insertRowText(ArrayList<VBContentRow> list, String text, int rowType) {
        VBContentRow row = new VBContentRow();
        row.setType(rowType);
        row.setMainTitle(text);
        row.setParent(0);
        row.setRecord(0);
        row.setNavigable(false);
        row.setExpandable(false);

        list.add(row);
    }

    //
    //
    //
    public String buildCreateDirectorySection() {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style='text-align:center;'>");
        sb.append("<div id=\"ndirblock\" style='display:none;'>");
        sb.append("<input style='font-size:160%' id=\"ndirname\" type=\"text\" size=\"20\"></input> &nbsp;&nbsp;&nbsp;&nbsp; \n");
        sb.append("<span onclick=\"Textabase.createNewDirectory(" + contentPage.getCurrentPageType() + "," + contentPage.getCurrentPageId()
                + ", document.getElementById('ndirname').value );\" class=\"actionlink\">Create</span>");
        sb.append("&nbsp;&nbsp;| &nbsp;&nbsp;");
        sb.append("<span onclick=\"hideElem('ndirblock');showElem('ndirstart');\" class=\"actionlink\">Cancel</span>\n");
        sb.append("</div>");
        sb.append("<div id=\"ndirstart\">");
        sb.append("<span onclick=\"hideElem('ndirstart');showElem('ndirblock');\" class=\"actionlink\">Create new directory</span>");
        sb.append("&nbsp;&nbsp;| &nbsp;&nbsp;");
        sb.append("<span class='actionlink' onclick=\"showEditIcons();\">Edit List</span>");
        sb.append("</div>");
        sb.append("<div class=\"movingNotice\"><div style='text-align:center;margin:16pt;padding:8pt;'");
        sb.append("<span style='text-align:center;color:red' onclick=\"onCancelMoving()\" class=\"actionLink\">Cancel Moving Operation</span>");
        sb.append("</div></div>");

        return sb.toString();
    }

    //
    //
    //
    public WebResourceResponse buildContentPageForRecord(int itemType, int recordId) throws UnsupportedEncodingException {

        boolean showEditButton = false;
        boolean initContents = false, initBookmarks = false, initNotes = false, initHighs = false;
        String mainTitle;
        String placement;
        //StringBuilder htmlPage = new StringBuilder();
        //VBContentRow item = contentPage.currentContentPage;
        ArrayList<VBContentRow> itemsList = contentPage.getCurrentRows();

        Folio currentFolio = FolioLibraryService.getCurrentFolio();
        if (currentFolio == null) {
            return createWebResponseFromString("text/html", "UTF-8", "No page.");
        }

        itemsList.clear();
        contentPage.setCurrentPageType(itemType);
        contentPage.setCurrentPageId(recordId);


        if (itemType == VBContentRow.TYPE_ITEM ||
                itemType == VBContentRow.TYPE_BACK) {
            initContents = true;
            itemsList.addAll(currentFolio.findContentItemsByParent(recordId));
        } else if (itemType == VBContentRow.TYPE_BOOKMARK) {
            initBookmarks = true;
            //insertSectionTitle(itemsList, VBContentRow.TYPE_BOOKMARK);
            currentFolio.getBookmarksContentItems(itemsList, recordId);
            insertRowText(itemsList, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
            showEditButton = true;
        } else if (itemType == VBContentRow.TYPE_NOTE) {
            initNotes = true;
            //insertSectionTitle(itemsList, VBContentRow.TYPE_NOTE);
            currentFolio.getNotesContentItems(itemsList, recordId);
            insertRowText(itemsList, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
            showEditButton = true;
        } else if (itemType == VBContentRow.TYPE_HIGHLIGHTER) {
            initHighs = true;
            //insertSectionTitle(itemsList, VBContentRow.TYPE_NOTE);
            currentFolio.getHighlightsContentItems(itemsList, recordId);
            insertRowText(itemsList, buildCreateDirectorySection(), VBContentRow.TYPE_NOTICE);
            showEditButton = true;
        }

        HtmlElement ehtml = new HtmlElement("html");
        HtmlElement e1, e2, e3, erow;
        HtmlElement ebody, currItem;

        String clickAction;
        HtmlStyleCollection styles = new HtmlStyleCollection();
        HtmlStyle style = null;

        ehtml.setParam("id", "hi" + pageCounter++);
        e1 = ehtml.createNode("head");
        e2 = e1.createNode("meta");
        e2.setParam("charset", "UTF-8");
        e2 = e1.createNode("style");
        e2.appendNode(styles);
        e2 = e1.createNode("script");
        e2.setParam("src", "http://assets/general.js");

        // registering styles
        style = styles.addStyle("contpara");
        style.setStyle("width", "100%");
        style.setStyle("height", "48pt");
        style.setStyle("padding", "4pt");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("conticon");
        style.setStyle("float", "left");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("conttext");
        style.setStyle("padding", "8pt");

        style = styles.addStyle("actionlink");
        style.setStyle("color", "#7f007f");
        style.setStyle("text-decoration", "underline");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("editIcon");
        style.setStyle("width", "48pt");
        style.setStyle("height", "48pt");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("smallIcon");
        style.setStyle("width", "32pt");
        style.setStyle("height", "32pt");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("popupMenuItem");
        style.setStyle("font-weight", "bold");
        style.setStyle("color", "white");
        style.setStyle("padding", "8pt");
        style.setStyle("border-bottom", "1px solid white");
        style.setStyle("cursor", "pointer");

        style = styles.addStyle("movingNotice");
        style.setStyle("color", "blue");
        style.setStyle("display", "none");

        // creating body
        ebody = ehtml.createNode("body");
        ebody.setStyle("background", UIManager.getHtmlHexColor(R.color.general_background));
        ebody.setStyle("padding-left", "4%");
        ebody.setStyle("padding-right", "4%");

        //-----
        // local variables
        //
        e1 = ebody.createNode("div");
        e1.setParam("id", "localVars");
        e1.setStyle("display", "none");
        e2 = e1.createNode("div");
        e2.setParam("id", "currentId");
        e2.appendText("0");
        e2 = e1.createNode("div");
        e2.setParam("id", "currentTitle");
        e2.appendText("0");
        e2 = e1.createNode("div");
        e2.setParam("id", "currentType");
        e2.appendText("0");
        e2 = e1.createNode("div");
        e2.setParam("id", "movingMode");
        e2.appendText("0");



        //-------------
        // popup dialog for deleting
        //
        e1 = ebody.createNode("div");
        e1.setParam("id", "dialogDelete");
        e1.setStyle("background", "#5f7f9f");
        e1.setStyle("margin-left", "32pt");
        e1.setStyle("display", "none");
        e1.setStyle("padding", "16pt");
        e2 = e1.createNode("h3");
        e2.setStyle("text-align", "center");
        e2.appendText("Delete item <span id='delDlgText' style='color:white;'></span> ?");
        e1.appendText("<table style='text-align:center;width:100%' cellspacing=8pt>"
                + "<tr><td onclick=\"hideElem('dialogDelete');Textabase.deleteContentItem(getVar('currentType'),getVar('currentId'));\">"
                + "YES<td onclick=\"hideElem('dialogDelete');\">NO</table>");


        // -------------
        // popup menu for EDIT
        //
        e1 = ebody.createNode("div");
        e1.setParam("id", "popupMenuEdit");
        e1.setStyle("background", "#5f7f9f");
        e1.setStyle("padding", "8pt");
        e1.setStyle("margin-left", "32pt");
        e1.setStyle("display", "none");

        e2 = e1.createNode("div");
        e2.setParam("class", "popupMenuItem");
        e2.setParam("onclick", "showMoveDialog();");
        e2.appendText("MOVE");

        e2 = e1.createNode("div");
        e2.setParam("class", "popupMenuItem");
        e2.setParam("onclick", "showDeleteDialog();");
        e2.appendText("DELETE");

        e2 = e1.createNode("div");
        e2.setParam("class", "popupMenuItem");
        e2.setParam("onclick", "hideElem('popupMenuEdit');");
        e2.appendText("CLOSE");


        // ------------------------
        // creating top menu
        //
        e1 = ebody.createNode("table");
        e1.setParam("align", "center");
        e1.setStyle("border", "0px solid black");
        e1.setParam("cellspacing", "8pt");

        e1 = e1.createNode("tr");
        e2 = e1.createNode("td");
        if (itemType != VBContentRow.TYPE_ITEM) {
            e2.appendText("<div onclick=\"Textabase.selectCurrentTab('content')\" style='text-align:center;font-size:75%;cursor:pointer'><img class='smallIcon' src=\"http://assets/content_icon_dir.png\"><br>");
            e2.appendText("Contents</div>");
        }
        if (itemType != VBContentRow.TYPE_BOOKMARK) {
            e2 = e1.createNode("td");
            e2.appendText("<div onclick=\"Textabase.selectCurrentTab('bookmark')\" style='text-align:center;font-size:75%;cursor:pointer'><img class='smallIcon' src=\"http://assets/content_bkmk.png\"><br>");
            e2.appendText("Bookmarks</div>");
        }
        if (itemType != VBContentRow.TYPE_NOTE) {
            e2 = e1.createNode("td");
            e2.appendText("<div onclick=\"Textabase.selectCurrentTab('note')\" style='text-align:center;font-size:75%;cursor:pointer'><img class='smallIcon' src=\"http://assets/content_notes.png\"><br>");
            e2.appendText("Notes</div>");
        }
        if (itemType != VBContentRow.TYPE_HIGHLIGHTER) {
            e2 = e1.createNode("td");
            e2.appendText("<div onclick=\"Textabase.selectCurrentTab('hightext')\" style='text-align:center;font-size:75%;cursor:pointer'><img class='smallIcon' src=\"http://assets/content_hightext.png\"><br>");
            e2.appendText("Highlights</div>");
        }

        e2 = e1.createNode("td");
        e2.appendText("<div onclick=\"Textabase.selectCurrentTab('appmap')\" style='text-align:center;font-size:75%;cursor:pointer'><img class='smallIcon' src=\"http://assets/app_map.png\"><br>");
        e2.appendText("App Map</div>");

        e1 = ebody.createNode("hr");
        e1.setStyle("width", "75%");
        e1.setStyle("height", "1pt");
        e1.setStyle("text-align", "center");

        //---------
        // inserting main title
        //
        if ((mainTitle = getItemTitle(itemType, recordId)) != null) {
            Log.i("javascript", "MainTITLE= " + mainTitle );
            int parentId = getItemParent(itemType, recordId);
            /*e1 = ebody.createNode("table");
            e1.setStyle("width", "100%");
            e1 = e1.createNode("tr");
            e2 = e1.createNode("td");
            e2.setStyle("width", "48pt");*/
            if (parentId != recordId) {
                VBContentRow row = new VBContentRow();
                row.setID(parentId);
                row.setType(VBContentRow.TYPE_BACK);
                row.setMainTitle("[PARENT]");
                row.setSubTitle(Integer.toString(itemType));
                itemsList.add(0, row);
            }

            VBContentRow row = new VBContentRow();
            row.setType(VBContentRow.TYPE_TITLE);
            row.setMainTitle(mainTitle);
            itemsList.add(0, row);
/*            e2 = e1.createNode("td");
            if (recordId != 0) {
                String strName = getItemTitle(itemType, -1);
                if (strName != null) {
                    e3 = e2.createNode("h3");
                    e3.setStyle("text-align", "center");
                    e3.appendText(strName);
                }
            }
            e3 = e2.createNode("h2");
            e3.setStyle("text-align", "center");
            e3.appendText(mainTitle);

            e2 = e1.createNode("td");
            e2.setStyle("width", "48pt");
            e2.appendText("&nbsp;");
//            if (itemsList.size() > 0 && showEditButton) {
//                e2.appendText("<span class='actionlink' onclick=\"showEditIcons();\">EDIT</span>");
//            }
*/
        }


        boolean itemIsDir = false;

        for (VBContentRow enumItem: itemsList) {
            Log.i("conth", "TRANS: = " + TextUtils.htmlEncode(enumItem.getMainTitle()));
            int enumItemType = enumItem.getType();

            if (enumItemType == VBContentRow.TYPE_TITLE) {
                e1 = ebody.createNode("div");
                e2 = e1.createNode("h3");
                e2.setStyle("text-align", "center");
                e2.appendText(enumItem.getMainTitle());
                e2 = ebody.createNode("hr");
                e2.setStyle("width", "75%");
                e2.setStyle("height", "1pt");
                e2.setStyle("text-align", "center");
            } else if (enumItemType == VBContentRow.TYPE_NOTICE) {

                e2 = ebody.createNode("p");
                e2.setStyle("text-align", "center");
                e2.setStyle("text-style", "italic");
                e2.appendText(enumItem.getMainTitle());

            } else if (enumItemType == VBContentRow.TYPE_BACK) {
                e1 = currItem = ebody.createNode("div");
                e1.setParam("class", "contpara");

                switch(Integer.parseInt(enumItem.getSubTitle())) {
                    case VBContentRow.TYPE_BOOKMARK:
                        e1.setParam("onclick", "onClickBookmarkDir('" + enumItem.getID() + "');");
                        break;
                    case VBContentRow.TYPE_ITEM:
                        e1.setParam("onclick", "Textabase.showContentPage('" + enumItem.getID() + "');");
                        break;
                    case VBContentRow.TYPE_NOTE:
                        e1.setParam("onclick", "onClickNoteDir('" + enumItem.getID() + "');");
                        break;
                    case VBContentRow.TYPE_HIGHLIGHTER:
                        e1.setParam("onclick", "onClickHighsDir('" + enumItem.getID() + "');");
                        break;
                    default:
                        break;
                }

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e3 = e2.createNode("img");
                e3.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                e3.setParam("src", "http://assets/content_up.png");
                e3.setStyle("width", "48pt");
                e3.setStyle("height", "48pt");

                e2 = e1.createNode("div");
                e2.setStyle("width", "8pt");
                e2.setStyle("float", "left");
                e2.appendText("&nbsp;");

                e2 = e1.createNode("div");
                e2.setParam("class", "conttext");
                e2.appendText("[PARENT]");
                e2.appendText("<br><span class='movingNotice'>Tap here to move selected item into this directory</span>");

            } else if (enumItemType == VBContentRow.TYPE_BOOKMARK) {
                e1 = currItem = ebody.createNode("div");
                e1.setParam("class", "contpara");
                e1.setParam("id", "R" + enumItem.getID());

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e3 = e2.createNode("img");
                e3.setParam("class", "editIcon");
                e3.setStyle("display", "none");
                e3.setParam("onclick", String.format(Locale.getDefault(), "preparePopupMenu('%d','%d');", enumItem.getID(), enumItemType));
                e3.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                e3.setParam("src", "http://assets/edit_icon.png");

                itemIsDir = (enumItem.getRecord() < 0) ? true : false;

                if (itemIsDir) {
                    clickAction = String.format(Locale.getDefault(), "onClickBookmarkDir('%d');",
                            enumItem.getID());
                } else {
                    clickAction = String.format(Locale.getDefault(), "Textabase.showTextRecord('%d');",
                            enumItem.getRecord());
                }

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e2.setParam("onclick", clickAction);
                e2 = e2.createNode("img");
                e2.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                if (itemIsDir) {
                    e2.setParam("src", "http://assets/content_icon_dir.png");
                } else {
                    e2.setParam("src", "http://assets/content_bkmk.png");
                }
                e2.setStyle("width", "48pt");
                e2.setStyle("height", "48pt");

                e2 = e1.createNode("div");
                e2.setStyle("width", "8pt");
                e2.setStyle("float", "left");
                e2.appendText("&nbsp;");

                e2 = e1.createNode("div");
                e2.setParam("class", "conttext");
                e2.setParam("id", "T" + enumItem.getID());
                e2.setParam("onclick", clickAction);
                e2.appendText(enumItem.getMainTitle());
                if (itemIsDir) {
                    e2.appendText("<br><span class='movingNotice'>Tap here to move selected item into this directory</span>");
                }

            } else if (enumItemType == VBContentRow.TYPE_NOTE) {

                e1 = currItem = ebody.createNode("div");
                //e1.setStyle("float", "clear");
                e1.setParam("class", "contpara");
                e1.setParam("id", "R" + enumItem.getID());

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e3 = e2.createNode("img");
                e3.setParam("class", "editIcon");
                e3.setStyle("display", "none");
                e3.setParam("onclick", String.format(Locale.getDefault(), "preparePopupMenu('%d','%d');", enumItem.getID(), enumItemType));
                e3.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                e3.setParam("src", "http://assets/edit_icon.png");

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");

                itemIsDir = (enumItem.getRecord() < 0);
                if (itemIsDir) {
                    clickAction = String.format(Locale.getDefault(), "onClickNoteDir('%d');", enumItem.getID());
                } else {
                    clickAction = String.format(Locale.getDefault(), "Textabase.showTextRecord('%d');", enumItem.getRecord());
                }

                e2 = e2.createNode("img");
                e2.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                if (itemIsDir) {
                    e2.setParam("src", "http://assets/content_icon_dir.png");
                } else {
                    e2.setParam("src", "http://assets/content_notes.png");
                }
                e2.setParam("onclick", clickAction);
                e2.setStyle("width", "48pt");
                e2.setStyle("height", "48pt");

                e2 = e1.createNode("div");
                e2.setStyle("width", "8pt");
                e2.setStyle("float", "left");
                e2.appendText("&nbsp;");

                e2 = e1.createNode("div");
                e2.setParam("id", "T" + enumItem.getID());
                e2.setParam("class", "conttext");
                e2.setParam("onclick", clickAction);

                e2.appendText(enumItem.getMainTitle());
                if (itemIsDir) {
                    e2.appendText("<br><span class='movingNotice'>Tap here to move selected item into this directory</span>");
                }


            } else if (enumItemType == VBContentRow.TYPE_HIGHLIGHTER) {

                e1 = currItem = ebody.createNode("div");
                //e1.setStyle("float", "clear");
                e1.setParam("class", "contpara");
                e1.setParam("id", "R" + enumItem.getID());

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e3 = e2.createNode("img");
                e3.setParam("class", "editIcon");
                e3.setStyle("display", "none");
                e3.setParam("onclick", String.format(Locale.getDefault(), "preparePopupMenu('%d','%d');", enumItem.getID(), enumItemType));
                e3.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                e3.setParam("src", "http://assets/edit_icon.png");

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");

                itemIsDir = (enumItem.getRecord() < 0);
                if (itemIsDir) {
                    clickAction = String.format(Locale.getDefault(), "onClickHighsDir('%d');", enumItem.getID());
                } else {
                    clickAction = String.format(Locale.getDefault(), "Textabase.showTextRecord('%d');", enumItem.getRecord());
                }

                e2 = e2.createNode("img");
                e2.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                if (itemIsDir) {
                    e2.setParam("src", "http://assets/content_icon_dir.png");
                } else {
                    e2.setParam("src", "http://assets/content_hightext.png");
                }
                e2.setParam("onclick", clickAction);
                e2.setStyle("width", "48pt");
                e2.setStyle("height", "48pt");

                e2 = e1.createNode("div");
                e2.setStyle("width", "8pt");
                e2.setStyle("float", "left");
                e2.appendText("&nbsp;");

                e2 = e1.createNode("div");
                e2.setParam("id", "T" + enumItem.getID());
                e2.setParam("class", "conttext");
                e2.setParam("onclick", clickAction);

                // list of colors for easy reference
                if (enumItem.getTag() != null) {
                    VBCustomHighlights note = (VBCustomHighlights) enumItem.getTag();
                    HashSet<String> colors = note.getHtmlColorCodes();
                    for (String color : colors) {
                        e2.appendText(String.format("<span style='color:%s'>&#x25aa;</span> ", color));
                    }
                }
                e2.appendText(enumItem.getMainTitle());
                if (itemIsDir) {
                    e2.appendText("<br><span class='movingNotice'>Tap here to move selected item into this directory</span>");
                }


            } else if (enumItemType == VBContentRow.TYPE_ITEM) {

                e1 = currItem = ebody.createNode("div");
                e1.setParam("class", "contpara");

                e2 = e1.createNode("div");
                e2.setParam("class", "conticon");
                e2 = e2.createNode("img");
                e2.closingType = HtmlNode.HtmlNodeClosingType.AlwaysSingle;
                int nodeType = enumItem.getNodeType();
                if (enumItem.getNodeChildren() == 0)
                    nodeType = 1;
                switch (nodeType) {
                    case 1:
                        e2.setParam("src", "http://assets/content_icon_text.png");
                        currItem.setParam("onclick", String.format(Locale.getDefault(), "Textabase.showTextPage('%d','%d');",
                                enumItem.getRecord(), enumItem.getNextSibling() - 1));
                        break;
                    case 2:
                        e2.setParam("src", "http://assets/content_icon_book.png");
                        currItem.setParam("onclick", String.format(Locale.getDefault(), "Textabase.showContentPage('%d');", enumItem.getRecord()));
                        break;
                    default:
                        e2.setParam("src", "http://assets/content_icon_dir.png");
                        currItem.setParam("onclick", String.format(Locale.getDefault(), "Textabase.showContentPage('%d');", enumItem.getRecord()));
                        break;
                }
                e2.setStyle("width", "48pt");
                e2.setStyle("height", "48pt");

                e1.appendHtml("<div style='width:48pt;float:left'>&nbsp;</div>");

                e1.appendHtml("<div class='conttext'>");
                e1.appendText(enumItem.getMainTitle());
                e1.appendHtml("</div>");
            }
        }

        contentPage.contentListAdapter.notifyDataSetChanged();
        return createWebResponseFromString("text/html", "UTF-8", ehtml.toString());
    }

    public WebResourceResponse buildTextPage(int startRecord, int stopRecord) throws UnsupportedEncodingException {

        String str = "";
        Folio currentFolio = FolioLibraryService.getCurrentFolio();
        if (currentFolio != null) {
            str = currentFolio.dataForRecordRange(startRecord, stopRecord);
        }
        return createWebResponseFromString("text/html", "UTF-8", str);
    }

    public String getLastPageCode() {
        return lastPageCode;
    }

    public void setLastPageCode(String lastPage) {
        this.lastPageCode = lastPage;
    }
}
