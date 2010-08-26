package dk.itu.big_red.model.interfaces.pure;

public interface INode extends IParent, IChild {
	public IControl getIControl();
	
	public Iterable<IPort> getIPorts();
	
	public String getName();
}
