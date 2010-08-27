package dk.itu.big_red.model.interfaces;

public interface INode extends IParent, IChild {
	public IControl getIControl();
	
	public Iterable<? extends IPort> getIPorts();
	
	public String getName();
}
