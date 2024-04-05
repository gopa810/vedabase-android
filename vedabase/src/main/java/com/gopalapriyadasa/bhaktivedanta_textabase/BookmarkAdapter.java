package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.util.List;

import com.gopalapriyadasa.textabase_engine.VBBookmark;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class BookmarkAdapter extends ArrayAdapter<VBBookmark> {

	private Context context;
	public int currentPosition = -1;
	
	public BookmarkAdapter(Context context, int resource,
			List<VBBookmark> objects) {
		super(context, resource, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.i("view", "bookmarkAdapter.getView");
		BookmarkItemView view = null;
		
		if (convertView == null) {
			view = new BookmarkItemView(context);
		} else {
			view = (BookmarkItemView)convertView;
		}
		
		VBBookmark bkmk = getItem(position);
		
		view.setString(bkmk.getName());
		view.setHighlighted(currentPosition == position);
		
		return view;
	}

	
}
