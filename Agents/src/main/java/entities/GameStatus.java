package entities;

import java.util.ArrayList;

public class GameStatus {
	public GameStatus(String id) {
		Id = id;
	}
	public String Id;
	public ArrayList<Word> words = new ArrayList<Word>();
	public String startingTeam;
	public String currentTeam;
	
	public boolean isWordInBoard(String name) {
		//returns true if the name is currently visible in the board (not guessed)
		for(Word w:words) {
			if(name.equals(w.name) && !w.isGuessed)
				return true;
		}
		return false;
	}
}
