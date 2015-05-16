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
		this.put("number", "0");
		this.put("position", "0 0 0 0");
		this.put("color", "YELLOW");
		
	}
	
	public OneNote(String title, String body, String date, String number, String position, String color){
		super();
		this.put("title", title);
		this.put("body", body);
		this.put("date", date);
		this.put("number", number);
		this.put("position", position);
		this.put("color", color);
	
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
	
	public String GetNumber(){
		return this.get("number");
	}
	
	public String GetPosition(){
		return this.get("position");
	}
	
	public String GetColor(){
		return this.get("color");
	}
	
	public void SetTitle(String title){
		this.put("title", title);
	}
	
	public void SetBody(String body){
		this.put("body", body);
	}
	
	public void SetDate(String date){
		this.put("date", date);
	}
	
	public void SetNumber(String number){
		this.put("number", number);
	}

	public void SetPosition(String position){
		this.put("position", position);
	}

	public void SetColor(String color){
		this.put("color", color);
	}
}
