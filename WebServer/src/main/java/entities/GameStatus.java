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
	public ArrayList<String> guessHistory = new ArrayList<String>();
}
