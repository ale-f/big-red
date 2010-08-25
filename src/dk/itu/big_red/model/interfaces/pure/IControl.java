package dk.itu.big_red.model.interfaces.pure;

public interface IControl {
	public Iterable<IPort> getIPorts();
	
	public String getName();
}
