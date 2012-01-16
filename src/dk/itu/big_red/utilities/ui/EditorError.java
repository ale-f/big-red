package dk.itu.big_red.utilities.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EditorError {
	public EditorError(final Composite parent, IStatus reason) {
		Label l = UI.newLabel(parent, SWT.CENTER | SWT.WRAP, reason.toString());
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l.setBackground(ColorConstants.yellow);
		l.setForeground(ColorConstants.red);
	}
}
