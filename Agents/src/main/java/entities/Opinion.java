package entities;

import jade.core.AID;

public class Opinion {
	private Double score;
	private String name;
	private AID holder;
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
	public AID getHolder() {
		return holder;
	}
	public void setHolder(AID holder) {
		this.holder = holder;
	}
	
	
}
