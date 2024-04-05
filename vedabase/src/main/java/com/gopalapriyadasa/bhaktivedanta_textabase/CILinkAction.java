package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Gopa702 on 7/27/2016.
 */
public class CILinkAction extends LinearLayout {
    private TextView textView;
    private String actionText;
    private ContentListAdapter adapter;

    public CILinkAction(Context context) {
        super(context);
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
        li.inflate(R.layout.ci_linkaction, this, true);

        actionText = null;
        textView = (TextView)findViewById(R.id.textView14);

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                CILinkAction row = CILinkAction.this;
                if (row.adapter != null) {
                    row.adapter.executeAction(actionText);
                }
            }
        });
    }

    public void setText(String s) {
        textView.setText(s);
    }

    public void setActionText(String s) {
        actionText = s;
    }

    public void setAdapter(ContentListAdapter adapter) {
        this.adapter = adapter;
    }
}
