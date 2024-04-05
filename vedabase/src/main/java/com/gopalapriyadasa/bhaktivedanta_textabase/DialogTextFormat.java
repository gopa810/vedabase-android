package com.gopalapriyadasa.bhaktivedanta_textabase;

import com.gopalapriyadasa.textabase_engine.FDCharFormat;
import com.gopalapriyadasa.textabase_engine.FDTypeface;
import com.gopalapriyadasa.textabase_engine.FlatParagraph;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class DialogTextFormat extends DialogFragment {

	private MainActivity mainActivity = null;
	private SeekBar seekBar1 = null;
	private SeekBar seekBar2 = null;
	private RadioGroup radioGroup = null;
	private RadioButton radio0 = null;
	private RadioButton radio1 = null;
	private TextView textView = null;

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

		View myView = inflater.inflate(R.layout.dialog_format, null);

		builder.setView(myView);
		builder.setPositiveButton(R.string.popup_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						FlatParagraph.setFontMultiplyer(convertProgressToBodySize(seekBar1.getProgress()));
						FlatParagraph.setDefaultFont(convertRadioIdToFontFace(radioGroup.getCheckedRadioButtonId()));
						FDCharFormat.setMultiplySpaces(convertProgressToLineSpacing(seekBar2.getProgress()));
						mainActivity.textPageGroup.textStyleSettingsDidChange();
					}
				});

		builder.setNegativeButton(R.string.popup_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		
		seekBar1 = (SeekBar)myView.findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar)myView.findViewById(R.id.seekBar2);
		radioGroup = (RadioGroup)myView.findViewById(R.id.radioGroup1);
		radio0 = (RadioButton)myView.findViewById(R.id.radio0);
		radio1 = (RadioButton)myView.findViewById(R.id.radio1);
		textView = (TextView)myView.findViewById(R.id.textView3);
		radio0.setText(FDTypeface.TIMES_FONT);
		radio1.setText(FDTypeface.ARIAL_FONT);
		radio0.setTypeface(FDTypeface.getTypeface(FDTypeface.TIMES_FONT, false, false));
		radio1.setTypeface(FDTypeface.getTypeface(FDTypeface.ARIAL_FONT, false, false));

		seekBar1.setMax(100);
		seekBar2.setMax(100);
		radioGroup.clearCheck();
		
		seekBar1.setProgress(convertBodySizeToProgress(FDCharFormat.getMultiplyFontSize()));
		seekBar2.setProgress(convertLineSpacingToProgress(FDCharFormat.getMultiplySpaces()));
		radioGroup.check(convertFontFaceToRadioId(FDTypeface.getDefaultFontName()));
		setExampleFontByRadioId(radioGroup.getCheckedRadioButtonId());
		textView.setTextSize(FDCharFormat.getMultiplyFontSize() *14);
		textView.setLineSpacing(0, FDCharFormat.getMultiplySpaces());
		
		seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					textView.setTextSize(convertProgressToBodySize(progress)*14);
				}
			}
		});
		
		seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					textView.setLineSpacing(0, convertProgressToLineSpacing(progress));
				}
			}
		});


		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				setExampleFontByRadioId(checkedId);
			}
		});
		
		return builder.create();
	}

	public void setExampleFontByRadioId(int checkedId) {
		if (checkedId == R.id.radio0) {
			textView.setTypeface(radio0.getTypeface());
		} else if (checkedId == R.id.radio1) {
			textView.setTypeface(radio1.getTypeface());
		}
	}
	
	public int convertBodySizeToProgress(float size) {
		if (size < 1)
			size = 1;
		if (size > 5)
			size = 5;
		return (int)((size - 1)*25);
	}
	
	public float convertProgressToBodySize(int progress) {
		return 1 + ((float)progress)/25;
	}
	
	public int convertLineSpacingToProgress(float spacing) {
		if (spacing < 1)
			spacing = 1;
		if (spacing > 3)
			spacing = 3;
		return (int)((spacing - 1) * 50);
	}
	
	public float convertProgressToLineSpacing(int progress) {
		return ((float)progress)/50 + 1;
	}
	
	public String convertRadioIdToFontFace(int id) {
		if (id == R.id.radio0) {
			return radio0.getText().toString();
		} else if (id == R.id.radio1) {
			return radio1.getText().toString();
		}		
		return "";
	}
	
	public int convertFontFaceToRadioId(String str) {
		if (str.equals(radio0.getText()))
			return R.id.radio0;
		if (str.equals(radio1.getText()))
			return R.id.radio1;
		return -1;
	}
}
