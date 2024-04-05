package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Gopa702 on 6/9/2016.
 */
public class UIManager {

    public static String getHtmlHexColor(int idColor) {
        MainActivity ma = MainActivity.getInstance();
        if (ma != null) {
            int color = ma.getResources().getColor(idColor);
            return String.format("#%06x", (color & 0xFFFFFF));
        }
        return "black";
    }

    public static InputStream getAssetStream(String assetName) throws IOException {
        MainActivity ma = MainActivity.getInstance();
        if (ma != null)
            return ma.getAssets().open(assetName);
        return null;
    }
}
