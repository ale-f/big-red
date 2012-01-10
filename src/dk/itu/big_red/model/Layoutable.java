package dk.itu.big_red.model;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
import dk.itu.big_red.utilities.geometry.Rectangle;

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
public abstract class Layoutable extends Colourable implements IAdaptable {
	protected abstract class LayoutableChange
	extends dk.itu.big_red.model.ModelObject.ModelObjectChange {
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
		public LayoutableChange inverse() {
			return getCreator().changeLayout(oldLayout);
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

		private boolean oldNameRecorded = false;
		private String oldName;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
			oldNameRecorded = true;
		}
		
		@Override
		public LayoutableChange inverse() {
			return getCreator().changeName(oldName);
		}
		
		@Override
		public boolean canInvert() {
			return oldNameRecorded;
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
	
	protected Rectangle layout = new Rectangle();
	protected Container parent = null;
	
	/**
	 * Gets the current layout of this object.
	 * @return the current layout
	 */
	public ReadonlyRectangle getLayout() {
		return layout;
	}

	/**
	 * Gets a copy of the layout of this object relative to the top-left of the
	 * root {@link Bigraph} rather than the immediate parent.
	 * @return the current layout relative to the root
	 */
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
		firePropertyChange(Layoutable.PROPERTY_LAYOUT, oldLayout, layout);
	}

	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph() {
		if (getParent() == null) {
			return null;
		} else return getParent().getBigraph();
	}

	/**
	 * Returns the parent of this object.
	 * @return an {@link Container}
	 */
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
	
	private ModelPropertySource propertySource;
	/**
	 * The property name fired when the ILayoutable's layout changes (i.e.,
	 * it's resized or moved). The property values are {@link Rectangle}s.
	 */
	public static final String PROPERTY_LAYOUT = "ILayoutableLayout";
	
	/**
	 * The property name fired when the name changes. The property values are
	 * {@link String}s.
	 */
	public static final String PROPERTY_NAME = "LayoutableName";
	
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
	
	@Override
	public Layoutable clone(Map<ModelObject, ModelObject> m) {
		Layoutable l = (Layoutable)super.clone(m);
		l.setLayout(getLayout().getCopy());
		l.setComment(getComment());
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
	protected Dimension relayout(ChangeGroup cg) {
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
		if (name.equals(PROPERTY_NAME)) {
			return getName();
		} else if (name.equals(PROPERTY_LAYOUT)) {
			return getLayout();
		} else return super.getProperty(name);
	}
	
	@Override
	public UserControl getUserControl() {
		return UserControl.NONE;
	}
}
