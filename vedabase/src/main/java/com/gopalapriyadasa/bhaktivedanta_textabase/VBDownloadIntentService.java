package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Gopa702 on 6/27/2016.
 */
public class VBDownloadIntentService extends IntentService {

    public VBDownloadIntentService() {
        super(VBDownloadIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent i = new Intent(this, VBDownloadIntentService.class);
        sendBroadcast(i);
    }
}
