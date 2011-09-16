package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 * @see IPoint
 */
public abstract class Point extends ModelObject implements ILayoutable, IAdaptable, ICommentable, IPoint {
	/**
	 * The property name fired when the source edge changes.
	 */
	public static final String PROPERTY_LINK = "PointLink";
	
	/**
	 * The colour to be given to Points not connected to a {@link Link}.
	 */
	public static final RGB DEFAULT_COLOUR = new RGB(255, 0, 0);
	
	@Override
	public ILink getILink() {
		return link;
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

	protected Rectangle layout = new Rectangle(5, 5, 10, 10);
	
	@Override
	public Rectangle getLayout() {
		return new Rectangle(layout);
	}

	@Override
	public void setLayout(Rectangle layout) {
		if (layout != null) {
			Rectangle oldLayout = new Rectangle(this.layout);
			this.layout.setBounds(layout);
			firePropertyChange(ILayoutable.PROPERTY_LAYOUT, oldLayout, layout);
		}
	}

	protected Link link = null;
	
	/**
	 * Replaces the current {@link Link} of this Point.
	 * @param l the new {@link Link}
	 * @return the previous {@link Link}, or <code>null</code> if
	 * there wasn't one
	 */
	public Link setLink(Link l) {
		Link oldLink = link;
		link = l;
		firePropertyChange(Point.PROPERTY_LINK, oldLink, l);
		return oldLink;
	}
	
	public Link getLink() {
		return link;
	}

	protected String name = "?";
	
	/**
	 * Gets the name of this Point.
	 * @return the current name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this Point.
	 * @param name the new name
	 */
	public void setName(String name) {
		if (name != null) {
			String oldName = this.name;
			this.name = name;
			firePropertyChange(INameable.PROPERTY_NAME, oldName, name);
		}
	}

	@Override
	public Rectangle getRootLayout() {
		return new Rectangle(getLayout()).translate(getParent().getRootLayout().getTopLeft());
	}

	private Container parent = null;
	
	@Override
	public Container getParent() {
		return parent;
	}

	@Override
	public void setParent(Container p) {
		if (p != null) {
			Container oldParent = parent;
			parent = p;
			firePropertyChange(PROPERTY_PARENT, oldParent, parent);
		}
	}
	
	/**
	 * Returns an empty list.
	 */
	@Override
	public List<ILayoutable> getChildren() {
		return new ArrayList<ILayoutable>();
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void addChild(ILayoutable c) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void removeChild(ILayoutable c) {
	}

	/**
	 * Returns false.
	 */
	@Override
	public boolean hasChild(ILayoutable c) {
		return false;
	}
	
	/**
	 * Returns false.
	 */
	@Override
	public boolean canContain(ILayoutable c) {
		return false;
	}
	
	@Override
	public Point clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(this);
		} else return null;
	}
}
