package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookmarkItemView extends LinearLayout {

	TextView textView = null;
	
	
	public BookmarkItemView(Context context) {
		super(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
		li.inflate(R.layout.bookmark_list_item, this, true);
		
		setTextView((TextView)findViewById(R.id.statusTitle));
		
	}


	public TextView getTextView() {
		return textView;
	}


	public void setTextView(TextView textView) {
		this.textView = textView;
	}
	
	public void setString(String str) {
		textView.setText(str);
	}

	public void setHighlighted(boolean state) {
		if (state) {
			setBackgroundResource(R.color.list_item_selected);
		} else {
			setBackgroundResource(0);
		}
	}
}
