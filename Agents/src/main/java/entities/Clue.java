package entities;

//import java.util.ArrayList;

public class Clue {
	public Clue(String stringedClue){
		//this constructor is useful when we have a clue of the shape "N ClueWord"
		String[] temp = stringedClue.split(" "); //for now we don't manage clueword that have a space in it
		// we assume it's correct, I am too lazy to manage exceptions
		N = Integer.parseInt(temp[0]);
		Word = temp[1];
	}
	
	public Clue(int n, String word) {
		N = n;
		Word = word;
	}
	public int N;
	public String Word;
	
	public String toString() {
		return N + " " + Word;
	}
	
}
