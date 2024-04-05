package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class AudioContentProvider extends ContentProvider {

	public static final Uri CONTENT_URI = Uri.parse("content://com.gpsl.vebadaseaudioprovider/file");
	public static final int SINGLE_FILE = 1;
	
	private static final UriMatcher uriMatcher;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.gpsl.vedabaseaudioprovider", "file/*", SINGLE_FILE);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		int type = uriMatcher.match(arg0);
		if (type == SINGLE_FILE) {
			return "audio/mp3";
		}
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			
		}
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}
}
