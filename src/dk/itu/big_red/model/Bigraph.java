package dk.itu.big_red.model;

import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.ILayoutable;

public class Bigraph extends Thing {
	protected Signature signature = new Signature();
	protected NamespaceManager namespaceManager = new NamespaceManager();
	protected ArrayList<Edge> edges = new ArrayList<Edge>();
	
	public Thing clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	
	/**
	 * Gets the {@link NamespaceManager} for this bigraph.
	 * @return a NamespaceManager
	 */
	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class);
	}
	
	public Bigraph getBigraph() {
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public void setParent(ILayoutable parent) {
		/* do nothing */
	}
	
	public ILayoutable getParent() {
		return null;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle();
	}
}
