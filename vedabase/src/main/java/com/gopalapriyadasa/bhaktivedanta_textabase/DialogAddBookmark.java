package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.util.Calendar;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.VBBookmark;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class DialogAddBookmark extends DialogFragment {

	
	MainActivity main = null;
	EditText editText = null;
	AlertDialog thisDialog = null;
	public boolean showMainBookmarkDialogAfter = true;
	public int currentRecordId = -1;
	
	@Override
	public void onAttach(Activity activity) {
		try {
		main = (MainActivity)activity;
		Log.i("dlg", "main is assigned");
		} catch(ClassCastException e) {
		}

		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View myView = inflater.inflate(R.layout.dialog_add_bookmark, null);
		editText = (EditText)myView.findViewById(R.id.editText1);
		
		builder.setView(myView);
		builder.setPositiveButton(R.string.btn_add,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						VBBookmark bkmk = new VBBookmark();
						bkmk.setName(editText.getText().toString());
						Log.i("bkmk", "new bookmark is " + bkmk.getName());
						
						if (currentRecordId < 0) {
							currentRecordId = main.textPageGroup.getCurrentRecordId();
						}
						bkmk.setRecordId(currentRecordId);
						Calendar c = Calendar.getInstance();
						bkmk.setCreateDate(c.getTime());

						Folio currentFolio = FolioLibraryService.getCurrentFolio();
						if (currentFolio != null) {
							currentFolio.addBookmark(bkmk);
							currentFolio.saveCustomReferences();
						}
						
						if (showMainBookmarkDialogAfter) {
							main.showBookmarkDialog();
						}
						
						main.contentPageGroup.onBookmarksChanged();
					}
				});

		builder.setNegativeButton(R.string.btn_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		editText.addTextChangedListener(new TextWatcher() {

			private void handleChange() {
				if (editText.getText().toString().trim().length() == 0) {
					thisDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					Folio currentFolio = FolioLibraryService.getCurrentFolio();
					if (currentFolio != null) {
						VBBookmark bkmk = currentFolio.findBookmarkByName(editText.getText().toString());
						thisDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(bkmk == null);
					}
				}
			}
			@Override
			public void afterTextChanged(Editable arg0) {
				handleChange();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
			}
			
		});
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					if (event.getAction() == KeyEvent.KEYCODE_ENTER)
						return true;
				}
				return false;
			}
		});
		
		thisDialog = builder.create();
		
		thisDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);				
			}
		});
		
		return thisDialog;
	}

}
