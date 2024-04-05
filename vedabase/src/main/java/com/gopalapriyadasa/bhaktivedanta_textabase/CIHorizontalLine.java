package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by Gopa702 on 7/26/2016.
 */
public class CIHorizontalLine extends LinearLayout {
    public CIHorizontalLine(Context context) {
        super(context);
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
        li.inflate(R.layout.ci_horizontal_line, this, true);

    }
}
