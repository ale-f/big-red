package dk.itu.big_red.editors.bigraph;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.Site;
import org.bigraph.model.changes.ChangeGroup;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.PropertySheetEntry;

import dk.itu.big_red.editors.assistants.Colour;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.IBigraphPart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;
import static dk.itu.big_red.editors.assistants.ExtendedDataUtilities.COMMENT;

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
			// String propertyName = child.getDisplayName();
			Object propertyID = child.getDescriptor().getId();
			EditPart j = (EditPart)getValues()[i];
			if (j instanceof LinkConnectionPart)
				j = ((LinkConnectionPart)j).getLinkPart();
			if (j instanceof IBigraphPart)
				target = ((IBigraphPart)j).getBigraph();
			
			// IPropertySource propertySource = getPropertySource(j);
			// Object oldValue = propertySource.getPropertyValue(propertyID);
			Object newValue = child.getValues()[i];
			
			if (newValue instanceof RGB)
				newValue = new Colour((RGB)newValue);
			
			if (propertyID.equals(Layoutable.PROPERTY_NAME)) {
				cg.add(((Layoutable)j.getModel()).changeName((String)newValue));
			} else if (COMMENT.equals(propertyID)) {
				cg.add(ExtendedDataUtilities.changeComment(
						(ModelObject)j.getModel(), (String)newValue));
			} else if (propertyID.equals(ExtendedDataUtilities.FILL)) {
				cg.add(ExtendedDataUtilities.changeFill(
						(ModelObject)j.getModel(), (Colour)newValue));
			} else if (propertyID.equals(ExtendedDataUtilities.OUTLINE)) {
				cg.add(ExtendedDataUtilities.changeOutline(
						(ModelObject)j.getModel(), (Colour)newValue));
			} else if (propertyID.equals(ExtendedDataUtilities.ALIAS)) {
				cg.add(ExtendedDataUtilities.changeAlias(((Site)j.getModel()), (String)newValue));
			} else if (propertyID.equals(ExtendedDataUtilities.PARAMETER)) {
				cg.add(ExtendedDataUtilities.changeParameter(
						(Node)j.getModel(), (String)newValue));
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
