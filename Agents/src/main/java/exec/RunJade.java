package exec;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.sf.extjwnl.JWNLException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import utils.Word2VecUser;
import utils.WordNetSearch;

public abstract class RunJade {
	public static String gameStatus = "offline";
	private static AgentContainer ac = null;
	private static Runtime rt = Runtime.instance();
	private static CountDownLatch simulationDone;
    public static void main(String[] args) throws StaleProxyException, InterruptedException, FileNotFoundException, JWNLException, CloneNotSupportedException{
        //BabelNetConfiguration bnc = BabelNetConfiguration.getInstance();
        //bnc.setConfigurationFile(new File("C:\\Users\\s.capani\\SdaiNlp\\BabelNet\\BabelNet-API-5.3\\config\\babelnet.properties"));
        Word2VecUser.initialize();
        WordNetSearch.initialize();
        
        Profile p = new ProfileImpl();
        p.setParameter(Profile.CONTAINER_NAME, "codenames-sim");
        ac = rt.createMainContainer(p);
        /*
        String agents = "";
        agents += "Judas:agents.OracleAgent(red,game-manager,str:conservative);";
        agents += "John:agents.OracleAgent(blue,str:aggressive);";
        agents += "Paul:agents.PlayerAgent(red,team-coord);";
        agents += "Peter:agents.PlayerAgent(red);";
        agents += "Mary:agents.PlayerAgent(blue);";
        agents += "Suzie:agents.PlayerAgent(blue,team-coord)";
        
        String[] jadeArgs = new String[] {
            "-gui",
            "-agents",
            agents
        };
        Boot.main(jadeArgs);
        */

        while(true) {
        	simulationDone = new CountDownLatch(1);
        	initializeAgents();
        	simulationDone.await();
        	restartPlatform();
        }
        
        
    }
    private static void initializeAgents() throws StaleProxyException{
    	AgentController a1 = ac.createNewAgent("Judas",agents.OracleAgent.class.getName(), new Object[]{"red","game-manager","str:conservative"});
    	AgentController a2 = ac.createNewAgent("John",agents.OracleAgent.class.getName(), new Object[]{"blue","str:aggressive"});
    	AgentController a3 = ac.createNewAgent("Paul",agents.PlayerAgent.class.getName(), new Object[]{"red","team-coord","str:depth"});
    	AgentController a4 = ac.createNewAgent("Peter",agents.PlayerAgent.class.getName(), new Object[]{"red","str:score"});
    	AgentController a5 = ac.createNewAgent("Mary",agents.PlayerAgent.class.getName(), new Object[]{"blue","str:depth"});
    	AgentController a6 = ac.createNewAgent("Suzie",agents.PlayerAgent.class.getName(), new Object[]{"blue","team-coord","str:score"});
    	AgentController a7 = ac.createNewAgent("Mark",agents.PlayerAgent.class.getName(), new Object[]{"red","str:depth-allpos"});
    	AgentController a8 = ac.createNewAgent("Jack",agents.PlayerAgent.class.getName(), new Object[]{"blue","str:depth-allpos"});
    	
    	a1.start();
    	a2.start();
    	a3.start();
    	a4.start();
    	a5.start();
    	a6.start();
    	a7.start();
    	a8.start();
    }
    private static void restartPlatform() throws StaleProxyException {
    	ac.kill();
    	Profile p = new ProfileImpl();
        p.setParameter(Profile.CONTAINER_NAME, "codenames-sim");
        ac = rt.createMainContainer(p);
        //initializeAgents();
    }
    
    public static void simEnded(){
    	simulationDone.countDown();
    }
}

// in folder /d
//build
//javac -cp "./lavoro/JADE-all-4.6.0/jade/lib/*;./EclipseWorkspace/test/src"  ./EclipseWorkspace/test/src/test/TestAgent.java
//execution
//$ java -cp "./lavoro/JADE-all-4.6.0/jade/lib/*;./EclipseWorkspace/test/src" jade.Boot -agents test:test.TestAgent