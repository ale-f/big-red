package dk.itu.big_red.model.interfaces;

public interface IControl {
	public Iterable<? extends IPort> getIPorts();
	
	public String getName();
}
