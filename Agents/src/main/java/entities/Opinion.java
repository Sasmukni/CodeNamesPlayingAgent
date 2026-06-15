package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jade.core.AID;

public class Opinion {
	private Double score;
	private String name;
	private List<AID> holders = new ArrayList<AID>();
	public Opinion() {
		
	}
	public Opinion(String op) {
		String[] values = op.split("\\*");
		this.setName(values[0]);
		this.setScore(Double.parseDouble(values[1]));
	}
	public Opinion(String name, Double score) {
		this.setName(name);
		this.setScore(score);
	}
	
	@Override
	public String toString() {
		return name + "*" + score;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==null || o.getClass() != getClass())
			return false;
		//two opinions are equals if they have the same name, even with a different score
		return this.getName().equals(((Opinion) o).getName());
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public List<AID> getHolders() {
		return holders;
	}
	public void setHolders(List<AID> holders) {
		this.holders = holders;
	}
	
	public void addHolder(AID holder){
		this.holders.add(holder);
	}

	public Double getTrustScore(Map<AID,Double> trustMap){
		//If an opinion is held by more than one player we take the best Trust score (the minimum between those)
		Double min= Double.MAX_VALUE;
		for(AID aid : holders){
			if(min > trustMap.get(aid)){
				min = trustMap.get(aid);
			}
		}
		return score + min;
	}
	
}
