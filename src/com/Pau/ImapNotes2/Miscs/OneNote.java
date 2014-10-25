package com.Pau.ImapNotes2.Miscs;

import java.util.HashMap;

public class OneNote extends HashMap<String,String>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OneNote(){
		super();
		this.put("title", "No Title");
		this.put("body", "No Body");
		this.put("date", "No Date");
		
	}
	
	public OneNote(String title, String body, String date){
		super();
		this.put("title", title);
		this.put("body", body);
		this.put("date", date);
	
	}
	
	public String GetTitle(){
		return this.get("title");
		
	}
	
	public String GetBody(){
		return this.get("body");
		
	}
	
	public String GetDate(){
		return this.get("date");
		
	}
	
}
