/**
 * 
 */
package com.gopalapriyadasa.bhaktivedanta_textabase;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Gopa702
 *
 */
public class CITitle extends LinearLayout {

	public TextView mainTitle;
	
	public CITitle(Context context) {
		super(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
		li.inflate(R.layout.ci_title, this, true);
		
		mainTitle = (TextView)findViewById(R.id.statusTitle);
	}
	
	public final String getMainTitle() {
		return mainTitle.getText().toString();
	}

	public final void setMainTitle(String mainTitle) {
		this.mainTitle.setText(mainTitle);
	}

	public CITitle(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

}
