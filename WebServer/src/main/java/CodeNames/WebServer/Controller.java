package CodeNames.WebServer;

import org.springframework.web.bind.annotation.*;

import Utils.CreateGame;
import Utils.GameStatusIO;
import entities.*;

@RestController
@RequestMapping("/api")
public class Controller {
    
    @GetMapping("/create-room")
    public String createRoom() {
    	String res = CreateGame.CreateGameMethod();
    	return res;
    }
    @GetMapping("/get-room")
    public GameStatus getRoomData(@RequestParam String Id) {
    	GameStatus res = GameStatusIO.getGameStatus(Id);
    	return res;
    }
    @PutMapping("/guess")
    public String guess(@RequestBody GuessDto ng) {
    	GameStatus gs = GameStatusIO.getGameStatus(ng.RoomId);
    	String res = "Not found";
    	for(Word w : gs.words) {
    	//gs.words.forEach((w) -> {
    		if(w.name.equals(ng.NameGuess) && !w.isGuessed) {
    			w.isGuessed = true;
    			res = w.label;
    			gs.guessHistory.add(w.name);
    		}
		}
    	//Update file
    	GameStatusIO.saveGameStatus(gs);
    	return res;
    }
    
    @PutMapping("/change-current-team")
    public String changeCurrentTeam(@RequestBody ChangeDto cg){
    	GameStatus gs = GameStatusIO.getGameStatus(cg.RoomId);
    	String res = "Not found";
    	gs.currentTeam = cg.NewTeam;
    	//Update file
    	GameStatusIO.saveGameStatus(gs);
    	return "ok"; // It should break if the room does not exist
    }
    
}
