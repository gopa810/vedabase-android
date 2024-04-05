package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.gopalapriyadasa.textabase_engine.FlatFileUtils;
//import com.gpsl.vedabase.data.Folio;
//import com.gpsl.vedabase.data.FolioLibraryService;

import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SearchPageWebClient extends WebViewClient {

	public static final String URL_BASE = "vbase://";
	public static final String URL_ASSETS = URL_BASE + "assets/";
	public static final String URL_RESULTS = URL_BASE + "results/";
	
	private MainActivity context = null;

	public MainActivity getContext() {
		return context;
	}

	public void setContext(MainActivity context) {
		this.context = context;
	}


	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		
		InputStream stream = null;
		String mimeType = null;
		String encoding = null;
		//Folio current = FolioLibraryService.getCurrentFolio();

		if (url.startsWith(URL_ASSETS)) {
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
			
		} else if (url.startsWith(URL_RESULTS)) {
			int resultsPage = Integer.parseInt(url.substring(URL_RESULTS.length()));
			
			String htmlText = context.searchPageGroup.getResultsPageHtmlText(resultsPage);
			encoding = "UTF-8";
			stream = new ByteArrayInputStream(htmlText.getBytes());
			mimeType = "text/html";
		}

		if (stream != null && mimeType != null) {
			return new WebResourceResponse(mimeType, encoding, stream);
		} else {
			return super.shouldInterceptRequest(view, url);
		}
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return super.shouldOverrideUrlLoading(view, url);
	}

}
