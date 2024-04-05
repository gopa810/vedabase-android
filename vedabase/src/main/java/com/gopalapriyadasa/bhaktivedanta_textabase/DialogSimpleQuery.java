package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.FolioQueryInstance;
import com.gopalapriyadasa.textabase_engine.FolioQueryTemplate;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogSimpleQuery extends DialogFragment {

	private MainActivity mainActivity = null;
	private EditText queryEditText = null;
	private TextView titleTextView = null;
	private String templateName = "";
	private Button prevButton = null;
	private Button nextButton = null;
	private Button clearButton = null;
	private int currentTemplateIndex = 0;
	//private Folio folio = null;
	
	@Override
	public void onAttach(Activity activity) {
		try {
			mainActivity = (MainActivity) activity;
		} catch (ClassCastException e) {
			mainActivity = null;
		}
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View myView = inflater.inflate(R.layout.dialog_simple_query, null);
		
		queryEditText = (EditText)myView.findViewById(R.id.editText1);

		builder.setView(myView);
		builder.setPositiveButton(R.string.popup_search,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i("dlg", "search button clicked");
						Folio folio = FolioLibraryService.getCurrentFolio();
						if (folio != null) {
							FolioQueryInstance fqi = new FolioQueryInstance();
							fqi.name = templateName;
							fqi.data = queryEditText.getText().toString();
							fqi.scopeIndex = 0;
							
							FolioQueryTemplate fqt = folio.findQueryTemplate(templateName);
							if (fqt != null) {
								Log.i("ass344", "aswees");
								fqi.pattern = fqt.pattern; 
							} else {
								Log.i("ass", "ass");
								fqi.name = "";
							}
							folio.addQueryHistory(fqi);
							mainActivity.searchPageGroup.startQuerySearch(fqi);
						}
					}
				});

		builder.setNegativeButton(R.string.popup_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		titleTextView = (TextView)myView.findViewById(R.id.statusTitle);
		updateDialogTitle();

		prevButton = (Button)myView.findViewById(R.id.buttonShowMenu);
		nextButton = (Button)myView.findViewById(R.id.buttonClosePane);
		clearButton = (Button)myView.findViewById(R.id.button3);

		
		prevButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentTemplateIndex > 0) {
					currentTemplateIndex--;
				}
				updateQueryField();
				validateHistoryButtons();
			}


		});
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Folio folio = FolioLibraryService.getCurrentFolio();
				if (folio != null) {
					int count = folio.queryHistory.size();
					if (currentTemplateIndex < (count - 1)) {
						currentTemplateIndex++;
					}
					updateQueryField();
					validateHistoryButtons();
				}
			}
		});
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Folio folio = FolioLibraryService.getCurrentFolio();
				if (folio != null) {
					currentTemplateIndex = folio.queryHistory.size();
					updateQueryField();
					validateHistoryButtons();
				}
			}
		});

		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			currentTemplateIndex = folio.queryHistory.size();
		}

		validateHistoryButtons();
		
		return builder.create();

	}

	public void updateQueryField() {
		queryEditText.setText("");
		templateName = "";
		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			if (folio.queryHistory.size() > currentTemplateIndex) {
				FolioQueryInstance qi = folio.queryHistory.get(currentTemplateIndex);
				templateName = qi.name;
				queryEditText.setText(qi.data);
			}
		}
		updateDialogTitle();
	}
	
	public void updateDialogTitle() {
		if (templateName != null && templateName.length() > 0) {
			titleTextView.setText("Query [" + templateName + "]");
		} else {
			titleTextView.setText("Query");
		}
	}

	private void validateHistoryButtons() {

		Folio folio = FolioLibraryService.getCurrentFolio();
		if (folio != null) {
			int count = folio.queryHistory.size();
			prevButton.setEnabled(count > 0 && currentTemplateIndex > 0);
			nextButton.setEnabled(count > 0 && currentTemplateIndex < (count - 1));
		} else {
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
		}
	}

	public void setTemplateName(CharSequence title) {

		templateName = (String) title;
	}

}
