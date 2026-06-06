package behaviours;
import jade.core.AID;
//import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.PlayerAgent;

public class InitializePlayer extends OneShotBehaviour {
	private AID[] gms; //it's only one but still an array
	private int status; // we have three steps: obtain gm AID, send gameId request, receive gameId answer
    public InitializePlayer(int status){
        super();
        this.status = status;
    }
    @Override
    public void action() {
    	System.out.println(myAgent.getName() + " Initializing Player");
    	//First thing First we need to define the oracle service for the Agent team
    	PlayerAgent ag = (PlayerAgent) myAgent;
        
        
        //check if it's a game manager
        MessageTemplate mt = MessageTemplate.MatchConversationId("game-id");
        if(ag.isGameMngr()) {
        	status = 2;
        	return;
        }
        if(status == 1) {
	        System.out.println(myAgent.getLocalName()+": wait game id answer");
        	ACLMessage reply = myAgent.blockingReceive(mt);
	        System.out.println(myAgent.getLocalName()+": game id answer recieved");
	        if(reply!=null) {
	        	ag.setGameId(reply.getContent());
	        	status = 2;
	        	System.out.println(myAgent.getLocalName()+": Initialization finalized");
	        	return;
	        }
        }
        if(status > 0) {
        	//myAgent.blockingReceive();// what do we want here? I forgor
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
        	//DFAgentDescription[] result = DFService.search(myAgent, dfd);
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
		    msg.setContent(ag.getTeam());
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