package entities;
import java.util.Collection;

public class PossibleClue{
	public PossibleClue(String word, Double score, Collection<String> intendedTargets){
		this.word = word;
		this.score = score;
		this.targets = intendedTargets; 
	}
	private String word;
	private Double score;
	private Collection<String> targets;
	public  String toString(){
		return word + " with a score of: "+score + " with the intended targets" + targets;
	} 
	public Double getScore() {
		return score;
	}
	public String getWord() {
		return word;
	}
	
}