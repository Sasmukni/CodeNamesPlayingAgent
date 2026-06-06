package behaviours;
import jade.content.AgentAction;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;
import utils.ApiCaller;

import java.util.ArrayList;
import java.util.List;

//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.OracleAgent;
import agents.PlayerAgent;
import entities.ChangeDto;
import entities.Message;
import exec.*;

public class GameManager extends Behaviour {
	
	private int redPlayers = 0;
	private int bluePlayers = 0;
	private List<AID> agents = new ArrayList<>();  
	
    public GameManager(){
        super();
    }
    @Override
    public void action() {
        // any agent could be a game manager! It's a parameter passed in the agent creation
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
	    	String gameId = "";
	        if(myAgent instanceof PlayerAgent) 
	        	gameId = ((PlayerAgent) myAgent).getGameId();
	        if(myAgent instanceof OracleAgent) 
	        	gameId = ((OracleAgent) myAgent).getGameId();
	        ACLMessage reply = req.createReply(ACLMessage.INFORM);
        	if(req.getOntology().equals("game-id")) {
        		//We need to count the players that are registering for which team
        		if(req.getContent().equals("red")) {
        			redPlayers++;
        		}else if(req.getContent().equals("blue")) {
        			bluePlayers++;
        		}
        		//we add the AID of the requesting agents to our AID list (useful for broadcasting messages)
        		agents.add(req.getSender());
	        	reply.setContent(gameId);
	        }else if(req.getOntology().equals("team-change")) {
	        	//call API
	        	System.out.println(myAgent.getLocalName() + ": I recieved a request for a team-change!");
	        	ApiCaller ac = new ApiCaller();
	        	ChangeDto cd = new ChangeDto();
	        	cd.NewTeam = req.getContent();
	        	cd.RoomId = gameId;
	            String res = ac.callPutApi("http://localhost:8080/api/change-current-team",cd); 
	            reply.setContent(res);
	            //Coincidentally with the team change we also want to update the 
	            if(myAgent instanceof PlayerAgent) 
	            	((PlayerAgent) myAgent).getGg().refreshBoard(ac.getGameStatus(gameId));
	            if(myAgent instanceof OracleAgent) 
	            	((OracleAgent) myAgent).getGg().refreshBoard(ac.getGameStatus(gameId));
	            
	        }else if(req.getOntology().equals("player-count")) {
	        	if(req.getContent().equals("red")) {
	        		reply.setContent(((Integer)redPlayers).toString());
	        	}else {
	        		reply.setContent(((Integer)bluePlayers).toString());
	        	}
	        }else if("game-has-ended".equals(req.getContent())) {
	        	ACLMessage closedMsg = new ACLMessage(ACLMessage.INFORM);
	        	closedMsg.setContent("game-closed");
	        	for(AID a: agents)
	        		closedMsg.addReceiver(a);
	        	myAgent.send(closedMsg);
	        	myAgent.doDelete();
        		RunJade.simEnded();
	        	
	        	// at the end of this preparation we will terminate our activity
	        	return; //we wont be sending any reply there
	        }else if(req.getOntology().equals("update-chat")) {
	        	String[] mes = req.getContent().split("#");
	        	if(mes.length > 2) {
		        	Message m = new Message(req.getSender().getLocalName(),mes[0], mes[1],mes[2]);
		            if(myAgent instanceof PlayerAgent) 
		            	((PlayerAgent) myAgent).getGg().addMessage(m);
		            if(myAgent instanceof OracleAgent) 
		            	((OracleAgent) myAgent).getGg().addMessage(m);
	        	}
	        }
	        myAgent.send(reply);
    	}else {
    		block();
    	}
        
    }
	@Override
	public boolean done() {
		//we could be done at the end of the game, but for single game simulations we don't care
		// TODO Auto-generated method stub
		return false;
	}
    
    /* 
    @Override
    public boolean done() {
        if(counter>=10)
            return true;
        return false;
    }
    */
}