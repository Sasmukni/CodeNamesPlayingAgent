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
import utils.ApiCaller;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.OracleAgent;
import entities.Clue;

public class ManageClueRequests extends Behaviour {
	private int newestClue = 0;
    public ManageClueRequests(){
        super();
    }
    @Override
    public void action() {
        // We don't want to receive requests before we have a clue ready!!
    	OracleAgent ag = (OracleAgent) myAgent;
        if(ag.getClue() == null)
        	return; //If we don't have a clue we don't want to read the given type 
        
    	MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-clue"),MessageTemplate.MatchContent(newestClue + ""));
        //System.out.println(myAgent.getLocalName() + ": Waiting for Clue Requests");
    	ACLMessage req = myAgent.receive(mt);
    	if(req != null) {
    		int clueNumber = Integer.parseInt(req.getContent());
    		Clue c = ag.getGameClues().get(clueNumber);
    		
    		if(c==null) {
    			System.out.println(myAgent.getLocalName() + ": I don't have an answer for this request yet");
    		//	myAgent.postMessage(req); // if we don't have the answer yet we put the message back in queue
    		//	block(); //we will call restart on this behavior 
    			// not sure if we should block here, but if we don't do it it's a (very busy wait)
    			return;
    		}
	        ACLMessage reply = req.createReply(ACLMessage.INFORM);
	        // we may have to keep track of all the clues we had given (so if a player asks for the third clue we can respond accordingly)
	        //Clue c = ag.getClue();
        	reply.setContent(c.toString()); // if we have a clue, respond with it
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
	public void setNewestClue(int nclue) {
		this.newestClue = nclue;
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