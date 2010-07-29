package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.ResourceWrapper;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class Bigraph extends Thing {
	protected ResourceWrapper<Signature> signature =
		new ResourceWrapper<Signature>();
	protected NamespaceManager namespaceManager = new NamespaceManager();
	
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
	
	public void setSignature(IFile file, Signature signature) {
		if (file != null && signature != null) {
			this.signature.setResource(file);
			this.signature.setModel(signature);
		}
	}
	
	public Signature getSignature() {
		return signature.getModel();
	}
	
	public IFile getSignatureFile() {
		return signature.getResource();
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
	
	private ArrayList<ILayoutable> sortedChildren =
		new ArrayList<ILayoutable>();
	
	@Override
	public void addChild(ILayoutable child) {
		sortedChildren.clear();
		super.addChild(child);
	}
	
	@Override
	public void removeChild(ILayoutable child) {
		sortedChildren.clear();
		super.removeChild(child);
	}
	
	@Override
	public List<ILayoutable> getChildren() {
		if (sortedChildren.size() == 0) {
			for (ILayoutable i : children) {
				if (i.getClass() != Edge.class)
					sortedChildren.add(i);
			}
			for (ILayoutable i : children) {
				if (i.getClass() == Edge.class)
					sortedChildren.add(i);
			}
		}
		return sortedChildren;
	}
}
