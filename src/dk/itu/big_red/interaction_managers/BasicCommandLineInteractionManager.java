package dk.itu.big_red.interaction_managers;

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

import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.preferences.RedPreferences;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.ProcessDialog;
import dk.itu.big_red.utilities.ui.UI;

public class BasicCommandLineInteractionManager extends InteractionManager {
	public static final int TO_TOOL_ID = 1000;
	public static final String TO_TOOL_LABEL = "To tool...";
	
	public static final int SAVE_ID = 1001;
	public static final String SAVE_LABEL = "Save...";
	
	public static final int COPY_ID = 1002;
	public static final String COPY_LABEL = "Copy";
	
	private Saver exporter;
	
	public BasicCommandLineInteractionManager(Saver exporter) {
		this.exporter = exporter;
	}
	
	@Override
	public void run() {
		try {
			IOAdapter io = new IOAdapter();
			exporter.setModel(getSimulationSpec()).
				setOutputStream(io.getOutputStream()).exportObject();
			createResultsWindow(UI.getShell(),
					IOAdapter.readString(io.getInputStream())).open();
		} catch (SaveFailedException ex) {
			ex.printStackTrace();
		}
	}

	public static Dialog createToolWindow(Shell parent, final String results) {
		return new Dialog(parent) {
			private Combo tools;
			
			private void setProgress(boolean progress) {
				UI.setEnabled(progress, tools);
			}
			
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
				tools.setItems(RedPreferences.getExternalTools());
				if (tools.getItemCount() > 0) {
					tools.select(0);
					setProgress(true);
				} else setProgress(false);
				
				return tools;
			}
			
			@Override
			protected void buttonPressed(int buttonId) {
				if (buttonId == IDialogConstants.OK_ID) {
					setReturnCode(Dialog.OK);
					new ProcessDialog(getShell(),
							new ProcessBuilder(tools.getText().split(" ")))
						.setInput(results).open();
					close();
				} else if (buttonId == IDialogConstants.CANCEL_ID) {
					setReturnCode(Dialog.CANCEL);
					cancelPressed();
				}
			}
		};
	}

	public static TitleAreaDialog createResultsWindow(Shell parent, final String results) {
		return new TitleAreaDialog(UI.getShell()) {
			private Text resultsText;
			
			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = new Composite(parent, SWT.NONE);
				c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout gl = new GridLayout(1, true);
				gl.marginLeft = gl.marginRight = gl.marginTop = 10;
				c.setLayout(gl);
				
				resultsText =
					new Text(c, SWT.MULTI | SWT.BORDER | SWT.WRAP |
							SWT.V_SCROLL);
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.widthHint = 400;
				gd.heightHint = 500;
				resultsText.setLayoutData(gd);
				resultsText.setText(results);
				return c;
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