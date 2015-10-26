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
		this.put("date", "No Date");
		this.put("uid", "0");
		
	}
	
	public OneNote(String title, String date, String uid){
		super();
		this.put("title", title);
		this.put("date", date);
		this.put("uid", uid);
	
	}
	
	public String GetTitle(){
		return this.get("title");
	}
	
	public String GetDate(){
		return this.get("date");
	}
	
	public String GetUid(){
		return this.get("uid");
	}
	
	public void SetTitle(String title){
		this.put("title", title);
	}
	
	public void SetDate(String date){
		this.put("date", date);
	}
	
	public void SetUid(String uid){
		this.put("uid", uid);
	}
	
	   @Override
	   public String toString() {
	        return ("Title:"+this.GetTitle()+
	                    " Date: "+ this.GetDate() +
	                    " Uid: "+ this.GetUid());
	   }
}
