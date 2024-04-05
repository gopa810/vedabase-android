package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.bhaktivedanta_textabase.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ContentPageFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment, container, false);
		
	}
}
