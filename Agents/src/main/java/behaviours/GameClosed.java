package behaviours;

import jade.core.behaviours.OneShotBehaviour;

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
