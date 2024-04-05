package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.NSRange;
import com.gopalapriyadasa.textabase_engine.VBContentRow;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class SearchPageAsyncTask extends AsyncTask<String, Integer, String> {

	private SearchPageGroup userInterfacePage;
	private int scopeIndex;
	
	public SearchPageGroup getUserInterfacePage() {
		return userInterfacePage;
	}

	public void setUserInterfacePage(SearchPageGroup uiPage) {
		this.userInterfacePage = uiPage;
	}

	@Override
	protected String doInBackground(String... arg0) {

        NSRange recordFilter = new NSRange();

		if (arg0.length < 1)
			return "";
		Folio currentFolio = FolioLibraryService.getCurrentFolio();
        if (currentFolio == null)
            return "";

        if (scopeIndex == 0) {
            recordFilter.setLocation(0);
            recordFilter.setLocationEnd(currentFolio.getRecordCount());
        } else {
            int currentTextIndex = userInterfacePage.mainActivity.textPageGroup.getCurrentRecordId();
            List<VBContentRow> rows = currentFolio.findContentItemsContainingRecord(currentTextIndex);
            VBContentRow row = findRowWithType(rows, mapUserInterfaceIndexToDatabaseIndex(scopeIndex));
            int si = scopeIndex;
            while (row == null) {
                if (si <= 1) {
                    // we cannot go lower, so whoel database is in scope
                    row = new VBContentRow();
                    row.setRecord(0);
                    row.setNextSibling(currentFolio.getRecordCount());
                } else {
                    si--;
                    row = findRowWithType(rows, mapUserInterfaceIndexToDatabaseIndex(si));
                }
            }

            recordFilter.setLocation(row.getRecord());
            recordFilter.setLocationEnd(row.getNextSibling() - 1);
        }

        SearchPageGroup.searchResults.flatTextAlternative = "";

		Log.i("search", "Asynchronous search started");
		currentFolio.search(arg0[0], SearchPageGroup.searchResults,
                userInterfacePage.mainActivity.textPageGroup.getPhrases(), recordFilter);
		Log.i("search", "Asynchronous search finished.");

        if (SearchPageGroup.searchResults.results.size() == 0) {
            SearchPageGroup.searchResults.flatTextAlternative += currentFolio.explainSearch(arg0[0]);
        }
		return "";
	}

    //
    // For user Interface we have these values:
    //  0 - Whole database, 1- Book, 2 - Article
    // Fore database we have these values:
    //  0 - unspecified, 1 - article, 2 - book
    // so we have to map
    private int mapUserInterfaceIndexToDatabaseIndex(int i) {
        if (i == 1) return 2;
        if (i == 2) return 1;
        return i;
    }

    private VBContentRow findRowWithType(List<VBContentRow> rows, int si) {
        for(VBContentRow row : rows) {
            if (row.getNodeType() == si) {
                return row;
            }
        }

        return null;
    }

    @Override
	protected void onPostExecute(String result) {
		Log.i("search", "User interface notified about the end of async search.");
		userInterfacePage.querySearchDidFinish();
		super.onPostExecute(result);
	}


	public int getScopeIndex() {
		return scopeIndex;
	}

	public void setScopeIndex(int scopeIndex) {
		this.scopeIndex = scopeIndex;
	}
}
