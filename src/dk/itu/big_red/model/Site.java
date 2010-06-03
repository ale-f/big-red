package dk.itu.big_red.model;

public class Site extends Thing {
	public Thing clone() throws CloneNotSupportedException {
		return new Site()._overwrite(this);
	}
	
	private int number;
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
}
