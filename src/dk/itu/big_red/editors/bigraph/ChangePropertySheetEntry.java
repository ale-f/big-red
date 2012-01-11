package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheetEntry;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.IBigraphPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.utilities.Colour;

public class ChangePropertySheetEntry extends UndoablePropertySheetEntry {

	public ChangePropertySheetEntry(CommandStack commandStack) {
		super(commandStack);
	}
	
	@Override
	protected PropertySheetEntry createChildEntry() {
		return new ChangePropertySheetEntry(getCommandStack());
	}
	
	@Override
	public void resetPropertyValue() {
		/* generates and enstacks Commands */
		super.resetPropertyValue();
	}
	
	@Override
	protected void valueChanged(PropertySheetEntry child) {
		 valueChanged((ChangePropertySheetEntry)child, new ChangeGroup(), null);
	}
	
	protected void valueChanged(ChangePropertySheetEntry child, ChangeGroup cg, Bigraph target) {
		/* generates and enstacks Commands */
		for (int i = 0; i < getValues().length; i++) {
			String propertyName = child.getDisplayName();
			Object propertyID = child.getDescriptor().getId();
			EditPart j = (EditPart)getValues()[i];
			if (j instanceof IBigraphPart)
				target = ((IBigraphPart)j).getBigraph();
			
			IPropertySource propertySource = getPropertySource(j);
			Object oldValue = propertySource.getPropertyValue(propertyID);
			Object newValue = child.getValues()[i];
			
			if (newValue instanceof String &&
			    ((String)newValue).length() == 0 &&
				(propertyID.equals(ModelObject.PROPERTY_COMMENT) ||
				 propertyID.equals(Site.PROPERTY_ALIAS)))
				newValue = null;
			
			if (newValue instanceof RGB)
				newValue = new Colour((RGB)newValue);
			
			if (propertyID.equals(Layoutable.PROPERTY_NAME)) {
				cg.add(((Layoutable)j.getModel()).changeName((String)newValue));
			} else if (propertyID.equals(ModelObject.PROPERTY_COMMENT)) {
				cg.add(((Layoutable)j.getModel()).changeComment((String)newValue));
			} else if (propertyID.equals(Colourable.PROPERTY_FILL)) {
				cg.add(((Colourable)j.getModel()).changeFillColour((Colour)newValue));
			} else if (propertyID.equals(Colourable.PROPERTY_OUTLINE)) {
				cg.add(((Colourable)j.getModel()).changeOutlineColour((Colour)newValue));
			} else if (propertyID.equals(Site.PROPERTY_ALIAS)) {
				cg.add(((Site)j.getModel()).changeAlias((String)newValue));
			}
		}
		if (getParent() != null) {
			((ChangePropertySheetEntry)getParent()).valueChanged(this, cg, target);
		} else {
			getCommandStack().execute(new ChangeCommand() {
				@Override
				public ChangeCommand prepare() {
					return this;
				}
			}.setTarget(target).setChange(cg).prepare());
		}
	}
}
