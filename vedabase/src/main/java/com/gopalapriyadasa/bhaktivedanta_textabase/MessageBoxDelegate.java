package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.util.HashMap;

public interface MessageBoxDelegate {

	public void messageBoxAnswerOK(HashMap<String,String> mb);
	public void messageBoxAnswerCancel(HashMap<String,String> mb);
	
}
