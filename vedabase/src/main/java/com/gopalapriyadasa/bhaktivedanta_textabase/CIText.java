package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Gopa702 on 7/27/2016.
 */
public class CIText extends LinearLayout {

    private TextView textView;

    public CIText(Context context) {
        super(context);
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
        li.inflate(R.layout.ci_text, this, true);

        textView = (TextView)findViewById(R.id.textView13);
    }

    public void setText(String s) {
        textView.setText(s);
    }
}
