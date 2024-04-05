package com.gopalapriyadasa.bhaktivedanta_textabase;


import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.util.Log;

public class MainTabListener implements TabListener {

	public MainActivity main = null;
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		Log.i("tablist", "reselected " + tab.getText());
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (main != null) {
			main.SetCurrentTab(Integer.parseInt(tab.getTag().toString()));
		}
		Log.i("tablist", "selected " + tab.getText());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Log.i("tablist", "unselected " + tab.getText());
	}

}
