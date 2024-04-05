package com.gopalapriyadasa.textabase_engine;

public class FlatFileContentItem {
	public String getA() {
		return A;
	}
	public void setA(String a) {
		A = a;
	}
	public String getB() {
		return B;
	}
	public void setB(String b) {
		B = b;
	}
	public String getC() {
		return C;
	}
	public void setC(String c) {
		C = c;
	}
	public String getD() {
		return D;
	}
	public void setD(String d) {
		D = d;
	}
	private String A;
	private String B;
	private String C;
	private String D;
	private boolean etlStarted;
	

	public boolean isEtlStarted() {
		return etlStarted;
	}
	public void setEtlStarted(boolean etlStarted) {
		this.etlStarted = etlStarted;
	}
	
	
}
