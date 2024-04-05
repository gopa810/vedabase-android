package com.gopalapriyadasa.bhaktivedanta_textabase;

/**
 * Created by Gopa702 on 6/27/2016.
 */
public interface VBDownloaderDelegate {

    public int onFileDownloaded(String fileTag, String filePath, boolean success, int reason);

}
