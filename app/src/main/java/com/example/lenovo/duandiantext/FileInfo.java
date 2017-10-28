package com.example.lenovo.duandiantext;

import java.io.Serializable;

public class FileInfo implements Serializable{
	private String url;
	private int length;
	private int start;
	private int now;
	public FileInfo(String url,int length,int start,int now){
		this.url = url;
		this.length = length;
		this.start = start;
		this.now = now;
	}
	public FileInfo(){
		
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getNow() {
		return now;
	}
	public void setNoW(int noe) {
		this.now = now;
	}
	
}

