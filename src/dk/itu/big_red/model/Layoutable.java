package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

/**
 * All of the objects which can actually appear on a bigraph are instances of
 * {@link Layoutable}. This extends {@link ModelObject} with
 * implementations of {@link ILayoutable}, {@link ICommentable}, and {@link
 * IAdaptable}.
 * @author alec
 * @see ModelObject
 *
 */
public abstract class Layoutable extends ModelObject implements IAdaptable, ILayoutable, ICommentable {
	protected Rectangle layout = new Rectangle();
	protected Container parent = null;
	
	@Override
	public Rectangle getLayout() {
		return layout;
	}

	@Override
	public Rectangle getRootLayout() {
		return getLayout().getCopy().translate(getParent().getRootLayout().getTopLeft());
	}

	
	/**
	 * Sets the layout of this object.
	 * @param layout the new layout (which will belong to this object)
	 */
	protected void setLayout(Rectangle newLayout) {
		if (newLayout == null)
			return;
		Rectangle oldLayout = layout;
		layout = newLayout;
		firePropertyChange(PROPERTY_LAYOUT, oldLayout, layout);
	}

	@Override
	public Bigraph getBigraph() {
		if (getParent() == null) {
			return null;
		} else return getParent().getBigraph();
	}

	@Override
	public Container getParent() {
		return parent;
	}

	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	protected void setParent(Container parent) {
		this.parent = parent;
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
	
	private ModelPropertySource propertySource;
	
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
	
	/**
	 * Returns a new instance of this {@link Layoutable}'s class,
	 * created as though by <code>this.getClass().newInstance()</code>.
	 * @return a new instance of this Layoutable's class, or
	 * <code>null</code>
	 */
	protected Layoutable newInstance() {
		try {
			return getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Creates and returns a new copy of this {@link Layoutable}.
	 * <p>(Although the returned copy is a {@link Layoutable}, it's
	 * really an instance of whatever subclass this object is.)
	 * @return a new copy of this {@link Layoutable}
	 */
	@Override
	public Layoutable clone() {
		Layoutable m = newInstance();
		m.setLayout(getLayout());
		m.setComment(getComment());
		return m;
	}
	
	/**
	 * The space that should be present between any two {@link Layoutable}s
	 * after a <i>relayout</i> has been applied.
	 */
	protected static final int PADDING = 25;
	
	/**
	 * Creates {@link Change}s which will resize this object to a sensible
	 * default size.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return the proposed new size of this object
	 */
	protected Dimension relayout(ChangeGroup cg) {
		cg.add(new BigraphChangeLayout(this, new Rectangle(0, 0, 50, 50)));
		return new Dimension(50, 50);
	}
}
