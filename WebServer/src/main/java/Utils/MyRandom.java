package Utils;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public final class MyRandom {
	public static Set<Integer> RandomIntegerSet (int N, int max){
		Set<Integer> uniqueNumbers = new HashSet<>();

		Random random = new Random();
        while (uniqueNumbers.size() < N) {
            int num = random.nextInt(max);// + 1; 
            uniqueNumbers.add(num);
        }
        return uniqueNumbers;
	}
	
	public static String CoinFlip () {
		Random random = new Random();
		return random.nextInt(2) == 0 ? "blue" : "red";
	}
}
