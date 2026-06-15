package Utils;

import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.springframework.util.ResourceUtils;

import java.io.File;
//import org.springframework.util.ResourceUtils;

import entities.GameStatus;

public final class GameStatusIO {
	public static boolean saveGameStatus(GameStatus gs) {
		String res = new Gson().toJson(gs);
		try {
			String rootPath = System.getProperty("user.dir");
			//String classPath = System.getProperty("java.class.path");
			File fileDynamic = new File(rootPath +"/WebServer/target/classes/data/"+gs.Id+".json"); //dynamic
			if(!fileDynamic.exists())
				fileDynamic.createNewFile();
			
	        try (FileOutputStream fos = new FileOutputStream(fileDynamic)) {
	            // Write initial content if needed
	            fos.write(res.getBytes());
	            System.out.println("File created and written.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
			File file =new File(rootPath + "/WebServer/src/main/resources/data/" + gs.Id + ".json"); //persistent
			if(!file.exists())
				file.createNewFile();
			
	        try (FileOutputStream fos = new FileOutputStream(file)) {
	            // Write initial content if needed
	            fos.write(res.getBytes());
	            System.out.println("File created and written.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
        }catch(IOException e) {
        	e.printStackTrace();
            return false;
        }
		
		return true;
	}
	
	public static GameStatus getGameStatus(String Id) {
		GameStatus gs = new GameStatus(Id);
		//String jsonObj = "";
		try {
			File file = ResourceUtils.getFile("classpath:data/"+Id+".json");
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	            /*String line;
	            while ((line = reader.readLine()) != null) {
	                //System.out.println(line);
	            	//allWords.add(line);
	            	jsonObj +=line;
	            }*/
	            gs = new  Gson().fromJson(reader, GameStatus.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (IOException e) {
            e.printStackTrace();
		}
		return gs;
	}
}
