package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.gopalapriyadasa.textabase_engine.FolioLibraryService;

/**
 * Created by Gopa702 on 7/5/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // refreshing idle status message when connection is changed
        if (FolioLibraryService.getUpdateManagerStatus() == FolioLibraryService.UPDATE_MANAGER_IDLE) {
            MainActivity ma = MainActivity.getInstance();
            if (ma != null && ma.databasePageGroup != null) {
                ma.databasePageGroup.setIdleStatus();
            }
        } else {
            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                // connection was lost
                // TODO: do some action for downloading status, not necessary for updating or checking

            }
        }
        /*FolioLibraryService service =  FolioLibraryService.getInstance();
        String status = service.isNetworkAvailable() ? "Network available" : "Network not available";
        Toast.makeText(context, status, Toast.LENGTH_LONG).show();*/
    }
}
