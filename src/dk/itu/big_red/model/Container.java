package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Node;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.util.Utility;

/**
 * The <code>Container</code> provides the basic functionality shared by most
 * elements of a bigraph. <code>Container</code>s can be moved around, copied, and
 * deleted by the user, so {@link Root}s, {@link Node}s, and {@link Site}s are
 * all <code>Container</code>s.
 * 
 * <p><code>Container</code>s are only useful if they've been added to a {@link
 * Bigraph} - make sure you do that before you manipulate them.
 * @author alec
 *
 */
public class Container extends LayoutableModelObject implements IAdaptable, ILayoutable, ICommentable {
	protected ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
	
	private IPropertySource propertySource = null;
	
	public Container() {
		layout = new Rectangle(10, 10, 100, 100);
	}
	
	public boolean canContain(ILayoutable child) {
		return false;
	}
	
	public void addChild(ILayoutable child) {
		boolean added = children.add(child);
		if (added) {
			child.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, child);
		}
	}
	
	public void removeChild(ILayoutable child) {
		boolean removed = children.remove(child);
		if (removed)
			firePropertyChange(PROPERTY_CHILD, child, null);
	}
	
	public List<ILayoutable> getChildren() {
		return children;
	}

	public boolean hasChild(ILayoutable child) {
		return children.contains(child);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null)
				propertySource = new ModelPropertySource(this);
			return propertySource;
		}
		return null;
	}
	
	protected Container _overwrite(Container orig) throws CloneNotSupportedException {
		setParent(orig.getParent());
		setLayout(new Rectangle(
			orig.getLayout().x + 10, orig.getLayout().y + 10,
			orig.getLayout().width, orig.getLayout().height));
		
		Iterator<ILayoutable> it = orig.getChildren().iterator();
		while (it.hasNext()) {
			ILayoutable child = it.next();
			ILayoutable childClone = child.clone();
			addChild(childClone);
			childClone.setLayout(child.getLayout());
		}
		
		return this;
	}
	
	@Override
	public Container clone() throws CloneNotSupportedException {
		return new Container()._overwrite(this);
	}
	
	public Signature getSignature() {
		return getBigraph().getSignature();
	}
	
	public void relayout() {
		int leftProgress = 10;
		int maxHeight = 0;
		int topOffset = 10;
		
		if (this instanceof Bigraph)
			topOffset += ((Bigraph)this).upperRootBoundary;
		
		for (ILayoutable i : Utility.groupListByClass(getChildren(), Container.class)) {
			Rectangle layout = i.getLayout();
			if (maxHeight < layout.height)
				maxHeight = layout.height;
			layout.setLocation(leftProgress, topOffset);
			leftProgress += layout.width + 10;
			i.setLayout(layout);
		}
		setLayout(getLayout().setSize(leftProgress, maxHeight + 20));
	}

	private String comment = null;
	
	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
}
