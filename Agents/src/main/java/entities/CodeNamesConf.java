package entities;

import java.util.List;
import java.util.ArrayList;

public class CodeNamesConf {
    public List<AgentConf> agents;

    public boolean validate(){
        //it's mandatory to respect the following conditions
        // -at least an oracle and a player for each team
        // -one and only one game manager
        // -one and only one team coordinator, of role player, for each team
        // -all the agents have a different name
        boolean isOk = true;
        List<String> agentNames= new ArrayList<>();
        int gameManagerCount=0;
        int redOracle = 0;
        int blueOracle = 0;
        int redPlayers = 0;
        int bluePlayers = 0;
        int blueTeamCoord = 0;
        int redTeamCoord = 0;   
        for(AgentConf ac: agents){
            if(agentNames.contains(ac.name)) {
                System.err.println("There are too many agents named: " + ac.name);
                isOk = false;
            }
            if(ac.attributes.contains("game-manager"))
                gameManagerCount++;
            if(ac.attributes.contains("red")){
                if(ac.role.toLowerCase().equals("player")){
                    redPlayers++;
                    if(ac.attributes.contains("team-coord"))
                        redTeamCoord++;
                }else{
                    redOracle++;
                }
            }else if(ac.attributes.contains("blue")){
                if(ac.role.toLowerCase().equals("player")){
                    bluePlayers++;
                    if(ac.attributes.contains("team-coord"))
                        blueTeamCoord++;
                }else{
                    blueOracle++;
                }
            }
        }
        if(gameManagerCount==0 || gameManagerCount >= 2){
            System.err.println("There has to be one and only one game manager!");
            isOk=false;
        }
        if(redOracle==0 || redOracle >= 2){
            System.err.println("There has to be one and only one oracle for the red team!");
            isOk=false;
        }
        if(blueOracle==0 || blueOracle >= 2){
            System.err.println("There has to be one and only one oracle for the blue team!");
            isOk=false;
        }
        if(redPlayers==0){
            System.err.println("There has to be at least one player for the red team!");
            isOk=false;
        }
        if(bluePlayers==0){
            System.err.println("There has to be at least one player for the blue team!");
            isOk=false;
        }
        if(redTeamCoord==0 || redTeamCoord >= 2){
            System.err.println("There has to be one and only one team coordinator with the role of player for the red team!");
            isOk=false;
        }
        if(blueTeamCoord==0 || blueTeamCoord >= 2){
            System.err.println("There has to be one and only one team coordinator with the role of player for the blue team!");
            isOk=false;
        }
        return isOk;
    }
}
