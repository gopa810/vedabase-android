package com.gopalapriyadasa.textabase_engine;

import android.app.DownloadManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.gopalapriyadasa.bhaktivedanta_textabase.DatabasePageGroup;
import com.gopalapriyadasa.bhaktivedanta_textabase.MainActivity;
import com.gopalapriyadasa.bhaktivedanta_textabase.MessageBox;
import com.gopalapriyadasa.bhaktivedanta_textabase.MessageBoxDelegate;
import com.gopalapriyadasa.bhaktivedanta_textabase.R;
import com.gopalapriyadasa.bhaktivedanta_textabase.VBDownloader;
import com.gopalapriyadasa.bhaktivedanta_textabase.VBDownloaderDelegate;

public class FolioLibraryService implements VBDownloaderDelegate, MessageBoxDelegate, FolioDelegate {

	private String folioDirectory;
	private Folio currentFolio = null;
    private Context runContext = null;
	private Handler handler = null;
    private MainActivity mainActivity;
    private Date lastRelCheckTime = null;
    private String newVersionTitle = "";
    //private String tempraryBinaryFile = "";
    //private String targetBinaryFile = "";
    private int customReferencesBeforeWrite = -1;

    private long initialFreeSpace = 0L;
    private long downloadedSize = 0L;

    public static final String PREFERENCES_FILE = "com.gpsl.folioLibraryService";
    public static final String DATABASE_FILE = "vedabase.ivd";

    // status of update manager
    public static final int UPDATE_MANAGER_IDLE = 10;
    public static final int UPDATE_MANAGER_CHECKING = 20;
    public static final int UPDATE_MANAGER_DOWNLOADING = 30;
    public static final int UPDATE_MANAGER_UPDATING = 40;
    private int updateManagerStatus = UPDATE_MANAGER_IDLE;

    public String APPDIR_OLD = null;
    public String APPDIR_NEW = "com.vedabase";
    public String DatabaseFilePath = "";

    private static FolioLibraryService instance = new FolioLibraryService();

	public void onCreate(MainActivity main) {
        mainActivity = main;

        acceptContext(main);

        APPDIR_OLD = mainActivity.getPackageName();
        APPDIR_NEW = "com.vedabase";

        SharedPreferences sp = mainActivity.getSharedPreferences(PREFERENCES_FILE, 0);

        updateManagerStatus = sp.getInt("ProcessStatus", UPDATE_MANAGER_IDLE);
        DatabaseFilePath = sp.getString("DatabaseFilePath", "");

        File curentFile = new File(DatabaseFilePath);
        if (!curentFile.exists()) {
            File file1 = findDatabaseOnOldPath();
            if (file1 != null) {
                DatabaseFilePath = file1.getAbsolutePath();
                saveDatabaseFilePath(DatabaseFilePath);
            }
        }
    }


    public String loadDatabaseFilePath()
    {
        MainActivity ma = MainActivity.getInstance();
        if (ma == null)
            return "";
        SharedPreferences sp = ma.getSharedPreferences(PREFERENCES_FILE, 0);
        return sp.getString("DatabaseFilePath", "");
    }

    public void saveDatabaseFilePath(String value)
    {
        DatabaseFilePath = value;
        MainActivity ma = MainActivity.getInstance();
        if (ma != null) {
            SharedPreferences sp = ma.getSharedPreferences(PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("DatabaseFilePath", value);
            editor.commit();
        }
    }

    public void acceptContext(Context ctx) {

        if (handler == null) {
            handler = new Handler(ctx.getMainLooper());
        }

        if (runContext == null) {
            runContext = ctx;
        }
    }


    public File findDatabaseOnOldPath() {
        String[] paths = new String[] {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                        + File.separatorChar + DATABASE_FILE,
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separatorChar + "Android"
                        + File.separatorChar + "data"
                        + File.separatorChar + APPDIR_OLD
                        + File.separatorChar + DATABASE_FILE,
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separatorChar + "Android"
                        + File.separatorChar + "data"
                        + File.separatorChar + APPDIR_NEW
                        + File.separatorChar + DATABASE_FILE
        };

        for (String path: paths) {
            File file = new File(path);
            if (file.exists())
                return file;
        }

        return null;
    }



    public static void onDestroy() {
        FolioLibraryService service = getInstance();
        service.Close();
        service.mainActivity = null;
		service.handler = null;
	}

	public boolean onResume() {
        /*try {
             mainActivity.registerReceiver(vbDownloader, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception ex) {

        }*/

        return ReloadLibraryIntoViewer(false);
	}

	public void onPause() {
        try {
            //mainActivity.unregisterReceiver(vbDownloader);
        } catch (Exception x) {

        } finally {
            Close();
        }
    }

	public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public long externalStorageFree() {
        File file = Environment.getExternalStorageDirectory().getAbsoluteFile();
        long freeMbytes = file.getFreeSpace()*10000;
        return freeMbytes;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)runContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public String isSpaceAvailable() {

        long freeSpace = externalStorageFree() / 1048576;
        Log.i("memory", "External storage has " + freeSpace + " MBytes");
        if (databaseFileExists()) {
            if (freeSpace < 100) {
                return "Not enough space for downloading database. Free at least 100 MBytes of space on your external storage.";
            }
        } else {
            if (freeSpace < 1200) {
                return "Not enough space for downloading database. Free at least 1.2 GBytes of space on your external storage.";
            }
        }

        return null;
    }
    //
    // Checking the status of local database against server version
    //
    public void downloadDatabaseFile() {

        Date nowTime = new Date();
        String message = null;

        // if last check of relevance was done within last 60 minutes
        // then do not check now
        if (lastRelCheckTime != null && lastRelCheckTime.getTime() < (nowTime.getTime() - 3600000)) {
            Log.i("UpdateManager", "Refusing update due to last update being less than hour before.");
            return;
        }

        /*if (!isNetworkAvailable()) {
            Log.i("UpdateManager", "Refusing update due to unavailable network");
            updateManagerStatus = UPDATE_MANAGER_IDLE;
            getDatabasePage().setErrorStatus("Network connection is not available.");
            return;
        }

        if ((message = isSpaceAvailable()) != null) {
            updateManagerStatus = UPDATE_MANAGER_IDLE;
            getDatabasePage().setErrorStatus(message);
            return;
        }*/

        Log.i("UpdateManager", "Request for vedabase.ivd posted.");
        //https://www.dropbox.com/s/v3ol786ejzstdd2/vedabase.ivd?dl=0

        // test file 1kB
        //VBDownloader.downloadFile("https://dl.dropbox.com/s/v3ol786ejzstdd2/vedabase.ivd?dl=1", this, "database");

        // working file 1GB
        VBDownloader.downloadFile("https://dl.dropbox.com/s/57ydtd8a90z1m1w/vedabase.ivd?dl=1", this, "database");

        lastRelCheckTime = nowTime;

        //ShowDownloadsListActivity();

    }

    public static void ShowDownloadsListActivity() {
        MainActivity ma = MainActivity.getInstance();
        if (ma != null) {
            Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ma.startActivity(dm);
        }
    }

    //
    // send request for versiosn file on the server
    // response will be asynchronous by calling of the function onFileDownloaded
    //
    public void downloadVersionsFile() {

        if (getUpdateManagerStatus() == FolioLibraryService.UPDATE_MANAGER_IDLE) {
            setUpdateManagerStatus(FolioLibraryService.UPDATE_MANAGER_CHECKING, null);

            Log.i("UpdateManager", "Request for versions.txt posted.");
            VBDownloader.downloadFile("https://s3.amazonaws.com/vedabase-down-uss/versions.txt", this, "versions");
        }
    }

    public String getSafeValue(HashMap<String,String> args, String key, String defaultValue) {
        if (args.containsKey(key)) {
            return args.get(key);
        }
        return defaultValue;
    }

    public static SharedPreferences getSharedPreferences() {
        return MainActivity.getInstance().getSharedPreferences(PREFERENCES_FILE, 0);
    }

    //
    // this starts whole download process and updating of folio database
    // Steps are as follows:
    //    before:
    //        set download status to DOWNLOADING (PERM)
    //        remove old database
    //        set next downloaded file (PERM)
    //        set last downloaded file (PERM)
    //    repeat for all files:
    //        check if next != last, if yes then start download file
    //              if next == last, then goto step after all downloads
    //    after downloading of particular file:
    //        append to target database file (temporary) unzipped
    //        set next downloaded file (PERM)
    //    after all downloaded:
    //        set download status to UPDATING (PERM)
    //        clear variables in perm
    //        rename temporary DB to main DB
    //        set download status to IDLE (PERM)
    //        refresh file scan
/*    public void startDatabaseDownloading(HashMap<String,String> args) {

        this.targetBinaryFile = getMainDataFilePath(APPDIR_NEW);
        this.tempraryBinaryFile = getFolioDirectory(APPDIR_NEW) + File.separatorChar + "temp.bin";

        if (runContext != null) {
            // initialization of the whole download and update process
            SharedPreferences sp = runContext.getSharedPreferences(PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = sp.edit();
            setUpdateManagerStatus(FolioLibraryService.UPDATE_MANAGER_DOWNLOADING, editor);
            editor.putInt("LastFileIndex", Integer.parseInt(getSafeValue(args, "FILES", "0")));
            editor.putLong("PartSize", Long.parseLong(getSafeValue(args, "PARTSIZE", "10")) * 1048576L);
            editor.putInt("NextFileIndex", 0);
            editor.putString("BaseUrl", "https://s3.amazonaws.com/vedabase-down-uss/" + args.get("DIRECTORY"));
            editor.putString("TemporaryBinary", tempraryBinaryFile);
            editor.commit();
            targetBinaryFile = sp.getString("TargetBinary", getMainDataFilePath(APPDIR_NEW));
        }

        // removing old database file
        File file = new File(targetBinaryFile);
        file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("UpdateManager", "Could not create file " + targetBinaryFile);
        }

        // now start downloading loop
        if (mainActivity != null) {
            mainActivity.setCurrentTabCode("dbinfo");
        }

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                downloadNextPartFile();
            }
        });
    }
*/

    /*public void downloadNextPartFile() {

        SharedPreferences sp = runContext.getSharedPreferences(PREFERENCES_FILE, 0);

        int lastFileIndex = sp.getInt("LastFileIndex", 0);
        int nextFileIndex = sp.getInt("NextFileIndex", 0);
        long partSize = sp.getLong("PartSize", 10L);

        if (nextFileIndex < lastFileIndex) {
            String tag = String.format(Locale.getDefault(), "PartFile:%d", nextFileIndex);
            String baseUrl = sp.getString("BaseUrl", "");
            if (baseUrl.length() <= 0)
                return;
            downloadedSize += partSize;
            long missingSpace = externalStorageFree() - (lastFileIndex - nextFileIndex) * partSize;
            getDatabasePage().showLowSpaceMessage(missingSpace);
            if (missingSpace > 0) {
                Log.i("UpdateManager", "Looks like we have enough space for storage (" + Long.toString(-missingSpace) + " MB)");
            }
            String fileUrl = String.format(Locale.getDefault(), "%s/fpart.%d.zip", baseUrl, nextFileIndex);
            Log.i("UpdateManager", "Posting request for: " + fileUrl);
            VBDownloader.downloadFile(fileUrl, this, tag);
        } else {
            //    after all downloaded:
            //        set download status to UPDATING (PERM)
            //        clear variables in perm
            //        rename temporary DB to main DB
            //        set download status to IDLE (PERM)
            //        refresh file scan

            SharedPreferences.Editor editor = sp.edit();
            setUpdateManagerStatus(FolioLibraryService.UPDATE_MANAGER_UPDATING, null);
            editor.remove("LastFileIndex");
            editor.remove("NextFileIndex");
            editor.commit();

            String targetMainFile = sp.getString("TargetBinary", "");
            String targetTempFile = sp.getString("TemporaryBinary", "");
            Log.i("UpdateManager", "Moving source file from: " + targetTempFile);
            Log.i("UpdateManager", "Moving target file to:   " + targetMainFile);

            if (targetMainFile.length() > 0 && targetTempFile.length() > 0) {
                File tempFile = new File(targetTempFile);
                File mainFile = new File(targetMainFile);
                tempFile.renameTo(mainFile);

                setUpdateManagerStatus(FolioLibraryService.UPDATE_MANAGER_IDLE, editor);
                editor.putString("currentVersion", sp.getString("nextVersion", "0"));
                editor.commit();
            }

            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ReloadLibraryIntoViewer(true);
                }
            });
        }
    }*/

    //
    // check files
    // if database is available, then load database into viewer
    // if database is not available, then display database fragment
    //
    public boolean ReloadLibraryIntoViewer(boolean force) {
        if (force || currentFolio == null) {
            currentFolio = null;
            checkAvailableLibraries();
            MainActivity ma = MainActivity.getInstance();
            if (ma != null) {
                if (currentFolio != null) {
                    ma.SetCurrentTab(R.id.textPane);
                    ma.LoadFolioTextRecords(0, true);
                    return true;
                } else {
                    ma.SetCurrentTab(R.id.databaseFragment);
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public int onFileDownloaded(String fileTag, String filePath, boolean success, int reason) {
        /*if (fileTag.equals("versions")) {
            if (success) {
                Log.i("UpdateManager", "File versions.txt downloaded");
                onFileVersionsDownloaded(filePath);
            } else {
                Log.i("UpdateManager", "File versions.txt NOT downloaded, reason " + reason);
                setUpdateManagerStatus(UPDATE_MANAGER_IDLE, null);
            }
        } else if (fileTag.startsWith("PartFile:")) {
            if (success) {
                Log.i("UpdateManager", "File " + fileTag + " downloaded");
                onFilePartDownloaded(filePath);
            } else {
                Log.i("UpdateManager", "File " + fileTag + " failed, reason " + reason);
                // do nothing in this branch, because we will retry downloading
            }

            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    downloadNextPartFile();
                }
            });
        } else*/
        if (fileTag.equals("database")) {
            saveDatabaseFilePath(filePath);
            DatabaseFilePath = filePath;
            ReloadLibraryIntoViewer(true);
        }

        return 0;
    }

    /*private void onFileVersionsDownloaded(String filePath) {

        HashMap<String,String> args = new HashMap<String, String>();
        File file = new File(filePath);
        try {
            FileReader reader = new FileReader(file);
            BufferedReader buff = new BufferedReader(reader);
            String line;
            while((line = buff.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#")) {
                    int pos = line.indexOf("=");
                    if (pos > 0) {
                        String key = line.substring(0, pos);
                        String value = line.substring(pos + 1);
                        args.put(key, value);
                        Log.i("UpdateManager", "   get parameters: " + key + " := " + value);
                        if (key.equals("USERTITLE")) {
                            setNewVersionTitle(value);
                        }
                    }
                }
            }
            buff.close();
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();

        setUpdateManagerStatus(UPDATE_MANAGER_IDLE, null);

        if (!databaseFileExists()) {
            Log.i("UpdateManager", "New version is available. Starting download.");
            //startDatabaseDownloading(args);
            args.put("STEP", "versions");
            MessageBox.showMessage(this, "Start downloading database?", MessageBox.MB_YESNO, args);
        } else {
            // process args
            SharedPreferences prefs = runContext.getSharedPreferences(PREFERENCES_FILE, 0);
            String last = prefs.getString("currentVersion", "0");
            if (args.containsKey("VERSION")) {
                if (!last.equals(args.get("VERSION"))) {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putString("nextVersion", args.get("VERSION"));
                    ed.commit();

                    // we should ask user for the permission to update database
                    Log.i("UpdateManager", "New version is available. Asking user for permission to update");
                    args.put("STEP", "versions");
                    MessageBox.showMessage(this, "Start downloading database?", MessageBox.MB_YESNO, args);
                } else {
                    Log.i("UpdateManager", "Version from file and version from SharedPreferences are the same, equal to " + last);
                }
            }
        }

    }*/

    /*public void onFilePartDownloaded(String filePath) {
        File file = new File(filePath);

        try {
            Log.i("UpdateManager", "Part file was downloaded. Now unpack it and append.");
            SharedPreferences sp = runContext.getSharedPreferences(PREFERENCES_FILE, 0);

            int lastFileIndex = sp.getInt("LastFileIndex", 0);
            int nextFileIndex = sp.getInt("NextFileIndex", 0);
            long partSize = sp.getLong("PartSize", 10485760);
            String tempFilePath = sp.getString("TemporaryBinary", "");
            if (tempFilePath.length() == 0)
                return;

            // now unzip part file and append to main temp file
            // close main temp file
            unpackAndAppendZip(tempFilePath, filePath, nextFileIndex * partSize);

            // now delete part file
            file.delete();

            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("NextFileIndex", nextFileIndex + 1);
            editor.commit();

            getDatabasePage().setDownloadStatusProgress(nextFileIndex*100 / lastFileIndex);

        } catch (Exception ex) {
            Log.e("UpdateManager", "Exception broke onFilePartDownloaded: " + ex.getMessage());
        }
    }

    //
    // Unpack file and append to the big file
    //
    private boolean unpackAndAppendZip(String targetPath, String zipSourcePath, long position)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(zipSourcePath);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[4096];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...

                File targetFile = new File(targetPath);
                FileOutputStream fout = new FileOutputStream(targetPath, true);
                FileChannel fchan = fout.getChannel();
                fchan.truncate(position);

                Log.i("UpdateManager", "APP Starting to append from position: " + targetFile.length());
                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fchan.close();
                fout.close();
                zis.closeEntry();

                //Log.i("UpdateManager", "APP Appended " + filename);
                //Log.i("UpdateManager", "APP Current size after appending: " + targetFile.length());
            }

            zis.close();
        }
        catch(IOException e)
        {
            Log.e("UpdateManager", "Error during unpacking: " + e.getMessage());
            getDatabasePage().setDownloadError("ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }*/

    public DatabasePageGroup getDatabasePage() {
        return MainActivity.getInstance().databasePageGroup;
    }

    public void ensureDataDirectoryExists(String subdir)
	{
    	File fm;
    	folioDirectory = getFolioDirectory(subdir);
    	fm = new File(folioDirectory);
    	if (!fm.exists())
    	{
	    	folioDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "Android";
	
	    	fm = new File(folioDirectory);
	    	if (!fm.exists()) {
	    		fm.mkdir();
	    	}
	
	    	folioDirectory += File.separatorChar + "data";
	    	fm = new File(folioDirectory);
	    	if (!fm.exists()) {
	    		fm.mkdir();
	    	}
	    	
	    	folioDirectory += File.separatorChar + subdir;
	       	fm = new File(folioDirectory);
	       	if (!fm.exists()) {
	        	fm.mkdir();
	    	}
    	}
	}
	
	public String getFolioDirectory(String subdir)
	{
		if (folioDirectory == null || folioDirectory.length() == 0) {
			folioDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separatorChar + "Android"
                    + File.separatorChar + "data"
                    + File.separatorChar + subdir;
		}

		Log.i("path", folioDirectory);
		return folioDirectory;
	}

    public String getMainDataFilePath(String subdir) {

		return getFolioDirectory(subdir) + File.separatorChar + DATABASE_FILE;
	}
	
	public Boolean databaseFileExists()
	{
        File mainFile = new File(DatabaseFilePath);
        return mainFile.exists();
	}

	/*
	 * Scanning of external storage for databases
	 */
	public boolean checkAvailableLibraries() {

        MainActivity ma = MainActivity.getInstance();
        if (ma == null) {
            Log.e("apk", "MainActivity is null");
            return false;
        }

        DatabaseFilePath = loadDatabaseFilePath();
        File mainFile = new File(DatabaseFilePath);
        if (!mainFile.exists()) {
            Log.e("apk", "File " + mainFile.getAbsolutePath() + " not found.");
            return false;
        }

        if (mainSource == null) {
            mainSource = new FlatFileSource();
        }

        try
        {
            Log.i("apk", "opening database from file " + DatabaseFilePath);
            SQLiteDatabase folio = ma.openOrCreateDatabase(DatabaseFilePath, Context.MODE_PRIVATE, null);
            Folio newFolio = new Folio(folio);
            newFolio.setDatabasePath(mainFile.getParent());
            newFolio.loadCustomReferences();
            newFolio.loadQueries();
            newFolio.validateUniqueId();
            newFolio.registerDelegate(this);

            // setting the text dimmensions
            newFolio.setBodyFontFamily(FDTypeface.getDefaultFontName());
            newFolio.setBodyFontSize((int) FDCharFormat.getMultiplyFontSize() * 14);
            newFolio.setBodyLineSpacing((int) FDCharFormat.getMultiplySpaces() * 100);

            currentFolio = newFolio;
            mainSource.setFolio(currentFolio);
            ma.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.getInstance().SetFolio(currentFolio);
                }
            });

            return true;
        }
        catch(Exception e)
        {
            Log.i("apk", "Opening folio failed with message: " + e.getMessage());
        }

        return false;
	}
	
	/*public static Folio OpenFolio(String filePath) {
		return null;
	    	// initialization of all helper variables
	    	//FolioLibraryService.folioDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "Android"
			//		+ File.separatorChar + "obb" + File.separatorChar  + ctx.getPackageName();
	    	//FolioLibraryService.freeBytesInternal = new File(ctx.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
	    	FolioLibraryService.freeBytesExternal = new File(ctx.getExternalFilesDir(null).toString()).getFreeSpace();
	    	
	    	String rawFilePath = ctx.getExternalFilesDir(null).toString();
	    	Log.i("inita", rawFile.getAbsolutePath());
	    	
	    	Log.i("init", "Storage state: " + Environment.getExternalStorageState());
			Log.i("init", "Storage path: " + Environment.getExternalStorageDirectory().getAbsolutePath());
			Log.i("init", "Free external: " + freeBytesExternal);
			//Log.i("init", "Free internal: " + freeBytesInternal);
			
			Log.i("init", "Package name: " + ctx.getPackageName());
	}*/
	
	public void Close() {
		if (currentFolio != null) {
			currentFolio.close();
			currentFolio = null;
		}
	}
	
	public static Folio getCurrentFolio() {
		FolioLibraryService instance = getInstance();
        if (instance.currentFolio == null) {
            if (instance.databaseFileExists()) {
                instance.checkAvailableLibraries();
            }
        }

        return instance.currentFolio;
	}
	
	private static FlatFileSource mainSource = null;
	//private static String lastCollection = ""; 
	
	public static FlatFileSource getMainSource() {
		return getInstance().mainSource;
	}

	public File getMainDataFile(String subdir) {
		File mainTest = new File(getMainDataFilePath(subdir));
		return mainTest;
	}

    public static int getUpdateManagerStatus() {
        return getInstance().updateManagerStatus;
    }

    public static void setUpdateManagerStatus(int ums, SharedPreferences.Editor ed) {
        getInstance().updateManagerStatus = ums;
        if (ed != null) {
            ed.putInt("ProcessStatus", ums);
        }

        DatabasePageGroup dbPage = MainActivity.getInstance().databasePageGroup;
        switch (ums) {
            case FolioLibraryService.UPDATE_MANAGER_CHECKING:
                dbPage.setCheckingStatus();
                break;
            case FolioLibraryService.UPDATE_MANAGER_DOWNLOADING:
                dbPage.setDownloadStatusProgress(0);
                break;
            case FolioLibraryService.UPDATE_MANAGER_IDLE:
                dbPage.setIdleStatus();
                break;
            case FolioLibraryService.UPDATE_MANAGER_UPDATING:
                dbPage.setUpdatingStatus();
                break;
        }
    }

    //
    // this is very critical method
    // forcing update means deleting current database
    // very carefully shoudl be used by user
    //
    public void forceUpdateStart() {
        SharedPreferences sp = runContext.getSharedPreferences(FolioLibraryService.PREFERENCES_FILE, 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("ProcessStatus", UPDATE_MANAGER_IDLE);
        ed.putString("currentVersion", "0");
        ed.commit();

        // reset last time
        lastRelCheckTime = null;
        updateManagerStatus = UPDATE_MANAGER_IDLE;

        File mf = new File(getMainDataFilePath(APPDIR_NEW));
        if (mf.delete()) {
            Log.i("file", "File " + getMainDataFilePath(APPDIR_NEW) + " deleted successfuly");
        } else {
            Log.e("file", "File " + getMainDataFilePath(APPDIR_NEW) + " could not be deleted.");
        }

        // delayed check for database relevance
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // start update
                downloadDatabaseFile();
            }
        }, 1000);
    }

    @Override
    public void messageBoxAnswerOK(HashMap<String, String> mb) {

        if (!mb.containsKey("STEP")) {
            Log.e("", "Dialog data does not contain key STEP");
        }

        String stepKey = mb.get("STEP");
        if (stepKey.equals("versions")) {
            Log.i("UpdateManager", "We can start with downloading of files.");
            //startDatabaseDownloading(mb);
            downloadDatabaseFile();
        }
    }

    @Override
    public void messageBoxAnswerCancel(HashMap<String, String> mb) {

    }


    public void customReferencesDidChange() {
        if (currentFolio != null) {
//            currentFolio.saveCustomReferences();
            if (customReferencesBeforeWrite < 0) {
                customReferencesBeforeWrite = 5;
                triggerCountdownWrite();
            } else {
                customReferencesBeforeWrite = 5;
            }
        }
    }

    private void triggerCountdownWrite() {
        Log.i("Folio", "Count down timer before writing custom references: " + customReferencesBeforeWrite);

        MainActivity ma = MainActivity.getInstance();
        if (ma != null) {
            ma.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (customReferencesBeforeWrite == 0) {
                        commitChanges();
                        customReferencesBeforeWrite = -1;
                    } else {
                        customReferencesBeforeWrite--;
                        triggerCountdownWrite();
                    }
                }
            }, 1000L);
        }
    }

    public void commitChanges() {
        if (currentFolio != null) {
            currentFolio.saveCustomReferences();
        }
    }

    public String getNewVersionTitle() {
        return newVersionTitle;
    }

    public void setNewVersionTitle(String newVersionTitle) {
        this.newVersionTitle = newVersionTitle;
    }

    public static FolioLibraryService getInstance() {
        return instance;
    }


    public Handler getHandler() {
        if (handler == null) {
            MainActivity ma = MainActivity.getInstance();
            if (ma != null)
                handler = new Handler(ma.getMainLooper());
            else
                handler = new Handler();
        }
        return handler;
    }
}
