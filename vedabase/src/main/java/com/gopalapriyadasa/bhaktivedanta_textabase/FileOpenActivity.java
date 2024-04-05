package com.gopalapriyadasa.bhaktivedanta_textabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

public class FileOpenActivity extends Activity {

    private UiView mView;
    private Button buttonParent;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_file_open);

        mView = (UiView) getFragmentManager().findFragmentById(R.id.file_list);
        buttonParent = (Button)findViewById(R.id.button);
        buttonCancel = (Button)findViewById(R.id.button2);

        buttonParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.goParentFolder();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }
}
