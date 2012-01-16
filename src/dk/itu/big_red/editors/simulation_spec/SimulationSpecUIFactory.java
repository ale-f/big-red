package dk.itu.big_red.editors.simulation_spec;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.preferences.RedPreferencePage;
import dk.itu.big_red.utilities.ui.ProcessDialog;
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
			private Combo tools;
			private ProcessBuilder pb;
			
			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = new Composite(parent, SWT.NONE);
				c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout gl = new GridLayout(1, true);
				gl.marginLeft = gl.marginRight = IDialogConstants.LEFT_MARGIN;
				c.setLayout(gl);
				
				tools = new Combo(c, SWT.NONE);
				tools.setLayoutData(
						new GridData(SWT.FILL, SWT.TOP, true, false));
				
				return tools;
			}
			
			@Override
			protected void buttonPressed(int buttonId) {
				if (buttonId == IDialogConstants.OK_ID) {
					setReturnCode(Dialog.OK);
					new ProcessDialog(getShell(), pb).setInput(results).open();
					close();
				} else if (buttonId == IDialogConstants.CANCEL_ID) {
					setReturnCode(Dialog.CANCEL);
					cancelPressed();
				}
			}
			
			{
				pb = new ProcessBuilder(
					RedPlugin.getInstance().getPreferenceStore().
						getString(RedPreferencePage.PREFERENCE_BIGMC_PATH).
							split(" "));
			}
		};
	}
	
	public static TitleAreaDialog createResultsWindow(Shell parent, final String results) {
		return new TitleAreaDialog(UI.getShell()) {
			private Text resultsText;
			
			@Override
			protected Control createDialogArea(Composite parent) {
				resultsText =
					new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP |
							SWT.V_SCROLL);
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.widthHint = 400;
				gd.heightHint = 500;
				resultsText.setLayoutData(gd);
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
					createToolWindow(getShell(), resultsText.getText()).open();
				} else if (buttonId == SAVE_ID) {
					FileDialog d = UI.getFileDialog(getShell(),
							SWT.SAVE | SWT.APPLICATION_MODAL);
					d.setText("Save simulation spec as...");
					
					String filename = d.open();
					if (filename != null) {
						try {
							FileWriter fw = new FileWriter(filename);
							fw.write(resultsText.getText());
							fw.close();
							setMessage("Saved to \"" + filename + "\".",
								IMessageProvider.INFORMATION);
						} catch (IOException x) {
							setErrorMessage(x.getLocalizedMessage());
						}
					}
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