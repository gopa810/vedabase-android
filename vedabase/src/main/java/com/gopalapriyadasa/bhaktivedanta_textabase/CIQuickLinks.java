package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gopalapriyadasa.textabase_engine.VBContentRow;

/**
 * Created by Gopa702 on 7/26/2016.
 */
public class CIQuickLinks extends RelativeLayout {

    private View contentLink;
    private View bookmarkLink;
    private View noteLink;
    private View highlightLink;
    private View appLink;

    public CIQuickLinks(Context context) {
        super(context);

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
        li.inflate(R.layout.ci_quicklinks, this, true);

        contentLink = findViewById(R.id.quickLinkContents);
        bookmarkLink = findViewById(R.id.quickLinkBookmarks);
        noteLink = findViewById(R.id.quickLinkNotes);
        highlightLink = findViewById(R.id.quickLinkHighlights);
        appLink = findViewById(R.id.quickLinkAppMap);
    }

    public void setActionsForPage(final ContentPageGroup page) {

        contentLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.executeCommand("load contents 0");
            }
        });
        bookmarkLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.executeCommand("load bookmark 0");
            }
        });
        noteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.executeCommand("load note 0");
            }
        });
        highlightLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.executeCommand("load hightext 0");
            }
        });
        appLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page.executeCommand("show appmap");
            }
        });
    }


    public void showAllExcept(int itemType) {
        switch (itemType) {
            case VBContentRow.TYPE_ITEM:
                contentLink.setVisibility(GONE);
                bookmarkLink.setVisibility(VISIBLE);
                noteLink.setVisibility(VISIBLE);
                highlightLink.setVisibility(VISIBLE);
                break;
            case VBContentRow.TYPE_BOOKMARK:
                contentLink.setVisibility(VISIBLE);
                bookmarkLink.setVisibility(GONE);
                noteLink.setVisibility(VISIBLE);
                highlightLink.setVisibility(VISIBLE);
                break;
            case VBContentRow.TYPE_HIGHLIGHTER:
                contentLink.setVisibility(VISIBLE);
                bookmarkLink.setVisibility(VISIBLE);
                noteLink.setVisibility(VISIBLE);
                highlightLink.setVisibility(GONE);
                break;
            case VBContentRow.TYPE_NOTE:
                contentLink.setVisibility(VISIBLE);
                bookmarkLink.setVisibility(VISIBLE);
                noteLink.setVisibility(GONE);
                highlightLink.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
        appLink.setVisibility(VISIBLE);
    }
}
