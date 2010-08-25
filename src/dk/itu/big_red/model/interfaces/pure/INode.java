package dk.itu.big_red.model.interfaces.pure;

public interface INode extends IPlace {
	public IPlace getIPlace();
	public IControl getIControl();
	
	public Iterable<IPort> getIPorts();
	
	public String getName();
}
