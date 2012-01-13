package dk.itu.big_red.editors.simulation_spec;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.utilities.ui.UI;

public class SimulationSpecUIFactory {
	public static final int TO_TOOL_ID = 1000;
	public static final String TO_TOOL_LABEL = "To tool...";
	
	public static final int SAVE_ID = 1001;
	public static final String SAVE_LABEL = "Save...";
	
	public static final int COPY_ID = 1002;
	public static final String COPY_LABEL = "Copy";
	
	public static Dialog createToolWindow(Shell parent, final String results) {
		return new Dialog(parent) {
			{
				setBlockOnOpen(true);
			}
		};
	}
	
	public static TitleAreaDialog createResultsWindow(Shell parent, final String results) {
		return new TitleAreaDialog(UI.getShell()) {
			private Text resultsText;
			
			@Override
			protected Control createDialogArea(Composite parent) {
				resultsText = new Text(parent, SWT.MULTI | SWT.BORDER);
				resultsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				resultsText.setText(results);
				return resultsText;
			}
			
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, IDialogConstants.OK_ID, "OK", true);
				createButton(parent, TO_TOOL_ID, TO_TOOL_LABEL, false);
				createButton(parent, SAVE_ID, SAVE_LABEL, false);
				createButton(parent, COPY_ID, COPY_LABEL, false);
			}
			
			@Override
			protected void buttonPressed(int buttonId) {
				if (buttonId == TO_TOOL_ID) {
					createToolWindow(getShell(), results).open();
				} else if (buttonId == SAVE_ID) {
					
				} else if (buttonId == COPY_ID) {
					UI.setClipboardText(resultsText.getText());
					setMessage("Copied to the clipboard.",
							IMessageProvider.INFORMATION);
				} else super.buttonPressed(buttonId);
			}
			
			@Override
			protected Control createButtonBar(Composite parent) {
				Control c = super.createButtonBar(parent);
				c.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
				return c;
			}
			
			@Override
			public void create() {
				super.create();
				
				setTitle("Simulation spec exported");
				setMessage("The simulation spec has been exported.");
			}
		};
	}
}