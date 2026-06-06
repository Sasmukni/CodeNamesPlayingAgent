package entities;

public class Word {
	
	public Word(String name, String label) {
		this.name = name;
		this.label = label;
		isGuessed = false;
	}
	
	public Word(String name, String label, int pos) {
		this.name = name;
		this.label = label;
		this.pos = pos;
		isGuessed = false;
	}
	
	public String name;
	public String label; // Labels are [Black, Red, Blue, Blank]
	public boolean isGuessed;
	public int pos; //Useful for visualization
	
}
