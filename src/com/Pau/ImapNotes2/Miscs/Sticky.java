package com.Pau.ImapNotes2.Miscs;

public class Sticky {

	private static String text;
	private static String position;
	private static String color;

	public Sticky() {
		this.text = "";
		this.position = "0 0 0 0";
		this.color = "YELLOW";
	}
	
	public Sticky(String text, String position, String color) {
		this.text = text;
		this.position = position;
		this.color = color;
	}
	
	public String GetPosition(){
		return this.position;
	}
	
	public String GetText(){
		return this.text;
	}
		
	public String GetColor(){
		return this.color;
	}
	
	public void SetText(String text){
		this.text = text;
	}
	
	public void SetPosition(String position){
		this.position = position;
	}

	public void SetColor(String color){
		this.color = color;
	}
}
