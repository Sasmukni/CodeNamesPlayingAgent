package utils;

public abstract class CodeNamesUtils {
	public static String getOtherTeam(String myTeam) {
		return myTeam.equals("red")? "blue" : "red"; // if we are red the other team has to be blue, otherwise
	}
}