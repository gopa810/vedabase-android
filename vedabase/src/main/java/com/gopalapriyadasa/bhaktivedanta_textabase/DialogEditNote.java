package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.VBCustomNotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogEditNote extends DialogFragment {

	private EditText editText = null;
	private MainActivity mainActivity = null;
	public int recordId = -1;
	private int recordToSet = -1;
	private VBCustomNotes notes = null;

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

		View myView = inflater.inflate(R.layout.dialog_edit_note, null);
		editText = (EditText)myView.findViewById(R.id.editText1);

		builder.setView(myView);
		builder.setPositiveButton(R.string.popup_close,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
					}
				});

		builder.setNegativeButton(R.string.btn_update,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Folio folio = FolioLibraryService.getCurrentFolio();
						if (notes == null) {
							if (recordId > 0) {
								notes = folio.safeRecordNotesForRecord(recordId); 
							}
						}
						if (notes != null) {
							notes.setNoteText(editText.getText().toString());
							mainActivity.textPageGroup.endlessTextNotesChanged();
						}
					}
				});
		
		if (recordToSet >= 0) {
			setRecord(recordToSet);
		}

		return builder.create();
	}

	public void setRecord(int record) {

		if (editText != null) {
			Folio folio = FolioLibraryService.getCurrentFolio();
			recordId = record;
			notes = null;
			if (folio != null)
				notes = folio.recordNotesForRecord(record);
			if (notes != null && notes.getNoteText() != null) {
				editText.setText(notes.getNoteText());
			}
		} else {
			recordToSet = record;
		}
	}

}
