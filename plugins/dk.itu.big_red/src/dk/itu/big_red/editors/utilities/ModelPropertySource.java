package dk.itu.big_red.editors.utilities;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.names.policies.INamePolicy;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import org.bigraph.extensions.param.ParameterUtilities;
import dk.itu.big_red.utilities.ui.NullTextPropertyDescriptor;

public class ModelPropertySource implements IRedPropertySource {
	private Layoutable object;
	
	public ModelPropertySource(Layoutable object) {
		this.object = object;
	}
	
	protected Layoutable getModel() {
		return object;
	}
	
	@Override
	public Object getEditableValue() {
		return null;
	}
	
	public abstract class ChangeValidator implements ICellEditorValidator {
		public abstract IChange getChange(Object value);
		
		@Override
		public String isValid(Object value) {
			try {
				ExecutorManager.getInstance().tryValidateChange(
						(PropertyScratchpad)null, getChange(value));
				return null;
			} catch (ChangeRejectedException cre) {
				return cre.getRationale();
			}
		}
	}
	
	private class NameValidator extends ChangeValidator {
		@Override
		public IChange getChange(Object value) {
			return new BoundDescriptor(getModel().getBigraph(),
					new NamedModelObject.ChangeNameDescriptor(
							getModel().getIdentifier(), (String)value));
		}
	}
	
	private List<IPropertyDescriptor> properties =
			new ArrayList<IPropertyDescriptor>();
	
	protected void addPropertyDescriptor(IPropertyDescriptor d) {
		properties.add(d);
	}
	
	/**
	 * Creates the {@link IPropertyDescriptor}s for this {@link
	 * ModelPropertySource}.
	 * <p>Subclasses can override, but they should call the {@code super}
	 * implementation at the earliest opportunity.
	 */
	protected void buildPropertyDescriptors() {
		addPropertyDescriptor(new PropertyDescriptor("Class", "Class"));
		
		if (object instanceof Link) {
			addPropertyDescriptor(new ColorPropertyDescriptor(
					ColourUtilities.OUTLINE, "Colour"));
		} else if (object instanceof Node) {
			addPropertyDescriptor(new ColorPropertyDescriptor(
					ColourUtilities.FILL, "Fill colour"));
			addPropertyDescriptor(new ColorPropertyDescriptor(
					ColourUtilities.OUTLINE, "Outline colour"));
			INamePolicy p = ParameterUtilities.getParameterPolicy(
					((Node)object).getControl());
			if (p != null)
				addPropertyDescriptor(new NullTextPropertyDescriptor(
						ParameterUtilities.PARAMETER, "Parameter"));
		}
		
		if (object instanceof ModelObject)
			addPropertyDescriptor(new NullTextPropertyDescriptor(
					ExtendedDataUtilities.COMMENT, "Comment"));
		if (object instanceof Layoutable) {
			NullTextPropertyDescriptor d = new NullTextPropertyDescriptor(
					Layoutable.PROPERTY_NAME, "Name");
			d.setValidator(new NameValidator());
			addPropertyDescriptor(d);
		}
	}
	
	@Override
	public final IPropertyDescriptor[] getPropertyDescriptors() {
		properties.clear();
		buildPropertyDescriptors();
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return object.getType();
		} else {
			if (ParameterUtilities.PARAMETER.equals(id)) {
				return ParameterUtilities.getParameter((Node)object);
			} else if (ExtendedDataUtilities.COMMENT.equals(id)) {
				return ExtendedDataUtilities.getComment(object);
			} else if (ColourUtilities.FILL.equals(id)) {
				return ColourUtilities.getFill(object).getRGB();
			} else if (ColourUtilities.OUTLINE.equals(id)) {
				return ColourUtilities.getOutline(object).getRGB();
			} else if (Layoutable.PROPERTY_NAME.equals(id)) {
				return object.getName();
			} else return null;
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	/**
	 * @deprecated Do not call this method.
	 * @throws UnsupportedOperationException always and forever
	 */
	@Override
	@Deprecated
	public final void setPropertyValue(Object id, Object value) {
		throw new Error(new UnsupportedOperationException(
				"" + id + ", " + value).fillInStackTrace());
	}

	/**
	 * @deprecated Do not call this method.
	 * @throws UnsupportedOperationException always and forever
	 */
	@Override
	@Deprecated
	public final void resetPropertyValue(Object id) {
		throw new Error(
				new UnsupportedOperationException("" + id).fillInStackTrace());
	}
	
	@Override
	public IChange setPropertyValueChange(Object id, Object newValue) {
		if (Layoutable.PROPERTY_NAME.equals(id)) {
			return new BoundDescriptor(getModel().getBigraph(),
					new NamedModelObject.ChangeNameDescriptor(
							getModel().getIdentifier(), (String)newValue));
		} else if (ExtendedDataUtilities.COMMENT.equals(id)) {
			return ExtendedDataUtilities.changeComment(
					getModel(), (String)newValue);
		} else if (ColourUtilities.FILL.equals(id)) {
			return ColourUtilities.changeFill(
					getModel(), new Colour((RGB)newValue));
		} else if (ColourUtilities.OUTLINE.equals(id)) {
			return ColourUtilities.changeOutline(
					getModel(), new Colour((RGB)newValue));
		} else if (ParameterUtilities.PARAMETER.equals(id)) {
			return ParameterUtilities.changeParameter(
					(Node)getModel(), (String)newValue);
		} else return null;
	}

	@Override
	public IChange resetPropertyValueChange(Object id) {
		if (ColourUtilities.FILL.equals(id)) {
			return ColourUtilities.changeFill(getModel(), null);
		} else if (ColourUtilities.OUTLINE.equals(id)) {
			return ColourUtilities.changeOutline(getModel(), null);
		} else return null;
	}
}
