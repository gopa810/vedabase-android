package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class TextWebView extends WebView {

	public TextWebView(Context context) {
		super(context);
	}
	
	

	public TextWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}



	public TextWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}



	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}

	
}
