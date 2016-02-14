package com.Pau.ImapNotes2.Miscs;

public class Sticky {

	private static String text;
	private static String position;
	private static String color;

	public Sticky() {
		Sticky.text = "";
		Sticky.position = "0 0 0 0";
		Sticky.color = "YELLOW";
	}
	
	public Sticky(String text, String position, String color) {
		Sticky.text = text;
		Sticky.position = position;
		Sticky.color = color;
	}
	
	public String GetPosition(){
		return Sticky.position;
	}
	
	public String GetText(){
		return Sticky.text;
	}
		
	public String GetColor(){
		return Sticky.color;
	}
	
	public void SetText(String text){
		Sticky.text = text;
	}
	
	public void SetPosition(String position){
		Sticky.position = position;
	}

	public void SetColor(String color){
		Sticky.color = color;
	}
}
