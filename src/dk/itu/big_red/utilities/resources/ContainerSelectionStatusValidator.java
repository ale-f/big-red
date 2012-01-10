package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * ContainerSelectionStatusValidators only validate selections which contain a
 * single {@link IContainer}.
 * @author alec
 *
 */
public class ContainerSelectionStatusValidator implements ISelectionStatusValidator {
	@Override
	public IStatus validate(Object[] selection) {
		IStatus r = FileSelectionStatusValidator.OK_STATUS;
		if (selection.length != 1 || !(selection[0] instanceof IContainer))
			r = FileSelectionStatusValidator.CANCEL_STATUS;
		return r;
	}

}
