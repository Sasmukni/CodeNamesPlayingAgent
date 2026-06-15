package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import utils.ApiCaller;
import utils.GameGUI;

import javax.swing.text.BadLocationException;

import agents.CodeNamesAgent;

public class CreateRoom extends OneShotBehaviour {
	private String roomId = null; 
    public CreateRoom(String room){
        super();
        this.roomId = room;
    }
    @Override
    public void action() {
		ApiCaller ac = new ApiCaller();
    	if(roomId == null) {
	    	System.out.println(myAgent.getName() + " From Create Room: I going to open a new GameRoom");
	        roomId = ac.callGetApi("http://localhost:8080/api/create-room"); //create room api
	        System.out.println(myAgent.getName() + " Room: " + roomId + " created"); 
        }
		GameGUI gg;
		try {
			gg = new GameGUI(ac.getGameStatus(roomId));
			gg.setVisible(true);
			CodeNamesAgent cna = (CodeNamesAgent) myAgent;
			cna.setGg(gg);
			cna.setGameId(roomId);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    }
}