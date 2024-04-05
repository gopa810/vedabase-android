package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.VBContentRow;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

public class CIItem extends LinearLayout {

	public ImageView icon;
	public TextView mainTitle;
	private int selectedStatus = VBContentRow.SELECTION_NO;
	
	private VBContentRow item = null;
	private ContentListAdapter adapter = null;
	
	public CIItem(Context context) {
		super(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
		li.inflate(R.layout.ci_item, this, true);

		icon = (ImageView)findViewById(R.id.imageView);

		mainTitle = (TextView)findViewById(R.id.statusTitle);

		this.setOnClickListener(textClicker);

		setSelectedStatus(VBContentRow.SELECTION_NO);

	}

	public void setIconRecourceId(int id) {
		icon.setImageResource(id);
	}
	
	public CIItem(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	// setter for adapter
	//
	public void setAdapter(ContentListAdapter adapter) {
		this.adapter = adapter;
	}
	
	// getter for adapter
	//
	public ContentListAdapter getAdapter() {
		return adapter;
	}
	
	
	final OnClickListener textClicker = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {

			if (arg0 instanceof CIItem) {

				CIItem view = (CIItem)arg0;
				VBContentRow row = view.getItem();
				String action = row.getAction();
				adapter.executeAction(action);
			}

		}
	};


	public void setItem(VBContentRow _item) {
		item = _item;

		// text fields
		mainTitle.setText(item.getMainTitle().trim());

		int nodeType = _item.getNodeType();
		int itemType = _item.getType();
		if (itemType == VBContentRow.TYPE_BACK) {
			icon.setImageResource(R.drawable.content_up);
		} else if (itemType == VBContentRow.TYPE_NOTE) {
			if (_item.getRecord() < 0) {
				icon.setImageResource(R.drawable.content_icon_dir);
			} else {
				icon.setImageResource(R.drawable.content_notes);
			}
		} else if (itemType == VBContentRow.TYPE_BOOKMARK) {
			if (_item.getRecord() < 0) {
				icon.setImageResource(R.drawable.content_icon_dir);
			} else {
				icon.setImageResource(R.drawable.content_bkmk);
			}
		} else if (itemType == VBContentRow.TYPE_HIGHLIGHTER) {
			if (_item.getRecord() < 0) {
				icon.setImageResource(R.drawable.content_icon_dir);
			} else {
				icon.setImageResource(R.drawable.content_hightext);
			}
		} else if (itemType == VBContentRow.TYPE_ITEM) {
			if (nodeType == 1) {
				icon.setImageResource(R.drawable.content_icon_text);
			} else if (nodeType == 2) {
				icon.setImageResource(R.drawable.content_icon_book);
			} else {
				icon.setImageResource(R.drawable.content_icon_dir);
			}
		}
	}
	
	public VBContentRow getItem()
	{
		return item;
	}

	public int getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(int selectedStatus) {
		this.selectedStatus = selectedStatus;
	}
}
