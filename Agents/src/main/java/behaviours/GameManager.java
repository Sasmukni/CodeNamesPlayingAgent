package behaviours;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

import agents.CodeNamesAgent;
import entities.ChangeDto;
import entities.Message;
import exec.RunJade;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ApiCaller;

public class GameManager extends Behaviour {
	
	private int redPlayers = 0;
	private int bluePlayers = 0;
	private List<AID> agents = new ArrayList<>();
    public GameManager(){
        super();
    }
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.or(
        		MessageTemplate.MatchOntology("game-id"),
        		MessageTemplate.or(
        				MessageTemplate.MatchOntology("team-change"),
                		MessageTemplate.or(
                				MessageTemplate.MatchOntology("player-count"),
                				MessageTemplate.or(
                        				MessageTemplate.MatchOntology("update-chat"),
                        				MessageTemplate.MatchContent("game-has-ended")
                    				)
            				)
				)
			);
      
    	ACLMessage req = myAgent.receive(mt);
    	if(req != null) {
        	if(req.getOntology().equals("game-id")) {
				gameIdResponse(req);
	        }else if(req.getOntology().equals("team-change")) {
				teamChangeResponse(req); 
	        }else if(req.getOntology().equals("player-count")) {
	        	playerCountResponse(req);
	        }else if("game-has-ended".equals(req.getContent())) {
				closeGame();
	        }else if(req.getOntology().equals("update-chat")) {
	        	updateChat(req);
	        }
    	}else {
    		block();
    	}
    }

	@Override
	public boolean done() {
		return false;
	}

	private void gameIdResponse(ACLMessage req){
		CodeNamesAgent cna = (CodeNamesAgent) myAgent;
		String gameId = cna.getGameId();
		ACLMessage reply = req.createReply(ACLMessage.INFORM);	
		//We need to count the players that are registering for which team
		if(req.getContent().equals("red")) {
			redPlayers++;
		}else if(req.getContent().equals("blue")) {
			bluePlayers++;
		}
		//we add the AID of the requesting agents to our AID list (useful for broadcasting messages)
		agents.add(req.getSender());
		reply.setContent(gameId);
		myAgent.send(reply);
	}

	private void teamChangeResponse(ACLMessage req){
		CodeNamesAgent cna = (CodeNamesAgent) myAgent;
		String gameId = cna.getGameId();
		ACLMessage reply = req.createReply(ACLMessage.INFORM);	
		System.out.println(myAgent.getLocalName() + ": I recieved a request for a team-change!");
		ApiCaller ac = new ApiCaller();
		ChangeDto cd = new ChangeDto();
		cd.NewTeam = req.getContent();
		cd.RoomId = gameId;
		String res = ac.callPutApi("http://localhost:8080/api/change-current-team",cd); 
		reply.setContent(res);
		//Coincidentally with the team change we also want to update the gui
		cna.getGg().refreshBoard(ac.getGameStatus(gameId));
		myAgent.send(reply);
	}
	private void playerCountResponse(ACLMessage req){
		ACLMessage reply = req.createReply(ACLMessage.INFORM);	
		if(req.getContent().equals("red")) {
			reply.setContent(((Integer)redPlayers).toString());
		}else {
			reply.setContent(((Integer)bluePlayers).toString());
		}
		myAgent.send(reply);
	}
	private void closeGame(){
		((CodeNamesAgent) myAgent).getGg().setDefaultCloseOperation(HIDE_ON_CLOSE); // if we close an old window we don't want to close the program
		ACLMessage closedMsg = new ACLMessage(ACLMessage.INFORM);
		closedMsg.setContent("game-closed");
		for(AID a: agents)
			closedMsg.addReceiver(a);
		myAgent.send(closedMsg);
		myAgent.doDelete();
		RunJade.simEnded();
	}
	private void updateChat(ACLMessage req){
		CodeNamesAgent cna = (CodeNamesAgent) myAgent;
		String[] mes = req.getContent().split("#");
		if(mes.length > 2) {
			Message m = new Message(req.getSender().getLocalName(),mes[0], mes[1],mes[2]);
			cna.getGg().addMessage(m);
		}
	}
}