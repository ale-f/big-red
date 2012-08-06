package dk.itu.big_red.editors.rule;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Site;
import org.bigraph.model.changes.IChange;

import dk.itu.big_red.editors.utilities.ModelPropertySource;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.utilities.ui.NullTextPropertyDescriptor;

public class ReactumPropertySource extends ModelPropertySource {
	public ReactumPropertySource(Layoutable object) {
		super(object);
	}
	
	private class AliasValidator extends ChangeValidator {
		@Override
		public IChange getChange(Object value) {
			return ExtendedDataUtilities.changeAlias(
					((Site)getModel()), (String)value);
		}
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		if (ExtendedDataUtilities.ALIAS.equals(id)) {
			return ExtendedDataUtilities.getAlias((Site)getModel());
		} else return super.getPropertyValue(id);
	}
	
	@Override
	protected void buildPropertyDescriptors() {
		super.buildPropertyDescriptors();
		if (getModel() instanceof Site) {
			NullTextPropertyDescriptor d = new NullTextPropertyDescriptor(
					ExtendedDataUtilities.ALIAS, "Alias");
			d.setValidator(new AliasValidator());
			addPropertyDescriptor(d);
		}
	}
	
	@Override
	public IChange setPropertyValueChange(Object id, Object newValue) {
		if (ExtendedDataUtilities.ALIAS.equals(id)) {
			return ExtendedDataUtilities.changeAlias(
					(Site)getModel(), (String)newValue);
		} else return super.setPropertyValueChange(id, newValue);
	}
}
