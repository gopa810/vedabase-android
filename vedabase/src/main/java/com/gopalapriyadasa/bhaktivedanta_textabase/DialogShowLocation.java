package com.gopalapriyadasa.bhaktivedanta_textabase;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DialogShowLocation extends DialogFragment {

	public String message;
	public Activity activity;
	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		this.activity = getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(R.string.dlg_title_location);
		
		builder.setMessage(message);
		
		builder.setPositiveButton(R.string.ecm_copy, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ClipboardManager clipboard = (ClipboardManager)
				        activity.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData newText = ClipData.newPlainText("location text", message);
				
				clipboard.setPrimaryClip(newText);
			}
		});
		
		builder.setNegativeButton(R.string.btn_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		return builder.create();
	}

}
