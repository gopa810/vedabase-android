package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class FlatFileSource implements FlatFileSourceInterface {

	public static final int BULK_SIZE = 16;
	private static final int MAX_BULK_AGE = 16;
	
	public class RecordBulk {
		public int age = 0;
		
		public boolean loaded = false;
		public int baseId = 0;
		public int count = 0;
		public int bulkPage = 0;
		public FDRecordBase [] records = new FDRecordBase[BULK_SIZE];
	}
	
	public class RecordBulkLoader extends AsyncTask<Integer, Void, FlatFileSource>
	{
		public RecordBulk bulk = null;
		public int page = 0;
		public FlatFileSource source = null;
		public int direction = 1;
		public FlatFileDestination destination = null;
		
		@Override
		protected FlatFileSource doInBackground(Integer... pages) {

			Folio folio = FolioLibraryService.getCurrentFolio();
			if (folio == null)
				return source;
			if (!source.isPageLoaded(page)) {
				int baseId = page * BULK_SIZE;
				List<FolioTextRecord> recs = folio.findTextRecords(baseId, BULK_SIZE - 1);
				
				Log.i("loadt", "async loading bulk page: " + page);
				bulk = new RecordBulk();
				bulk.age = 0;
				bulk.baseId = baseId;
				bulk.count = recs.size();
				bulk.bulkPage = page;
	
				int i = 0;
				if (recs.size() > 0) {
					i = recs.get(0).getRecord() - baseId;
					if (i < 0) {
						i = 0;
					}
				}
				for(FolioTextRecord rec : recs) {
					if (i >= BULK_SIZE)
						break;
					bulk.records[i] = source.convert(rec);
					bulk.records[i].requestedAlign = direction;
					i++;
				}
			}
			return source;
		}
		
		protected void onPostExecute(FlatFileSource source) {

			if (bulk != null) {
				Log.i("loadt", "async bulk page loading finished: " + bulk.bulkPage);
				source.setRecordPage(bulk);
				if (destination != null) {
					destination.RecordPageLoaded(bulk.bulkPage, direction);
				}
			}
			source.invokeLoadingPages(direction, destination);
		}
	}
	
//	protected Lock bulksLock = new Lock();
	protected ArrayList<RecordBulk> bulks = new ArrayList<RecordBulk>();
	private ArrayList<Integer> bulkPagesToLoad = new ArrayList<Integer>();
	private ArrayList<Integer> bulkPagesLoading = new ArrayList<Integer>();
	
	public void addPageToLoad(int i) {
		if (i >= 0) {
			for(Integer a : bulkPagesToLoad) {
				if (a == i)
					return;
			}
			for(Integer a : bulkPagesLoading) {
				if (a == i)
					return;
			}
			Log.i("loadt", "ADD bulk page: " + i);
			bulkPagesToLoad.add(i);
		}
	}
	
	public int getPageToLoad() {
		int i = -1;
		
		if (bulkPagesToLoad.size() > 0) {
			i = bulkPagesToLoad.get(0);
			bulkPagesToLoad.remove(0);
			bulkPagesLoading.add(i);
			Log.i("loadt", "REM bulk page is going to be loaded: " + i);
		}		
		
		return i;
	}
	
	public FlatFileSource() {
	}

	@Override
	public int getRecordCount() {

		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			return folio.getRecordCount();
		}
		return 0;
	}

	public RecordBulk getRecordPage(int page) {

		synchronized(bulks) {
			for (RecordBulk bulk : bulks) {
				if (bulk.bulkPage == page) {
					return bulk;
				}
			}
		}

		return null;		
	}

	public RecordBulk getSafeRecordPage(int page) {
		RecordBulk bulk;
		
		bulk = getRecordPage(page);
		if (bulk == null) {
			
			bulk = new RecordBulk();
			bulk.age = 0;
			bulk.baseId = page*BULK_SIZE;
			bulk.count = 0;
			bulk.bulkPage = page;
			synchronized(bulks) {
				bulks.add(bulk);
			}
		}
		
		return bulk;
	}

	@Override
	public FDRecordBase getRecord(int recordId, FlatFileDestination dest, int direction) {
		
		boolean hasLower = false;
		boolean hasHigher = false;
		int page = recordId / BULK_SIZE;
		FDRecordBase retVal = null;
		StringBuilder sb = new StringBuilder();

		synchronized(bulks) {
			for (RecordBulk bulk : bulks) {
				sb.append("bulk(" + bulk.bulkPage + "), ");
				if (bulk.bulkPage == page) {
					bulk.age = 0;
					try {
						retVal = bulk.records[recordId - bulk.baseId];
					} catch(Exception x) {
						Log.e("Bulks", "Requested bulk index error: recId=" + recordId + ", bulkPage=" + page + ", bulkBase=" + bulk.baseId);
					}
				} else if (bulk.bulkPage == page - 1) {
					hasLower = true;
					bulk.age = 0;
				} else if (bulk.bulkPage == page + 1) {
					hasHigher = true;
					bulk.age = 0;
				}
			}
		}

		if (retVal != null) {
			if (!hasHigher) {
				addPageToLoad(page + 1);
				addPageToLoad(page + 2);
			}
			if (!hasLower) {
				addPageToLoad(page - 1);
				addPageToLoad(page - 2);
			}
			if (!hasHigher || !hasLower) {
				invokeLoadingPages(direction, dest);
			}
			return retVal;
		}

		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			RecordBulk bulk = loadFakeRecordsPage(page, dest, direction, hasHigher, hasLower);
			if (recordId >= bulk.baseId)
				return bulk.records[recordId - bulk.baseId];
		}
		
		return null;
	}

	public RecordBulk loadFakeRecordsPage(int page, FlatFileDestination dest, int direction, boolean hasHigherPage, boolean hasLowerPage) {
		

		RecordBulk bulk = getRecordPage(page);
		if (bulk != null)
			return bulk;
		
		int baseId = page * BULK_SIZE;
		
		bulk = new RecordBulk();
		bulk.loaded = false;
		bulk.age = 0;
		bulk.baseId = baseId;
		bulk.count = BULK_SIZE;
		bulk.bulkPage = page;
		for(int i = 0; i < BULK_SIZE; i++) {
			bulk.records[i] = new FDRecordBase();
			bulk.records[i].requestedAlign = direction;
			bulk.records[i].recordId = baseId + i;
		}
		removeOldBulks();
		synchronized(bulk) {
			bulks.add(bulk);
		}

		// load records in background
		addPageToLoad(page);
		if (!hasHigherPage) {
			addPageToLoad(page + 1);
			addPageToLoad(page + 2);
		}
		if (!hasLowerPage) {
			addPageToLoad(page - 1);
			addPageToLoad(page - 2);
		}
		invokeLoadingPages(direction, dest);

		return bulk;
	}
	
	public boolean isPageLoaded(int page)
	{
		synchronized(bulks) {
			for(RecordBulk bulk : bulks) {
				if (bulk.bulkPage == page && bulk.loaded == true)
					return true;
			}
		}
		
		return false;
	}

	public void invokeLoadingPages(int direction, FlatFileDestination dest)
	{
		int nextPage = getPageToLoad();
		
		if (nextPage != -1) {
			// load records in background
			RecordBulkLoader loader = new RecordBulkLoader();
			loader.page = nextPage;
			loader.source = this;
			loader.direction = direction;
			loader.destination = dest;
			loader.execute(0);			
		}
	}
	
	public void setRecordPage(RecordBulk bulk) {
		
		RecordBulk found = getSafeRecordPage(bulk.bulkPage);
		
		found.loaded = true;
		found.baseId = bulk.baseId;
		found.count = bulk.count;
		found.records = bulk.records;
		
		Integer i = bulk.bulkPage;
		
		bulkPagesLoading.remove(i);
	}
	
	/*
	 * removing bulks which were used a long time ago
	 */
	public void removeOldBulks() {
		ArrayList<RecordBulk> toremove = new ArrayList<FlatFileSource.RecordBulk>();
		
		synchronized(bulks) {
			for(RecordBulk bulka : bulks) {
				bulka.age++;
				if (bulka.age > MAX_BULK_AGE) {
					toremove.add(bulka);
				}
			}
			bulks.removeAll(toremove);
		}
	}
	
	public void setFolio(Folio folio) {

		synchronized(bulks) {
			this.bulks.clear();
		}
	}
	
	public FDRecordBase convert(FolioTextRecord recDict) {

		FDRecordBase recordBase = null;
		FlatParagraph fp = new FlatParagraph(FolioLibraryService.getCurrentFolio());

		recordBase = fp.convertToRaw(recDict);
		return recordBase;
		
	}
	
	@Override
	public String getRecordPath(int record) {
		String title = "";
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio == null)
			return title;

		VBContentNode item = folio.findRecordPath(record);
		if (item != null) {
			title = folio.getDocumentPath(item);
		}
		return title;
	}

	@Override
	public VBCustomNotes recordNotesForRecord(int rec) {
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio == null)
			return null;
		return folio.recordNotesForRecord(rec);
	}

	@Override
	public boolean canHaveNotes() {
		return true;
	}

	@Override
	public VBCustomNotes safeRecordNotesForRecord(int rec) {
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio == null)
			return new VBCustomNotes();
		return folio.safeRecordNotesForRecord(rec);
	}

	@Override
	public VBCustomHighlights highlightersForRecord(int rec) {
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio == null)
			return null;
		return folio.highlightersForRecord(rec);
	}

	@Override
	public VBCustomHighlights safeHighlightersForRecord(int rec) {
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio == null)
			return new VBCustomHighlights();
		return folio.safeHighlightersForRecord(rec);
	}
}
