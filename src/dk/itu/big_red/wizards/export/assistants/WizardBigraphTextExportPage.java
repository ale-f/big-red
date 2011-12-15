package dk.itu.big_red.wizards.export.assistants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.Export.OptionDescriptor;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.preferences.RedPreferencePage;
import dk.itu.big_red.util.ProcessDialog;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.resources.Project;
import dk.itu.big_red.util.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.util.resources.Types;
import dk.itu.big_red.wizards.export.BigraphTextExportWizard;

public class WizardBigraphTextExportPage extends WizardPage {
	private Text bigraphText, resultText;
	private IPath bigraphPath;
	private Button clipboardButton, saveButton, bonusButton;
	
	private Label optionsLabel;
	private Composite optionsGroup;
	
	private IStructuredSelection selection = null;
	
	@Override
	public BigraphTextExportWizard getWizard() {
		return (BigraphTextExportWizard)super.getWizard();
	}
	
	public WizardBigraphTextExportPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.selection = selection;
		setPageComplete(false);
	}
	
	private boolean tryToLoadModel() {
		getWizard().setSource(null);
		
		String bT = bigraphText.getText();
		bigraphPath = new Path(bT);
		
		if (bT.length() == 0 || bigraphPath.segmentCount() == 0) {
			setErrorMessage("Bigraph is empty.");
			return false;
		}

		IResource bigraph = Project.findResourceByPath(null, bigraphPath);
		if (bigraph == null) {
			setErrorMessage("Bigraph '" + bT + "' does not exist.");
			return false;
		} else if (!(bigraph instanceof IFile)) {
			setErrorMessage("'" + bT + "' must be a signature.");
			return false;
		} else {
			IContentType t = Types.findContentTypeFor((IFile)bigraph);
			if (t == null || !t.getId().equals("dk.itu.big_red.bigraph")) {
				setErrorMessage("'" + bT + "' must be a bigraph.");
				return false;
			}
		}
		
		try {
			getWizard().setSource(BigraphXMLImport.importFile(
					Project.findFileByPath(null, bigraphPath)));
		} catch (Exception e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
	
	private boolean tryToExport() {
		Export<Bigraph> ex = getWizard().getExporter();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ex.setModel(getWizard().getSource());
			ex.setOutputStream(os);
			ex.exportObject();
		} catch (Exception e) {
			String message = e.getLocalizedMessage();
			setErrorMessage(message != null ? message :
				"An unknown error (of type '" + e + "') occurred.");
			e.printStackTrace();
			return false;
		}
		
		StringBuilder result = new StringBuilder();
		
		InputStreamReader r =
			new InputStreamReader(new ByteArrayInputStream(os.toByteArray()));
		char[] buffer = new char[1024];
		int read = 0;
		try {
			while (read != -1) {
				read = r.read(buffer);
				if (read > 0)
					result.append(buffer, 0, read);
			}
		} catch (IOException e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}

		resultText.setText(result.toString());
		return true;
	}
	
	private boolean validate() {
		setMessage(getDescription());
		setPageComplete(false);
		resultText.setText("");
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		
		if (!tryToLoadModel())
			return false;

		if (!tryToExport())
			return false;
		
		UI.setEnabled(true, clipboardButton, saveButton, resultText);
		setPageComplete(true);
		setErrorMessage(null);
		return true;
	}

	private void setBigraphPath(IPath path) {
		bigraphPath = path;
		bigraphText.setText(path.makeRelative().toString());
	}
	
	public IPath getBigraphPath() {
		return bigraphPath;
	}
	
	public void reset() {
		resultText.setText("");
		bigraphText.setText("");
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		setErrorMessage(null);
		setPageComplete(false);
		
		IResource r =
			Project.tryDesperatelyToGetAnIResourceOutOfAnIStructuredSelection(selection);
		if (r != null)
			bigraphText.setText(r.getFullPath().makeRelative().toString());
		
		populateOptionGroup();
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(3, false));
		
		Label folderLabel = new Label(root, 0);
		folderLabel.setText("&Bigraph:");
		
		bigraphText = new Text(root, SWT.BORDER);
		bigraphText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		bigraphText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validate());
			}
		});
		
		Button bigraphButton = new Button(root, SWT.CENTER);
		bigraphButton.setText("&Browse...");
		bigraphButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceTreeSelectionDialog d =
					new ResourceTreeSelectionDialog(getShell(),
						Project.getWorkspaceRoot(),
						ResourceTreeSelectionDialog.MODE_FILE,
						"dk.itu.big_red.bigraph");
				if (bigraphText.getText().length() > 0)
					d.setInitialSelection(Project.findFileByPath(null, new Path(bigraphText.getText())));
				d.open();
				IResource result = d.getFirstResult();
				if (result instanceof IFile)
					setBigraphPath(result.getFullPath());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		GridData bigraphButtonLayoutData = new GridData();
		bigraphButtonLayoutData.widthHint = 100;
		bigraphButton.setLayoutData(bigraphButtonLayoutData);
		
		Label signatureLabel = new Label(root, SWT.NONE);
		signatureLabel.setText("&Result:");
		signatureLabel.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, true));
		
		resultText = new Text(root, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData targetTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetTextLayoutData.horizontalSpan = 2;
		resultText.setLayoutData(targetTextLayoutData);
		
		optionsLabel = new Label(root, SWT.NONE);
		optionsLabel.setText("&Options:");
		optionsLabel.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, true));
		
		optionsGroup = new Composite(root, SWT.NONE);
		optionsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		groupLayoutData.horizontalSpan = 2;
		optionsGroup.setLayoutData(groupLayoutData);
		
		UI.setVisible(false, optionsLabel, optionsGroup);
		
		Composite group = new Composite(root, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		groupLayoutData = new GridData(SWT.RIGHT, SWT.FILL, true, false);
		groupLayoutData.horizontalSpan = 3;
		group.setLayoutData(groupLayoutData);
		
		clipboardButton = new Button(group, SWT.NONE);
		clipboardButton.setText("Copy to clipboard");
		clipboardButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				UI.setClipboardText(resultText.getText());
				setMessage("Copied to the clipboard.", IMessageProvider.INFORMATION);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText("Save...");
		saveButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog d = UI.getFileDialog(getShell(), SWT.SAVE | SWT.APPLICATION_MODAL);
					d.setText("Save exported model as...");
					
					String filename = d.open();
					if (filename != null) {
						try {
							FileWriter fw = new FileWriter(filename);
							fw.write(resultText.getText());
							fw.close();
							setMessage("Saved as \"" + filename + "\".", IMessageProvider.INFORMATION);
						} catch (IOException x) {
							setErrorMessage(x.getLocalizedMessage());
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
		});
		
		bonusButton = new Button(group, SWT.NONE);
		bonusButton.setText("Bonus...");
		bonusButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProcessDialog rd =
					new ProcessDialog(UI.getShell(),
						new ProcessBuilder(
							RedPlugin.getInstance().getPreferenceStore().
							getString(RedPreferencePage.PREFERENCE_BIGMC_PATH)));
				rd.setBlockOnOpen(true);
				rd.open();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		setControl(root);
	}

	public void populateOptionGroup() {
		for (Control c : optionsGroup.getChildren())
			c.dispose();
		
		final Export<Bigraph> exporter = getWizard().getExporter();
		
		List<OptionDescriptor> options = exporter.getOptions();
		
		if (options.size() != 0) {	
			for (final OptionDescriptor od : options) {
				Object ov = exporter.getOption(od.getID());
				if (ov instanceof Boolean) {
					final Button b = new Button(optionsGroup, SWT.CHECK);
					b.setSelection((Boolean)ov);
					b.setText(od.getDescription());
					b.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							exporter.setOption(od.getID(), b.getSelection());
							tryToExport();
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							widgetSelected(e);
						}
					});
				}
			}
			
			UI.setVisible(true, optionsLabel, optionsGroup);
		} else {
			UI.setVisible(false, optionsLabel, optionsGroup);
		}
		
		getShell().layout(true, true);
	}
}
