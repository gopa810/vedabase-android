package com.gopalapriyadasa.bhaktivedanta_textabase;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogTextViewActions extends DialogFragment {

	private MainActivity delegate;
	private CharSequence[] items;
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(R.string.dlg_title_tvmenu);
		builder.setItems(this.items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (delegate != null) {
					delegate.textViewActionSelected(which);
				}
			}
		});
		
		return builder.create();
	}
	public MainActivity getDelegate() {
		return delegate;
	}
	public void setDelegate(MainActivity delegate) {
		this.delegate = delegate;
	}
	public CharSequence[] getItems() {
		return items;
	}
	public void setItems(CharSequence[] items) {
		this.items = items;
	}

}
