package dk.itu.big_red.model;

public class InnerName extends Point {
	@Override
	public InnerName clone() {
		System.out.println("! Clone?");
		return new InnerName();
	}
	
	public boolean canContain(Thing child) {
		return false;
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
}
