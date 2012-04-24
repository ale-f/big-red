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

import dk.itu.big_red.preferences.RedPreferences;
import dk.itu.big_red.utilities.ui.UI;

public class ExportResults {
	private String results;
	
	public ExportResults(String results) {
		this.results = results;
	}
	
	public class ToolSelectorDialog extends Dialog {
		private Combo tools;
		
		private String results;
		
		public ToolSelectorDialog(Shell parentShell, String results) {
			super(parentShell);
			this.results = results;
		}

		private void setProgress(boolean progress) {
			UI.setEnabled(progress, tools);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout gl = new GridLayout(1, true);
			gl.marginLeft = gl.marginRight = gl.marginTop = 10;
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
				new ProcessDialog(getShell(),
						new ProcessBuilder(tools.getText().split(" ")))
					.setInput(results).open();
				okPressed();
			} else if (buttonId == IDialogConstants.CANCEL_ID) {
				cancelPressed();
			}
		}
	}

	public class ExportResultsDialog extends TitleAreaDialog {
		private Text resultsText;

		public ExportResultsDialog(Shell parentShell) {
			super(parentShell);
		}

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
			createButton(parent, BasicCommandLineInteractionManager.TO_TOOL_ID, BasicCommandLineInteractionManager.TO_TOOL_LABEL, false);
			createButton(parent, BasicCommandLineInteractionManager.SAVE_ID, BasicCommandLineInteractionManager.SAVE_LABEL, false);
			createButton(parent, BasicCommandLineInteractionManager.COPY_ID, BasicCommandLineInteractionManager.COPY_LABEL, false);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == BasicCommandLineInteractionManager.TO_TOOL_ID) {
				new ToolSelectorDialog(getShell(), resultsText.getText()).
					open();
			} else if (buttonId == BasicCommandLineInteractionManager.SAVE_ID) {
				FileDialog d = new FileDialog(getShell(),
					SWT.SAVE | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
				d.setText("Save As");
				
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
			} else if (buttonId == BasicCommandLineInteractionManager.COPY_ID) {
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
			
			setTitle("Export complete");
			setMessage("The document has been exported.");
		}
	}
}