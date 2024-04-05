package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.gopalapriyadasa.textabase_engine.FolioLibraryService;

import java.util.HashMap;


/**
 * Created by Gopa702 on 6/27/2016.
 */
public class VBDownloader extends BroadcastReceiver {


    private static HashMap<String,VBDownloaderDelegate> downloadsMap = new HashMap<String, VBDownloaderDelegate>();
    private static HashMap<String,String> downloadsMapTags = new HashMap<String, String>();
    private static String p_currentRefId;
    private static long p_currentId;


    public static void onStart() {
        if (p_currentId <= 0) {
            SharedPreferences sp = FolioLibraryService.getSharedPreferences();
            p_currentId = sp.getLong("recentDownfileId", -1);
            p_currentRefId = "vb" + p_currentId;
        }
    }

    public static VBDownloadedFileInfo CurrentFileInfo(Context context) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(p_currentId);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            Log.i("DWN", "Found progress in queue");
            if (cursor.moveToFirst()) {
                VBDownloadedFileInfo vbi = new VBDownloadedFileInfo();
                vbi.Status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch(vbi.Status)
                {
                    case DownloadManager.STATUS_FAILED:
                        vbi.StatusText = "Downloading has failed";
                        break;
                    case DownloadManager.STATUS_PENDING:
                        vbi.StatusText = "Downloading is pending";
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        vbi.StatusText = "Downloading data for Vedabase";
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        vbi.StatusText = "Data downloaded successfuly";
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        vbi.StatusText = "Downloading paused";
                        break;
                }
                vbi.Name = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                vbi.Size = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                vbi.DownloadedSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                vbi.DetailsText = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                cursor.close();

                return vbi;
            }
        }

        return null;
    }

    //
    // DOWNLOAD FILE ASYNCHRONOUSLY
    // after downloading, call method onFileDownloaded in delegate and use argument tag
    // as identificator for the file
    //
    public static boolean downloadFile(String fileUrl, VBDownloaderDelegate delegate, String tag) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }

        String url = fileUrl;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Database for Bhaktivedanta Vedabase containing all texts.");
        request.setTitle("Vedabase Database");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        //request.setDestinationInExternalFilesDir(MainActivity.getInstance(), Environment.DIRECTORY_DOWNLOADS, "vedabase.ivd");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "vedabase.ivd");
        request.setVisibleInDownloadsUi(true);

// get download service and enqueue file
        DownloadManager manager = (DownloadManager) MainActivity.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
        p_currentId = manager.enqueue(request);
        p_currentRefId = "vb" + p_currentId;
        downloadsMap.put(p_currentRefId, delegate);
        downloadsMapTags.put(p_currentRefId, tag);
        Log.i("DWN", "request for file put into queue.");

        SharedPreferences sp = FolioLibraryService.getSharedPreferences();
        SharedPreferences.Editor esp = sp.edit();
        esp.putLong("recentDownfileId", p_currentId);
        esp.commit();


        return true;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        FolioLibraryService.getInstance().acceptContext(context);
        long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        String refTag = "vb" + reference;
        Log.i("DWN", "Downloaded " + refTag);
        if (downloadsMap.containsKey(refTag)) {
            Log.i("DWN", "Found in map");
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(reference);
            Cursor cursor = downloadManager.query(query);
            if (cursor != null) {
                Log.i("DWN", "Found in queue");
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    String fileSaved = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    VBDownloaderDelegate delegate = downloadsMap.get(refTag);
                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Log.i("DWN", "Status is: SUCCESS");
                            delegate.onFileDownloaded(downloadsMapTags.get(refTag), fileSaved, true, reason);
                            downloadsMap.remove(refTag);
                            downloadsMapTags.remove(refTag);
                            break;
                        case DownloadManager.STATUS_FAILED:
                            Log.i("DWN", "Status is: FAILED");
                            delegate.onFileDownloaded(downloadsMapTags.get(refTag), fileSaved, false, reason);
                            downloadsMap.remove(refTag);
                            downloadsMapTags.remove(refTag);
                            break;
                        default:
                            Log.i("DWN", "Status is: " + status);
                            break;
                    }

                }

                cursor.close();
            }
            else {
                Log.i("DWN", "Not found in queue");
            }
        }

        SharedPreferences sp = FolioLibraryService.getSharedPreferences();
        SharedPreferences.Editor esp = sp.edit();
        esp.putLong("recentDownfileId", -1);
        esp.commit();

    }
}
