package com.gopalapriyadasa.textabase_engine;

import android.graphics.Paint;
import android.util.Log;

import java.util.HashSet;

public class VBHighlighterAnchor {

	public static String[] colors = new String [] {
	    "#ffff00",
	    "#00ff00",
	    "#00ffff",
	    "#ff0000",
	    "#ff00ff",
	    "#ff6600",
	    "#ff6699",
	    "#6666ff",
	    "#7f7fff",
	    "#ff7f7f"
	};

	public static int[] binColors = new int [] {
	    0xffffff00,
	    0xff00ff00,
	    0xff00ffff,
	    0xffff0000,
	    0xffff00ff,
	    0xffff6600,
	    0xffff6699,
	    0xff6666ff,
	    0xff7f7fff,
	    0xffff7f7f
	};
	
	public static Paint[] colorPaints = new Paint[16];


	public byte[] highlighterMap = null;
	
	public int getHighlighterAtPos(int index) {
		if (highlighterMap == null || highlighterMap.length <= index)
			return -1;
		
		return highlighterMap[index];
	}
	
	public void setHighlighterRange(int start, int end, int highlighterId) {
		if (highlighterMap == null) {
			highlighterMap = new byte[end];
		} else if (highlighterMap.length < end) {
			byte[] target = new byte[end];
			System.arraycopy(highlighterMap, 0, target, 0, highlighterMap.length);
			highlighterMap = target;
		}
		
		for(int i = start; i < end; i++) {
			highlighterMap[i] = (byte)highlighterId;
		}
	}

	public void getHtmlColorCodes(HashSet<String> colorset) {
		if (highlighterMap == null)
			return;
		for (byte b : highlighterMap) {
			if (b >= 0 && !colorset.contains(colors[b])) {
				colorset.add(colors[b]);
			}
		}
	}

	public int startChar;
	public int highlighterId;

	public int getStartChar() {
		return startChar;
	}

	public void setStartChar(int startChar) {
		this.startChar = startChar;
	}

	public void setHighlighter(int j, int highlighterId2) {
		if (highlighterMap == null) {
			highlighterMap = new byte[j + 16];
			for(int a = 0; a < j+16; a++) {
				highlighterMap[a] = -1;
			}
		} else if (highlighterMap.length <= j) {
			byte[] target = new byte[j + 16];
			for(int a = 0; a < j+16; a++) {
				target[a] = -1;
			}
			System.arraycopy(highlighterMap, 0, target, 0, highlighterMap.length);
			highlighterMap = target;
		}
		
		highlighterMap[j] = (byte)highlighterId2;
		
		StringBuilder sb = new StringBuilder();
		for(int a = 0; a < highlighterMap.length; a++) {
			sb.append(String.format("%d,", highlighterMap[a]));
		}
		Log.i("high", "highId:" + highlighterId2);
		Log.i("high", sb.toString());
	}

	public static Paint getPaint(int highlighterId2) {

		if (highlighterId2 < 0 || highlighterId2 >= binColors.length)
			return null;
		Paint p = colorPaints[highlighterId2];
		if (p == null) {
			p = new Paint();
			p.setStyle(Paint.Style.FILL);
			p.setColor(binColors[highlighterId2]);
			colorPaints[highlighterId2] = p;
		}
		return p;
	}

}
