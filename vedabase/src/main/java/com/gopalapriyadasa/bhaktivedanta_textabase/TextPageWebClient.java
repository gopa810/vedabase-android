package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

import com.gopalapriyadasa.textabase_engine.FlatFileUtils;
import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.FolioObjectRecord;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TextPageWebClient extends WebViewClient {

	public static final String URL_SCHEMA = "http://";
	public static final String URL_TEXTS = URL_SCHEMA + "texts/";
	public static final String URL_HISTORY = URL_SCHEMA + "history/";
	public static final String URL_SEARCH = URL_SCHEMA + "search/";
	public static final String URL_RESOURCES = URL_SCHEMA + "resources/";
	public static final String URL_ASSETS = URL_SCHEMA + "assets/";
	public static final String URL_POPUP = URL_SCHEMA + "popup/";
	public static final String URL_NOTE = URL_SCHEMA + "note/";
	public static final String URL_INLINEPOPUP = URL_SCHEMA + "inlinepopup/";
	public static final String URL_LINKS = URL_SCHEMA + "links/";

	private MainActivity context = null;
	private DialogPopupText dialogFragment = null;

	public boolean isAttachedToDialog() {
		return getDialogFragment() != null;
	}
	
	public void setDialogFragment(DialogPopupText dialogFragment) {
		this.dialogFragment = dialogFragment;
	}

	public DialogPopupText getDialogFragment() {
		return dialogFragment;
	}
	
	public MainActivity getContext() {
		return context;
	}

	public void setContext(MainActivity context) {
		this.context = context;
	}

	@Override
	public void onLoadResource(WebView view, String url) {
		Log.i("web", "onLoadResource " + url);
		super.onLoadResource(view, url);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		Log.i("web", "onPageFinished " + url);
		super.onPageFinished(view, url);
		context.textPageGroup.showLoadingLabel(false);
		if (dialogFragment != null) {
			dialogFragment.setLoadingVisibility(false);
		}
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		Log.i("web", "onPageStarted " + url);
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		Log.i("web", "shouldInterceptRequest " + url + ", dialog:" + (isAttachedToDialog() ? "true" : "false"));
		InputStream stream = null;
		String mimeType = null;
		String encoding = null;
		Folio current = FolioLibraryService.getCurrentFolio();

		if (current == null) {
			stream = new ByteArrayInputStream("<p>Folio is not available</p>".getBytes());
			return new WebResourceResponse(mimeType, encoding, stream);
		}

		if (url.startsWith(URL_SCHEMA)) {
			if (url.startsWith(URL_TEXTS)) {
				if (isAttachedToDialog()) {
					DialogFragment dialog = getDialogFragment();
					dialog.dismiss();
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_TEXTS
								.length()));
						context.SetCurrentTab(R.id.textPane);
						context.LoadFolioTextRecords(record, true);
					}
				} else {
					String htmlText = null;
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_TEXTS
								.length()));
						htmlText = current.dataForRecordRange(record, record + 50);
					} else {
						htmlText = "<html><body><h1>Unfortunately...</h1><p>This page was not found.</p></body></html>";
					}
					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_HISTORY)) {
				// difference between TEXTS and HISTORY is, that HISTORY does not save this to history
				if (isAttachedToDialog()) {
					DialogFragment dialog = getDialogFragment();
					dialog.dismiss();
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_TEXTS
								.length()));
						context.SetCurrentTab(R.id.textPane);
						context.LoadFolioTextRecords(record, true);
					}
				} else {
					String htmlText = null;
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_HISTORY
								.length()));
						htmlText = current.dataForRecordRange(record, record + 50);
					} else {
						htmlText = "<html><body><h1>Unfortunately...</h1><p>This page was not found.</p></body></html>";
					}
					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_SEARCH)) {
				if (isAttachedToDialog()) {
					DialogFragment dialog = getDialogFragment();
					dialog.dismiss();
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_TEXTS
								.length()));
						context.SetCurrentTab(R.id.textPane);
						context.LoadFolioTextRecords(record, true);
					}
				} else {
					String htmlText = null;
					if (current != null) {
						int record = Integer.parseInt(url.substring(URL_SEARCH
								.length()));
						htmlText = current.dataForRecordRange(record, record + 50);
						htmlText = current.highlightWords(htmlText);
					} else {
						htmlText = "<html><body><h1>Unfortunately...</h1><p>This page was not found.</p></body></html>";
					}
					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_POPUP)) {
				if (isAttachedToDialog()) {
					String popupName = FlatFileUtils.decodeLinkSafeString(url
							.substring(URL_POPUP.length()));
					String htmlText = current != null ? current.htmlTextForPopup(popupName) : "";

					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_NOTE)) {
				if (isAttachedToDialog()) {
					int record = Integer.parseInt(url.substring(URL_NOTE.length()));
					String htmlText = current != null ? current.htmlTextForNoteRecord(record) : "";

					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_INLINEPOPUP)) {
				if (isAttachedToDialog()) {
					String link = url.substring(URL_INLINEPOPUP.length());
					String[] pathComponents = link.split("/");
					String htmlText = "";
					if (pathComponents.length > 2) {
	
						int record;
						int popup;
						if (pathComponents[0].equals("RD")) {
							Log.i("inline-rd", "before loading html");
							record = Integer.parseInt(pathComponents[1]);
							popup = Integer.parseInt(pathComponents[2]);
							htmlText = current.textForPopupNumber(record, popup);
							Log.i("inline-rd", "after loading html " + htmlText);
						} else if (pathComponents[0].equals("DP")) {
							Log.i("inline-dp", "before loading html");
							popup = Integer.parseInt(pathComponents[2]);
							htmlText = current.htmlTextForPopupForPopupNumber(
											FlatFileUtils.decodeLinkSafeString(pathComponents[1]),
									//pathComponents
											popup);
							Log.i("inline-dp", "after loading html " + htmlText);
						}

					} else {
						Log.i("inline", "not available");
						htmlText = current.envelopeHtmlInBody("<h1>Popup not available</h1>");
					}
					
					mimeType = "text/html";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(htmlText.getBytes());
				}
			} else if (url.startsWith(URL_LINKS)) {
				String link = url.substring(URL_LINKS.length());
				String[] arr = link.split("/");
				if (arr.length > 0) {
					if (arr[0].equals("DL") && arr.length > 1) {
						String objectName = FlatFileUtils
								.decodeLinkSafeString(arr[1]);
						FolioObjectRecord obj = current.findObject(objectName,
								FolioObjectRecord.DATA_STREAM);
						if (obj != null) {
							stream = obj.objectStream;
							mimeType = obj.objectType;
						}
					}
				}
				// this will be handled in shouldOverrideUrlRequest
			} else if (url.startsWith(URL_RESOURCES)) {

				String resource = url.substring(URL_RESOURCES.length());
				if (resource.equals("styles.css")) {
					mimeType = "text/css";
					encoding = "UTF-8";
					stream = new ByteArrayInputStream(current.getStyles()
							.getBytes());
				}
			} else if (url.startsWith(URL_ASSETS)) {
				String resource = url.substring(URL_ASSETS.length());
				try {
					stream = context.getAssets().open("assets/" + resource);
					encoding = "";
					mimeType = FlatFileUtils.getMimeType(FlatFileUtils
							.getFileExt(resource));
				} catch (Exception e) {
					Log.e("TextPageWebClient",
							"some error in getting asset with name " + resource);
				}
			}
		}

		if (stream != null && mimeType != null) {
			return new WebResourceResponse(mimeType, encoding, stream);
		} else {
			return super.shouldInterceptRequest(view, url);
		}
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.i("web", "shouldOverrideUrlLoading " + url + ", dialog:" + (isAttachedToDialog() ? "true" : "false"));
		Folio current = FolioLibraryService.getCurrentFolio();
		if (current == null)
			return false;

		if (url.startsWith(URL_POPUP)) {
			if (!isAttachedToDialog()) {
				//String popupName = FlatFileUtils.decodeLinkSafeString(url
				//		.substring(URL_POPUP.length()));
				//String htmlText = current.htmlTextForPopup(popupName);
				getContext().showPopupWithUrl(url, -1);
				return true;
			} else {
				return false;
			}
		} else if (url.startsWith(URL_NOTE)) {
			if (!isAttachedToDialog()) {
				int record = Integer.parseInt(url.substring(URL_NOTE.length()));
				//String htmlText = current.htmlTextForNoteRecord(record);
				getContext().showPopupWithUrl(url, record);
				return true;
			} else {
				return false;
			}
		} else if (url.startsWith(URL_INLINEPOPUP)) {
			if (!isAttachedToDialog()) {
				/*String link = url.substring(URL_INLINEPOPUP.length());
				String[] pathComponents = link.split("/");
				String htmlText = "";
				if (pathComponents.length > 2) {
	
					int record;
					int popup;
					if (pathComponents[0].equals("RD")) {
						record = Integer.parseInt(pathComponents[1]);
						popup = Integer.parseInt(pathComponents[2]);
						htmlText = current.textForPopupNumber(record, popup);
					} else if (pathComponents[0].equals("DP")) {
						popup = Integer.parseInt(pathComponents[2]);
						htmlText = current
								.htmlTextForPopupForPopupNumber(
										FlatFileUtils
												.decodeLinkSafeString(pathComponents[1]),
										popup);
					}
					getContext().showPopupWithHtmlText(htmlText);
				}*/
				getContext().showPopupWithUrl(url, -1);
				return true;
			} else {
				return false;
			}
		} else if (url.startsWith(URL_LINKS)) {
			String link = url.substring(URL_LINKS.length());
			String[] arr = link.split("/");
			if (arr.length > 1) {
				if (isAttachedToDialog()) {
					DialogFragment dialog = getDialogFragment();
					dialog.dismiss();
				}

				if (arr[0].equals("ML")) {
					String menuCommand = FlatFileUtils
							.decodeLinkSafeString(arr[1]);
					if (menuCommand.equals("Go Back")) {

					}
				} else if (arr[0].equals("QL")) {
					String siksa = FlatFileUtils.decodeLinkSafeString(arr[1]);
					int record = current.searchFirstRecord(siksa);
					Log.i("lynx", siksa);
					Log.i("lynx", "rec = " + record);
					if (record > 0) {
						navigateWebView(record);
					} else {
						getContext().onErrorUnreachableDestination(siksa);
					}
				} else if (arr[0].equals("EN")) {
					String siksa = FlatFileUtils.decodeLinkSafeString(arr[1]);
					int record = current.searchFirstRecord(siksa);
					if (record > 0) {
						navigateWebView(record);
					} else {
						getContext().onErrorUnreachableDestination(siksa);
					}
				} else if (arr[0].equals("JL")) {
					String jumpDestination = FlatFileUtils
							.decodeLinkSafeString(arr[1]);
					int record = current.findJumpDestination(jumpDestination);
					navigateWebView(record);
				}
			}
			return true;
		}

		return false;
	}

	public void navigateWebView(int record) {

		// this was calling getContext().navigateWebView(record)
		// and that method was implemented in MainActivty as:
		// 	public void navigateWebView(int record) {
		//    LoadFolioTextRecords(record, false);
		//    SetCurrentTab(R.id.textPane);
		//  }
	}

}
