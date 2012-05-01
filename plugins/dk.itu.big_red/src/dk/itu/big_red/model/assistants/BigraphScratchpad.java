package dk.itu.big_red.model.assistants;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.names.Namespace;

/**
 * The BigraphScratchpad is a wrapper around various kinds of {@link
 * Layoutable} which maps objects to copies of themselves, allowing
 * the copies to be modified without affecting the original.
 * @author alec
 *
 */
public class BigraphScratchpad extends PropertyScratchpad {
	private Bigraph bigraph;
	public BigraphScratchpad(Bigraph bigraph) {
		this.bigraph = bigraph;
	}
	
	/**
	 * Clears everything in this {@link BigraphScratchpad}.
	 * @return <code>this</code>, for convenience
	 */
	@Override
	public BigraphScratchpad clear() {
		super.clear();
		return this;
	}
	
	public Bigraph getBigraph() {
		return bigraph;
	}
	
	public void removeChildFor(Container a, Layoutable b) {
		this.<Layoutable>getModifiableList(a, Container.PROPERTY_CHILD).remove(b);
		setProperty(b, Layoutable.PROPERTY_PARENT, null);
		setNameFor(b, null);
	}
	
	public void addChildFor(Container a, Layoutable b, String name) {
		this.<Layoutable>getModifiableList(a, Container.PROPERTY_CHILD).add(b);
		setProperty(b, Layoutable.PROPERTY_PARENT, a);
		setNameFor(b, name);
	}
	
	public void setNameFor(Layoutable a, String b) {
		String currentName = a.getName(this);
		Namespace<Layoutable> ns = bigraph.getNamespace(Bigraph.getNSI(a));
		if (currentName != null)
			ns.remove(this, currentName);
		
		setProperty(a, Layoutable.PROPERTY_NAME, b);
		
		ns.put(this, b, a);
	}
}
