package dk.itu.big_red.editors.rule;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

import dk.itu.big_red.editors.utilities.ModelPropertySource;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeAliasDescriptor;
import dk.itu.big_red.utilities.ui.NullTextPropertyDescriptor;

public class ReactumPropertySource extends ModelPropertySource {
	public ReactumPropertySource(Layoutable object) {
		super(object);
	}
	
	private class AliasValidator extends ChangeValidator {
		@Override
		public IChangeDescriptor getChange(Object value) {
			Site site = (Site)getModel();
			return new ChangeAliasDescriptor(
					site.getIdentifier(),
					ExtendedDataUtilities.getAlias(site), (String)value);
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
	public IChangeDescriptor setPropertyValueChange(
			Object id, Object newValue) {
		if (ExtendedDataUtilities.ALIAS.equals(id)) {
			Site site = (Site)getModel();
			return new ChangeAliasDescriptor(
					site.getIdentifier(),
					ExtendedDataUtilities.getAlias(site), (String)newValue);
		} else return super.setPropertyValueChange(id, newValue);
	}
	
	@Override
	public IChangeDescriptor resetPropertyValueChange(Object id) {
		if (ExtendedDataUtilities.ALIAS.equals(id)) {
			Site site = (Site)getModel();
			return new ChangeAliasDescriptor(
					site.getIdentifier(),
					ExtendedDataUtilities.getAlias(site), null);
		} else return super.resetPropertyValueChange(id);
	}
}
