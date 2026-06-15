package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import entities.GameStatus;

public final class GameStatusIO {
	public static boolean saveGameStatus(GameStatus gs) {
		String res = new Gson().toJson(gs);

		File file = new File("games/"+gs.Id+".json");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException ex) {
				System.getLogger(GameStatusIO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
			}
		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			// Write initial content if needed
			fos.write(res.getBytes());
			System.out.println("File created and written.");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		/* 
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
		*/
	}
	
	public static GameStatus getGameStatus(String Id) {
		GameStatus gs = new GameStatus(Id);
		//String jsonObj = "";
		//ClassPathResource resource = new ClassPathResource("data/"+Id+".json");
		//File file = ResourceUtils.getFile("classpath:data/"+Id+".json");
		File file = new File("games/"+gs.Id+".json");
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
		return gs;
	}
}
