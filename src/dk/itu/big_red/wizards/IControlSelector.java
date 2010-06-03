package dk.itu.big_red.wizards;

import dk.itu.big_red.model.Control;

/**
 * Wizards which require the user to choose a control should implement
 * IControlSelector and should incorporate a ControlSelectionPage.
 * @author alec
 *
 */
public interface IControlSelector {
	public Control getSelectedControl();
	public void setSelectedControl(Control m);
}
