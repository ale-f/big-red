package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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

/**
 * The <code>Thing</code> provides the basic functionality shared by most
 * elements of a bigraph. <code>Thing</code>s can be moved around, copied, and
 * deleted by the user, so {@link Root}s, {@link Node}s, and {@link Site}s are
 * all <code>Thing</code>s.
 * 
 * <p><code>Thing</code>s are only useful if they've been added to a {@link
 * Bigraph} - make sure you do that before you manipulate them.
 * @author alec
 *
 */
public class Thing implements IAdaptable, ILayoutable, ICommentable {
	protected PropertyChangeSupport listeners =
		new PropertyChangeSupport(this);
	
	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ThingChild";
	/**
	 * The property name fired when the name of a Thing changes.
	 */
	public static final String PROPERTY_RENAME = "ThingRename";
	
	protected Rectangle layout;
	
	protected ArrayList<ILayoutable> children = new ArrayList<ILayoutable>();
	protected ILayoutable parent = null;
	
	private IPropertySource propertySource = null;
	
	public Thing() {
		this.layout = new Rectangle(10, 10, 100, 100);
	}

	@Override
	public Rectangle getLayout() {
		return new Rectangle(this.layout);
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = this.layout;
		this.layout = new Rectangle(newLayout);
		listeners.firePropertyChange(PROPERTY_LAYOUT, oldLayout, this.layout);
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		return false;
	}
	
	@Override
	public void addChild(ILayoutable child) {
		boolean added = this.children.add(child);
		if (added) {
			child.setParent(this);
			listeners.firePropertyChange(PROPERTY_CHILD, null, child);
		}
	}
	
	@Override
	public void removeChild(ILayoutable child) {
		boolean removed = this.children.remove(child);
		if (removed)
			listeners.firePropertyChange(PROPERTY_CHILD, child, null);
	}
	
	public List<ILayoutable> getChildren() {
		return this.children;
	}
	
	@Override
	public ILayoutable getParent() {
		return this.parent;
	}
	
	@Override
	public void setParent(ILayoutable parent) {
		this.parent = parent;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	@Override
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
	
	protected Thing _overwrite(Thing orig) throws CloneNotSupportedException {
		this.setParent(orig.getParent());
		this.setLayout(new Rectangle(
			orig.getLayout().x + 10, orig.getLayout().y + 10,
			orig.getLayout().width, orig.getLayout().height));
		
		Iterator<ILayoutable> it = orig.getChildren().iterator();
		while (it.hasNext()) {
			ILayoutable child = it.next();
			ILayoutable childClone = child.clone();
			this.addChild(childClone);
			childClone.setLayout(child.getLayout());
		}
		
		return this;
	}
	
	public Thing clone() throws CloneNotSupportedException {
		return new Thing()._overwrite(this);
	}
	
	public Signature getSignature() {
		return getBigraph().getSignature();
	}
	
	public void relayout() {
	}

	private String comment = null;
	
	@Override
	public String getComment() {
		return this.comment;
	}

	@Override
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
}
