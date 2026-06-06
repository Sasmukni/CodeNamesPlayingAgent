package behaviours;
import jade.content.AgentAction;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ApiCaller;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.OracleAgent;

public class InitializeOracle extends OneShotBehaviour {
	private AID[] gms; //it's only one but still an array
	private int status; // we have three steps: obtain gm AID, send gameId request, recieve gameId answer
    public InitializeOracle(int status){
        super();
        this.status = status;
    }
    @Override
    public void action() {
    	System.out.println(myAgent.getName() + " Initializing Oracle");
    	OracleAgent ag = (OracleAgent) myAgent;
        
        //check if it's a game manager
        MessageTemplate mt = MessageTemplate.MatchConversationId("game-id");
        if(ag.getIsGameMgr()) {
        	status = 2;
        	return;
        }
        if(status == 1) {
	        System.out.println(myAgent.getLocalName()+": Wait answer");
        	ACLMessage reply = myAgent.blockingReceive(mt);
	        System.out.println(myAgent.getLocalName()+": Answer recieved");
	        if(reply!=null) {
	        	ag.setGameId(reply.getContent());
	        	status = 2;
	        	return;
	        }
        }
        if(status > 0) {
        	return;
        }
        
        // If it's not a game manager we need to ask the gameId to the game manager
        //Look for the game manager service in the yellow pages 
        DFAgentDescription dfd = new DFAgentDescription(); 
        ServiceDescription sd = new ServiceDescription(); 
        sd.setType("game-manager"); // type of service 
        sd.setName("codenames"); // name of service
        dfd.addServices(sd); 
        try { 
        	DFAgentDescription[] result = DFService.searchUntilFound(myAgent,myAgent.getDefaultDF(), dfd,new SearchConstraints(),6000);
        	gms = new AID[result.length];
        	for(int i=0; i<result.length; i++) {
        		gms[i] = result[i].getName();
        	}
        }
        catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
        if(gms.length != 0) {
        	//the game service is online -> the game manager has a roomID to give out
        	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		    msg.addReceiver(gms[0]); // get the first game master
		    msg.setContent("oracle");
		    msg.setOntology("game-id");
		    msg.setConversationId("game-id");
		    myAgent.send(msg);
		    status = 1; // we successfully(?) sent a request
        }
        
    }
    public int onEnd(){
        return status;
    }
}