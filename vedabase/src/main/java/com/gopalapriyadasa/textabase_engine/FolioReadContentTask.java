package com.gopalapriyadasa.textabase_engine;

import android.os.AsyncTask;
import android.util.Log;

public class FolioReadContentTask extends AsyncTask<Integer, Integer, Integer> {

	public Folio folio;
	
	@Override
	protected Integer doInBackground(Integer... arg0) {

		Log.i("background", "start reading content...");
		folio.initContentObject();
		Log.i("background", "finished reading content...");
		
		return 0;
	}

}
