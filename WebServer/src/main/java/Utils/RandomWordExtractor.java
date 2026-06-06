package Utils;

import java.util.ArrayList;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.springframework.util.ResourceUtils;
import java.io.File;

public final class RandomWordExtractor {
	//private static String
	
	private static ArrayList<String> ReadDictionary(){
		ArrayList<String> allWords = new ArrayList<String>();
		try {
			File file = ResourceUtils.getFile("classpath:static/ListCodenameWords.txt");
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                //System.out.println(line);
	            	allWords.add(line);
	            }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (IOException e) {
            e.printStackTrace();
		}
		return allWords;
	}
	
	public static ArrayList<String> NRandomWords(int n){
		ArrayList<String> res = new ArrayList<String>();
        ArrayList<String> allWords = ReadDictionary();
		
		//Random random = new Random();
		Set<Integer> uniqueNumbers = MyRandom.RandomIntegerSet(n, allWords.size());// new HashSet<>();
        uniqueNumbers.forEach((i) -> res.add(allWords.get(i)));
        
		return res;
	}
}
