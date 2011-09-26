package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 * @see IPoint
 */
public abstract class Point extends Layoutable implements IAdaptable, IPoint {
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
}
