package dk.itu.big_red.model;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.ChangeGroup;

/**
 * All of the objects which can actually appear on a bigraph are instances of
 * {@link Layoutable}:
 * 
 * <ul>
 * <li>they have a <i>layout</i> (a {@link Rectangle}) which defines their
 * bounding box and which can change under some circumstances; and
 * <li>they have a <i>parent</i> (a {@link Container}), which contains them.
 * </ul>
 * 
 * <p>{@link Layoutable}s are subclasses of {@link Colourable}, and
 * additionally provide implementations of {@link ICommentable} and {@link
 * IAdaptable}.
 * 
 * @author alec
 * @see ModelObject
 *
 */
public abstract class Layoutable extends Colourable {
	/**
	 * The property name fired when the name changes.
	 */
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "LayoutableName";
	
	/**
	 * The property name fired when the layout changes.
	 */
	@RedProperty(fired = Rectangle.class, retrieved = Rectangle.class)
	public static final String PROPERTY_LAYOUT = "LayoutableLayout";
	
	/**
	 * The property name fired when the parent changes.
	 */
	@RedProperty(fired = Container.class, retrieved = Container.class)
	public static final String PROPERTY_PARENT = "LayoutableParent";
	
	abstract class LayoutableChange extends ModelObjectChange {
		@Override
		public Layoutable getCreator() {
			return Layoutable.this;
		}
	}
	
	public class ChangeLayout extends LayoutableChange {
		public Rectangle newLayout;
		
		protected ChangeLayout(Rectangle newLayout) {
			this.newLayout = newLayout;
		}

		private Rectangle oldLayout;
		@Override
		public void beforeApply() {
			oldLayout = getCreator().getLayout().getCopy();
		}
		
		@Override
		public ChangeLayout inverse() {
			return new ChangeLayout(oldLayout);
		}
		
		@Override
		public boolean canInvert() {
			return (oldLayout != null);
		}
		
		@Override
		public boolean isReady() {
			return (newLayout != null);
		}
		
		@Override
		public String toString() {
			return "Change(set layout of " + getCreator() + " to " + newLayout + ")";
		}
	}

	public class ChangeName extends LayoutableChange {
		public String newName;
		
		protected ChangeName(String newName) {
			this.newName = newName;
		}

		private String oldName;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
		}
		
		@Override
		public ChangeName inverse() {
			return new ChangeName(oldName);
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public boolean isReady() {
			return (newName != null);
		}
		
		@Override
		public String toString() {
			return "Change(set name of " + getCreator() + " to " + newName + ")";
		}
	}
	
	private Rectangle layout = new Rectangle();
	private Container parent = null;
	
	/**
	 * Gets the current layout of this object.
	 * @return the current layout
	 */
	public Rectangle getLayout() {
		return layout;
	}
	
	public Rectangle getLayout(IPropertyProviderProxy context) {
		return (Rectangle)getProperty(context, PROPERTY_LAYOUT);
	}
	
	/**
	 * Gets a copy of the layout of this object relative to the top-left of the
	 * root {@link Bigraph} rather than the immediate parent.
	 * @return the current layout relative to the root
	 */
	public Rectangle getRootLayout() {
		return getRootLayout(null);
	}

	public Rectangle getRootLayout(IPropertyProviderProxy context) {
		return getLayout(context).getCopy().translate(
				getParent(context).getRootLayout(context).getTopLeft());
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
	
	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph() {
		return getBigraph(null);
	}

	public Bigraph getBigraph(IPropertyProviderProxy context) {
		if (getParent(context) == null) {
			return null;
		} else return getParent(context).getBigraph(context);
	}
	
	/**
	 * Returns the parent of this object.
	 * @return an {@link Container}
	 */
	public Container getParent() {
		return parent;
	}

	public Container getParent(IPropertyProviderProxy context) {
		return (Container)getProperty(context, PROPERTY_PARENT);
	}
	
	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	protected void setParent(Container parent) {
		Container oldParent = this.parent;
		this.parent = parent;
		firePropertyChange(PROPERTY_PARENT, oldParent, parent);
	}
	
	@Override
	public Layoutable clone(Map<ModelObject, ModelObject> m) {
		Layoutable l = (Layoutable)super.clone(m);
		l.setLayout(getLayout().getCopy());
		return l;
	}
	
	/**
	 * The space that should be present between any two {@link Layoutable}s
	 * after a <i>relayout</i> has been applied.
	 */
	protected static final int PADDING = 25;
	
	/**
	 * Creates {@link LayoutableChange}s which will resize this object to a sensible
	 * default size.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return the proposed new size of this object
	 */
	protected Dimension relayout(IPropertyProviderProxy context, ChangeGroup cg) {
		cg.add(changeLayout(new Rectangle(0, 0, 50, 50)));
		return new Dimension(50, 50);
	}
	
	private String name = null;
	
	/**
	 * Gets this object's name.
	 * @return a String
	 */
	public String getName() {
		return name;
	}
	
	public String getName(IPropertyProviderProxy context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	/**
	 * Sets this object's name.
	 * @param name the new name for this object
	 */
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public LayoutableChange changeLayout(Rectangle newLayout) {
		return new ChangeLayout(newLayout);
	}
	
	public LayoutableChange changeName(String newName) {
		return new ChangeName(newName);
	}
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else if (PROPERTY_LAYOUT.equals(name)) {
			return getLayout();
		} else if (PROPERTY_PARENT.equals(name)) {
			return getParent();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		name = null;
		parent = null;
		
		super.dispose();
	}
}
