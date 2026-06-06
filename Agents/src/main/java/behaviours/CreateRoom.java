package behaviours;
//import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import utils.ApiCaller;
import utils.GameGUI;

import javax.swing.text.BadLocationException;

//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.OracleAgent;
import agents.PlayerAgent;

public class CreateRoom extends OneShotBehaviour {
	private String roomId = null; 
    public CreateRoom(String room){
        super();
        this.roomId = room;
    }
    @Override
    public void action() {
    	if(roomId == null) {
	    	System.out.println(myAgent.getName() + " From Create Room: I going to open a new GameRoom");
	        
	        // recieve message, if message if of type "roomcode" save it but don't change status
	        // for now we use a pre existing room for testing
	        //Agent agent = myAgent;
	        ApiCaller ac = new ApiCaller();
	        String gameid = ac.callGetApi("http://localhost:8080/api/create-room"); //create room api
	        // any agent could be a game manager! It's a parameter passed in the agent creation
			GameGUI gg;
			try {
				gg = new GameGUI(ac.getGameStatus(gameid));
				gg.setVisible(true);
				if(myAgent instanceof PlayerAgent) {
					//if we are the game manager we also want to manage the GUI
		        	((PlayerAgent) myAgent).setGg(gg);
		        	((PlayerAgent) myAgent).setGameId(gameid);
	        	}
		        if(myAgent instanceof OracleAgent) {
		        	((OracleAgent) myAgent).setGg(gg);
		        	((OracleAgent) myAgent).setGameId(gameid);
	        	}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        System.out.println(myAgent.getName() + " Room: " + gameid + " created"); 
        }else {
        	String gameid = roomId; //create room api
	        // any agent could be a game manager! It's a parameter passed in the agent creation
	        ApiCaller ac = new ApiCaller();
			GameGUI gg;
			try {
				gg = new GameGUI(ac.getGameStatus(gameid));
				gg.setVisible(true);
		        if(myAgent instanceof PlayerAgent) {
					//if we are the game manager we also want to manage the GUI
					
		        	((PlayerAgent) myAgent).setGg(gg);
		        	((PlayerAgent) myAgent).setGameId(gameid);
	        	}
		        if(myAgent instanceof OracleAgent) {
					((OracleAgent) myAgent).setGg(gg);
		        	((OracleAgent) myAgent).setGameId(gameid);
	        	}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
        }   
    }
}