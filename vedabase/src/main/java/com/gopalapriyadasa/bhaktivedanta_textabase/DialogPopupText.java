package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class DialogPopupText extends DialogFragment {

	private WebViewClient webViewClient = null;
	private WebView webView = null;
	private TextView textView1 = null;
	private TextView textView2 = null;
	private int record = -1;
	private String url = null;
	private MainActivity mainActivity = null;

	@Override
	public void onAttach(Activity activity) {
		try {
			mainActivity = (MainActivity) activity;
		} catch (ClassCastException e) {
			mainActivity = null;
		}
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View myView = inflater.inflate(R.layout.dialog_popup_text, null);
		webView = (WebView) myView.findViewById(R.id.webViewPopup);
		textView1 = (TextView)myView.findViewById(R.id.statusTitle);
		textView2 = (TextView)myView.findViewById(R.id.textView2);
		
		if (webViewClient != null) {
			Log.i("web", "setting webclient to webview in dialog");
			webView.setWebViewClient(webViewClient);
		}
		Log.i("webView", "check " + (webView == null ? "null" : "webView OK"));
		builder.setView(myView);
//		builder.setTitle("");
		builder.setPositiveButton(R.string.popup_close,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
					}
				});

		if (record > 0) {
			builder.setNegativeButton(R.string.popup_edit,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mainActivity != null) {
								mainActivity.showNoteEditor(record);
							}
						}
					});
		}
		// post loading
		if (webView != null && url != null) {
			loadUrl(url);
			url = null;
		}

		return builder.create();
	}

	public void setHtmlText(String htmlText) {
		if (webView != null) {
			webView.loadData(htmlText, "text/html", "UTF-8");
		}
	}

	public void setWebClient(WebViewClient client) {
		if (webView != null) {
			webView.setWebViewClient(client);
		} else {
			Log.i("web", "storing webclient in webview in dialog");
			webViewClient = client;
		}

	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public void setLoadingVisibility(boolean bvisible) {
		textView1.setVisibility(bvisible ? View.VISIBLE : View.GONE);
		textView2.setVisibility(bvisible ? View.VISIBLE : View.GONE);
	}
	
	public void loadUrl(String url) {
		if (webView != null) {
			Log.i("web", "url loading started");
			webView.loadUrl(url);
		} else {
			this.url = url;
			Log.i("web", "url loading not initialized, due to null webView");
		}
	}
}
