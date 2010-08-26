package dk.itu.big_red.model.interfaces;

public interface IControl {
	public Iterable<IPort> getIPorts();
	
	public String getName();
}
