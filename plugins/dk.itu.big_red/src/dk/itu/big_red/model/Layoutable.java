package dk.itu.big_red.model;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.assistants.IPropertyProvider;
import dk.itu.big_red.model.assistants.PropertyScratchpad;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.names.Namespace;

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
public abstract class Layoutable extends ModelObject {
	/**
	 * The property name fired when the name changes.
	 */
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "LayoutableName";
	
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
	
	public class ChangeRemove extends LayoutableChange {
		@Override
		public boolean isReady() {
			return (getCreator().getParent() != null);
		}
		
		private String oldName;
		private Container oldParent;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
			oldParent = getCreator().getParent();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null && oldParent != null);
		}
		
		@Override
		public Change inverse() {
			return oldParent.new ChangeAddChild(getCreator(), oldName);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + getCreator() + ")";
		}
	}
	
	private Container parent = null;
	
	/**
	 * Gets a copy of the layout of this object relative to the top-left of the
	 * root {@link Bigraph} rather than the immediate parent.
	 * @return the current layout relative to the root
	 */
	public Rectangle getRootLayout() {
		return getRootLayout(null);
	}

	public Rectangle getRootLayout(IPropertyProvider context) {
		return ExtendedDataUtilities.getLayout(context, this).getCopy().translate(
				getParent(context).getRootLayout(context).getTopLeft());
	}
	
	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph() {
		return getBigraph(null);
	}

	public Bigraph getBigraph(IPropertyProvider context) {
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

	public Container getParent(IPropertyProvider context) {
		return (Container)getProperty(context, PROPERTY_PARENT);
	}
	
	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	void setParent(Container parent) {
		Container oldParent = this.parent;
		this.parent = parent;
		firePropertyChange(PROPERTY_PARENT, oldParent, parent);
	}
	
	@Override
	public Layoutable clone(Map<ModelObject, ModelObject> m) {
		Layoutable l = (Layoutable)super.clone(m);
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
	protected Dimension relayout(IPropertyProvider context, ChangeGroup cg) {
		cg.add(ExtendedDataUtilities.changeLayout(this, new Rectangle(0, 0, 50, 50)));
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
	
	public String getName(IPropertyProvider context) {
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
	
	public void setName(PropertyScratchpad context, String name) {
		Namespace<Layoutable> ns =
				getBigraph(context).getNamespace(Bigraph.getNSI(this));
		
		ns.remove(context, getName(context));
		context.setProperty(this, Layoutable.PROPERTY_NAME, name);
		ns.put(context, name, this);
	}
	
	public LayoutableChange changeName(String newName) {
		return new ChangeName(newName);
	}
	
	public LayoutableChange changeRemove() {
		return new ChangeRemove();
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
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
