package behaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GameClosed extends OneShotBehaviour {
    public GameClosed(){
        super();
    }
    @Override
    public void action() {
    	System.out.println(myAgent.getLocalName() + ": For me the game is closed");
        myAgent.doDelete();	
    }
}
