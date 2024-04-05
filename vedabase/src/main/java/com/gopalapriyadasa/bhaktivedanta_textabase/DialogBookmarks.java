package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.Folio;
import com.gopalapriyadasa.textabase_engine.FolioLibraryService;
import com.gopalapriyadasa.textabase_engine.VBBookmark;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DialogBookmarks extends DialogFragment {

	private MainActivity mainActivity = null;
	//private Folio currentFolio = null;
	private ListView list = null;
	private BookmarkAdapter bookmarkAdapter = null;
	private Button btnAdd = null;
	private Button btnUpdate = null;
	private Button btnRemove = null;
	private Button btnGoto = null;
	private TextView emptyLabel = null;
	private DialogBookmarks thisDialog = null;
	
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

		thisDialog = this;
		
		View myView = inflater.inflate(R.layout.dialog_bookmarks, null);
		list = (ListView)myView.findViewById(R.id.listView1);
		btnAdd = (Button)myView.findViewById(R.id.btnAdd);
		btnUpdate = (Button)myView.findViewById(R.id.btnUpdate);
		btnRemove = (Button)myView.findViewById(R.id.btnRemove);
		btnGoto = (Button)myView.findViewById(R.id.btnGoto);
		emptyLabel = (TextView)myView.findViewById(R.id.textEmptyLabel);
		
		list.setItemsCanFocus(true);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				btnAdd.setEnabled(true);
				btnUpdate.setEnabled(true);
				btnRemove.setEnabled(true);
				btnGoto.setEnabled(true);
				Log.i("dlgs", "item selected");
				bookmarkAdapter.currentPosition = position;
				list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				list.setItemChecked(position, true);
			}
		});
			
	
		builder.setView(myView);
		builder.setPositiveButton(R.string.popup_close,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		/*
		 * add button handler
		 */
		btnAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				thisDialog.dismiss();
				DialogFragment dlg = new DialogAddBookmark();
				dlg.show(mainActivity.getFragmentManager(), "addbkmk");
			}
		});
		
		/*
		 * update button handler
		 */
		btnUpdate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (bookmarkAdapter.currentPosition >= 0) {
					Log.i("bkmk", "selected item is: " + bookmarkAdapter.currentPosition);
					VBBookmark bkmk = bookmarkAdapter.getItem(bookmarkAdapter.currentPosition);
					if (bkmk != null) {
						bkmk.setRecordId(mainActivity.textPageGroup.getCurrentRecordId());
						Folio currentFolio = FolioLibraryService.getCurrentFolio();
						if (currentFolio != null) {
							currentFolio.saveCustomReferences();
						}
					}
				}
				thisDialog.dismiss();
			}
		});

		/*
		 * remove button handler
		 */
		btnRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (bookmarkAdapter.currentPosition >= 0) {
					Log.i("bkmk", "removed item is: " + bookmarkAdapter.currentPosition);
					AlertDialog dialog = new AlertDialog.Builder(mainActivity).setMessage(R.string.dlg_bkmk_remove)
			                .setPositiveButton(android.R.string.ok, new OnClickListener() {

			                    @Override
			                    public void onClick(DialogInterface dialog, int which) {
			                        dialog.dismiss();
			                        if (bookmarkAdapter.currentPosition >= 0 && bookmarkAdapter.currentPosition < bookmarkAdapter.getCount()) {
			                        	bookmarkAdapter.remove(bookmarkAdapter.getItem(bookmarkAdapter.currentPosition));
			                        } else {
			                        	bookmarkAdapter.currentPosition = -1;
			                        }
			                        
			                        if (bookmarkAdapter.currentPosition >= bookmarkAdapter.getCount()) {
			                        	bookmarkAdapter.currentPosition = bookmarkAdapter.getCount() - 1;
			                        }
									Folio currentFolio = FolioLibraryService.getCurrentFolio();
									if (currentFolio != null) {
										currentFolio.saveCustomReferences();
									}
			                    }
			                }).setNegativeButton(android.R.string.cancel, new OnClickListener() {

			                    @Override
			                    public void onClick(DialogInterface dialog, int which) {
			                        dialog.dismiss();
			                    }
			                }).setOnCancelListener(new OnCancelListener() {

			                    @Override
			                    public void onCancel(DialogInterface dialog) {
			                        dialog.dismiss();
			                    }
			                }).create();
					dialog.show();
				}
			}
		});
		
		btnGoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (bookmarkAdapter.currentPosition >= 0) {
					VBBookmark bkmk = bookmarkAdapter.getItem(bookmarkAdapter.currentPosition);
					mainActivity.SetCurrentTab(R.id.textPane);
					mainActivity.LoadFolioTextRecords(bkmk.getRecordId(), true);
					thisDialog.dismiss();
				}
				
			}
		});

		Folio currentFolio = FolioLibraryService.getCurrentFolio();
		bookmarkAdapter = new BookmarkAdapter(this.getActivity(), R.layout.bookmark_list_item, currentFolio.getBookmarks());
		list.setAdapter(bookmarkAdapter);
		
		validateButtons();

		
		return builder.create();
	}

	public MainActivity getMainActivity() {
		return mainActivity;
	}

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public Folio getCurrentFolio() {
		return FolioLibraryService.getCurrentFolio();
	}

	public void setCurrentFolio(Folio currentFolio) {

	}
	
	public void validateButtons() {
		emptyLabel.setVisibility(bookmarkAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
		//list.setVisibility(bookmarkAdapter.getCount() == 0 ? View.GONE : View.VISIBLE);
	}

}
