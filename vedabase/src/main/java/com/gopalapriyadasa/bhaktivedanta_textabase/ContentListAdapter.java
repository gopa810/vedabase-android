package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.io.InvalidClassException;
import java.security.InvalidKeyException;
import java.util.List;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.VBContentRow;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ContentListAdapter extends ArrayAdapter<VBContentRow> {

	int resource;

	public ContentListAdapter(Context context, int _resource, List<VBContentRow> array) {
		super(context, _resource, array);
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		VBContentRow item = getItem(position);
		int type = item.getType();

		if (type == VBContentRow.TYPE_TITLE) {
			CITitle newView = null;
			if (convertView != null && convertView instanceof CITitle) {
				newView = (CITitle) convertView;
			} else {
				newView = new CITitle(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setMainTitle(item.getMainTitle());

			return newView;

		} else if (type == VBContentRow.TYPE_BACK) {
			CIItem newView = null;
			if (convertView != null && convertView instanceof CIItem) {
				newView = (CIItem)convertView;
			} else {
				newView = new CIItem(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setAdapter(this);
			newView.setItem(item);

			return newView;
		} else if (type == VBContentRow.TYPE_ITEM) {
			CIItem newView = null;
			if (convertView != null && convertView instanceof CIItem) {
				newView = (CIItem)convertView;
			} else {
				newView = new CIItem(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setAdapter(this);
			newView.setItem(item);
			if (item.getIconResourceId() > 0) {
				newView.setIconRecourceId(item.getIconResourceId());
			}
			return newView;
		} else if (type == VBContentRow.TYPE_QUICKLINKS) {
			if (item.getMainTitle().startsWith("quicktop:")) {
				CIQuickLinks newView = null;
				if (convertView != null && convertView instanceof CIQuickLinks) {
					newView = (CIQuickLinks) convertView;
				} else {
					newView = new CIQuickLinks(getContext());
				}
				newView.setActionsForPage(MainActivity.getInstance().contentPageGroup);
				newView.showAllExcept(Integer.parseInt(item.getMainTitle().substring(9)));

				return newView;
			} else {
			}
			return null;
		} else if (type == VBContentRow.TYPE_BOOKMARK) {
			CIItem newView = null;
			if (convertView != null && convertView instanceof CIItem) {
				newView = (CIItem)convertView;
			} else {
				newView = new CIItem(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setAdapter(this);
			newView.setItem(item);
			if (item.getIconResourceId() > 0) {
				newView.setIconRecourceId(item.getIconResourceId());
			}
			return newView;
		} else if (type == VBContentRow.TYPE_NOTE) {
			CIItem newView = null;
			if (convertView != null && convertView instanceof CIItem) {
				newView = (CIItem)convertView;
			} else {
				newView = new CIItem(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setAdapter(this);
			newView.setItem(item);
			if (item.getIconResourceId() > 0) {
				newView.setIconRecourceId(item.getIconResourceId());
			}
			return newView;

		} else if (type == VBContentRow.TYPE_HIGHLIGHTER) {
			CIItem newView = null;
			if (convertView != null && convertView instanceof CIItem) {
				newView = (CIItem) convertView;
			} else {
				newView = new CIItem(getContext());
				//newView.mainTitle.setTypeface(MainActivity.generalFont);
			}
			newView.setAdapter(this);
			newView.setItem(item);
			if (item.getIconResourceId() > 0) {
				newView.setIconRecourceId(item.getIconResourceId());
			}
			return newView;

		} else if (type == VBContentRow.TYPE_HR) {
			CIHorizontalLine newView = null;
			if (convertView != null && convertView instanceof CIHorizontalLine) {
				newView = (CIHorizontalLine) convertView;
			} else {
				newView = new CIHorizontalLine(getContext());
			}
			return newView;
		} else if (type == VBContentRow.TYPE_SPACE) {
			CISpace newView = null;
			if (convertView != null && convertView instanceof CISpace) {
				newView = (CISpace) convertView;
			} else {
				newView = new CISpace(getContext());
			}
			return newView;
		} else if (type == VBContentRow.TYPE_TEXT) {
			CIText text = null;
			if (convertView != null && convertView instanceof CIText) {
				text = (CIText)convertView;
			} else {
				text = new CIText(getContext());
			}
			text.setText(item.getMainTitle());
			return text;
		} else if (type == VBContentRow.TYPE_ACTION) {
			CILinkAction link = null;
			if (convertView != null && convertView instanceof CILinkAction) {
				link = (CILinkAction)convertView;
			} else {
				link = new CILinkAction(getContext());
			}
			link.setAdapter(this);
			link.setText(item.getMainTitle());
			link.setActionText(item.getAction());
			return link;
		} else {
			CISpace newView = null;
			if (convertView != null && convertView instanceof CISpace) {
				newView = (CISpace)convertView;
			} else {
				newView = new CISpace(getContext());
			}

			return newView;
		}
	}

	@Override
	public int getItemViewType(int position) {
		VBContentRow item = getItem(position);
		if (item != null) {
			return item.getType();
		} else {
			return 0;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 15;
	}

	public void executeAction(String str) {
		
		if (MainActivity.getInstance() != null) {
			if (str != null && str.length() > 0) {
				MainActivity.getInstance().contentPageGroup.executeCommand(str);
				return;
			}
		}
		Log.i("files", "item selected");
	}
	


	
}
