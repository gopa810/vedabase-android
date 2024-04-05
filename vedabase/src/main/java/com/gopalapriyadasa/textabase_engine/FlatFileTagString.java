package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;

public class FlatFileTagString {

	public static int  MAKEARRAY_STATUS_DEFAULT = 0;
	public static int  MAKEARRAY_STATUS_START_DECISION = 1;
	public static int  MAKEARRAY_STATUS_QUOTE_READ = 2;
	public static int  MAKEARRAY_STATUS_END_QUOTE = 3;
	public static int  MAKEARRAY_STATUS_READ_TAG = 4;

	
	StringBuilder _buffer = new StringBuilder();
	StringBuilder _extractedTag = new StringBuilder();
	
	public boolean isTagChar(char c) {
		return (Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '/');
	}
	
	public String tag()
	{
	    if (_extractedTag.toString().isEmpty())
	    {
	        int startIdx = 0;
	        if (_buffer.charAt(0) == '<')
	            startIdx++;
	        for (int i = startIdx; i < _buffer.length(); i++)
	        {
	            if (isTagChar(_buffer.charAt(i)))
	                _extractedTag.append(_buffer.charAt(i));
	            else
	                break;
	        }
	    }
	    return _extractedTag.toString();
	}

	public ArrayList<String> createArray() {
		//String tempPart = ";
		StringBuilder  part = new StringBuilder();
		ArrayList<String> tagParts = new ArrayList<String>();
		int brackets = 0;
		int status = MAKEARRAY_STATUS_DEFAULT;
	    int nextStatus = MAKEARRAY_STATUS_DEFAULT;
	    
		// main import procedure
		for(int idx = 0; idx < _buffer.length(); idx++)
		{
			char rd = _buffer.charAt(idx);
	        if (status == MAKEARRAY_STATUS_DEFAULT) {
	            if (rd == '<') {
	                status = MAKEARRAY_STATUS_START_DECISION;
	                nextStatus = MAKEARRAY_STATUS_DEFAULT;
	            }
	        } else if (status == MAKEARRAY_STATUS_START_DECISION) {
	            if (rd == '<') {
	                status = nextStatus;
	            } else {
	                part.append(rd);
	                brackets++;
	                status = MAKEARRAY_STATUS_READ_TAG;
	            }
	        } else if (status == MAKEARRAY_STATUS_QUOTE_READ) {
	            if (rd == '\"') {
	                status = MAKEARRAY_STATUS_END_QUOTE;
	            } else {
	                part.append(rd);
	            }
	        } else if (status == MAKEARRAY_STATUS_END_QUOTE) {
	            if (rd == '\"') {
	                part.append("\"");
	                status = MAKEARRAY_STATUS_QUOTE_READ;
	            } else {
	                tagParts.add(part.toString());
	                part.delete(0, part.length());
	                idx--;
	                status = MAKEARRAY_STATUS_READ_TAG;
	                continue;
	            }
	        } else if (status == MAKEARRAY_STATUS_READ_TAG) {
	            if (rd == '<') {
	                brackets++;
	                part.append("<");
	            } else if (rd == ':' || rd == ' ' || rd == ';' || rd == ',') {
	                if (part.length() > 0)
	                {
	                    tagParts.add(part.toString());
	                    part.delete(0,part.length());
	                }
	                if (rd != ' ')
	                {
	                    part.delete(0,part.length());
	                    tagParts.add(String.format("%c", rd));
	                }
	            }
	            else if (rd == '>')
	            {
	                brackets--;
	                if (brackets == 0)
	                {
	                    if (part.length() > 0)
	                    {
	                        tagParts.add(part.toString());
							part.delete(0,part.length());
	                    }
	                    break;
	                }
	                else 
	                {
	                    part.append(">");
	                }
	            }
	            else if (rd == '\"')
	            {
	                if (part.length() > 0)
	                {
	                    tagParts.add(part.toString());
	                    part.delete(0,part.length());
	                }
	                status = MAKEARRAY_STATUS_QUOTE_READ;
	            }
	            else {
	                part.append(rd);
	            }
			}
	        
		}
	    
	    if (part.length() > 0)
	    {
	        tagParts.add(part.toString());
	    }

	    return tagParts;
	}

	public void appendChar(char c) {
		_buffer.append(c);
	}
	public void appendString(String str) {
		_buffer.append(str);
	}

	public StringBuilder mutableBuffer() {
		return _buffer;
	}
	
	public String buffer() {
		return _buffer.toString();
	}
	
	public void clear() {
		_buffer.delete(0, _buffer.length());
		_extractedTag.delete(0,_extractedTag.length());
	}

}
