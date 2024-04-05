package com.gopalapriyadasa.textabase_engine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;

public class FlatFileUtils {

	public static String encodeLinkSafeString(String string) {

		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return string;
		}
	}
	
	public static String decodeLinkSafeString(String string) {
		try {
			return URLDecoder.decode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return string;
		}
	}

	
	public static HashSet<String> spaceTags = new HashSet<String>();
	
	public static void initSpaceTags() {
	    
		if (spaceTags.size() == 0) {
			String[] arr = new String[] {"<CR>", "<HR>", "<HS>", "<SP>",
	                "<SB>", "</SS>", "<TA>", "</TA>", "<CE>",
	                "</CE>", "<GP>", "<GD>", "<GM>", "<GT>",
	                "<GQ>", "<GI>", "<GA>", "<GF>"};
			
			for(String s : arr) {
				spaceTags.add(s);
			}
		}
	    
	}

	public static String removeTags(String str)
	{
	    if (str.isEmpty())
	        return "";
	    
	    StringBuilder  text = new StringBuilder(str);
	    
	    int start = 0;
	    int end = 0;
	    int status = 0;
	    int removeRange = 0;
	    int removedCount = 1;
	    
	    initSpaceTags();
	    
	    while (removedCount > 0) {
	        removedCount = 0;
	        for(int i = 0; (removedCount == 0) && (i < text.length()); i++)
	        {
	            if (status == 0) {
	                if (text.charAt(i) == '<') {
	                    status = 1;
	                }
	            } else if (status == 1) {
	                if (text.charAt(i) == '<') {
	                    status = 0;
	                } else {
	                    start = i-1;
	                    status = 2;
	                }
	            } else if (status == 2) {
	                if (text.charAt(i) == '>') {
	                    end = i;
	                    removeRange = 1;
	                    status = 0;
	                } else if (text.charAt(i) == '"') {
	                    status = 3;
	                }
	            } else if (status == 3) {
	                if (text.charAt(i) == '"') {
	                    status = 4;
	                }
	            } else if (status == 4) {
	                if (text.charAt(i) == '"') {
	                    status = 3;
	                } else if (text.charAt(i) == '>') {
	                    end = i;
	                    removeRange = 1;
	                    status = 0;
	                }
	            }
	            
	            if (removeRange == 1) {
	                String extractedTag = text.substring(start, end+1);
	                if (spaceTags.contains(extractedTag))
	                    text.replace(start, end + 1, " ");
	                else
	                    text.replace(start, end + 1, "");
	                removeRange = 0;
	                removedCount++;
	            }
	        }
	    }
	    
	    return text.toString();
	}

	public static String removeTagsAndNotes(String str)
	{
	    if (str.isEmpty())
	        return "";
	    
	    StringBuilder  text = new StringBuilder(str);
	    
	    int start = 0;
	    int end = 0;
	    int status = 0;
	    int removeRange = 0;
	    int removedCount = 1;
	    int pwLevel = 0;
	    int pwStart = 0;

	    initSpaceTags();
	    
	    while (removedCount > 0) {
	        removedCount = 0;
	        for(int i = 0; (removedCount == 0) && (i < text.length()); i++)
	        {
	            if (status == 0) {
	                if (text.charAt(i) == '<') {
	                    status = 1;
	                }
	            } else if (status == 1) {
	                if (text.charAt(i) == '<') {
	                    status = 0;
	                } else {
	                    start = i-1;
	                    status = 2;
	                }
	            } else if (status == 2) {
	                if (text.charAt(i) == '>') {
	                    end = i;
	                    removeRange = 1;
	                    status = 0;
	                } else if (text.charAt(i) == '"') {
	                    status = 3;
	                }
	            } else if (status == 3) {
	                if (text.charAt(i) == '"') {
	                    status = 4;
	                }
	            } else if (status == 4) {
	                if (text.charAt(i) == '"') {
	                    status = 3;
	                } else if (text.charAt(i) == '>') {
	                    end = i;
	                    removeRange = 1;
	                    status = 0;
	                }
	            }
	            
	            if (removeRange == 1) {
	                String extractedTag = text.substring(start, end+1);
	                if (extractedTag.startsWith("PW")) {
	                    if (pwLevel == 0)
	                        pwStart = start;
	                    pwLevel++;
	                } else if (extractedTag.startsWith("<LT")) {
	                    pwLevel--;
	                    if (pwLevel == 0) {
	                    	text.replace(pwStart, end + 1, "");
	                        removedCount++;
	                    }
	                } else if (pwLevel == 0) {
	                    if (spaceTags.contains(extractedTag))
	                        text.replace(start, end + 1, " ");
	                    else
	                        text.replace(start, end + 1, "");
	                    removedCount++;
	                }
	                removeRange = 0;
	            }
	        }
	    }
	    
	    return text.toString();
	}

	public static String getFileExt(String FileName)
    {       
         String ext = FileName.substring((FileName.lastIndexOf(".") + 1), FileName.length());
         return ext;
    }
	
	private static HashMap<String,String> mimeMap = new HashMap<String,String>();
	
	public static String getMimeType(String fileExt) {
		if (mimeMap.size() == 0) {
			initMimeMap();
		}
		if (mimeMap.containsKey(fileExt)) {
			return mimeMap.get(fileExt);
		}
		return "";
	}
	
	private static void initMimeMap() {
		mimeMap.put("png", "image/png");
		mimeMap.put("tif", "image/tiff");
		mimeMap.put("tiff", "image/tiff");
		mimeMap.put("bmp", "image/bmp");
		mimeMap.put("jpg", "image/jpg");
		mimeMap.put("jpeg", "image/jpg");
		mimeMap.put("gif", "image/gif");
	}

	public static boolean isImageFileExtension(String objectExtension) {
		return getMimeType(objectExtension).startsWith("image/");
	}	
}
