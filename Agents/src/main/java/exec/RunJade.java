package exec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;

import entities.AgentConf;
import entities.CodeNamesConf;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import net.sf.extjwnl.JWNLException;
import utils.Word2VecUser;
import utils.WordNetSearch;

public abstract class RunJade {
	private static AgentContainer ac = null;
	private static Runtime rt = Runtime.instance();
	private static CountDownLatch simulationDone;
	private static CodeNamesConf config;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, JWNLException, CloneNotSupportedException, ControllerException{
		String confPath = "";
		if (args != null) {
			boolean takeNext=false;
			for (Object arg : args) {
				if(takeNext){
					confPath=arg.toString();
				}
				else if(arg.toString().equals("--conf"))
					takeNext=true;	
			}
		}
		manageConfiguration(confPath);
        Word2VecUser.initialize();
        WordNetSearch.initialize();
        
        Profile p = new ProfileImpl();
        p.setParameter(Profile.CONTAINER_NAME, "codenames-sim");
        ac = rt.createMainContainer(p);

        while(true) {
        	simulationDone = new CountDownLatch(1);
        	initializeAgents();
        	simulationDone.await();
        	restartPlatform();
        }
        
        
    }
    private static void initializeAgents() throws ControllerException{
    	//initialize agents from configuration
		for(AgentConf agent: config.agents){
			Object[] attrs = new Object[agent.attributes.size()];
			int i=0;
			for(String attr: agent.attributes){
				attrs[i++]= attr;
			}
			AgentController at = ac.createNewAgent(
				agent.name,
				agent.role.toLowerCase().equals("player")?  agents.PlayerAgent.class.getName() : agents.OracleAgent.class.getName(),
				attrs);
			at.start();
		}
    }

	private static void manageConfiguration(String confPath){
		File configFile = new File(confPath);
		if(!configFile.exists()){
			System.out.println("Initialization: custom agent configuration not found, using default configuration instead");
			String rootPath = System.getProperty("user.dir");
			configFile = new File(rootPath +"/Agents/target/classes/defaultConf.json"); 
		}

		config = readConf(configFile);
		if(config==null || !config.validate()){
			System.out.println("Initialization: custom agent was not valid, using default configuration instead");
			String rootPath = System.getProperty("user.dir");
			configFile = new File(rootPath +"/Agents/target/classes/defaultConf.json"); 
			config = readConf(configFile);
		}
	}

	private static CodeNamesConf readConf(File configFile){
		CodeNamesConf res = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
			res = new  Gson().fromJson(reader, CodeNamesConf.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

    private static void restartPlatform() throws StaleProxyException {
    	ac.kill();
        rt.shutDown();
        rt = Runtime.instance();
    	Profile p = new ProfileImpl();
        p.setParameter(Profile.CONTAINER_NAME, "codenames-sim");
        ac = rt.createMainContainer(p);
    }
    
    public static void simEnded(){
    	simulationDone.countDown();
    }
}