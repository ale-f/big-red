package dk.itu.big_red.wizards.export.assistants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.editors.assistants.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.model.import_export.ModelExport;
import dk.itu.big_red.util.Project;
import dk.itu.big_red.util.Types;
import dk.itu.big_red.util.UI;

public class WizardBigraphTextExportPage extends WizardPage {
	private Text bigraphText, resultText;
	private IPath bigraphPath;
	private Button clipboardButton, saveButton;
	
	private Class<? extends ModelExport<Bigraph>> ex = null;
	private IStructuredSelection selection = null;
	
	public WizardBigraphTextExportPage(String pageName, IStructuredSelection selection, Class<? extends ModelExport<Bigraph>> ex) {
		super(pageName);
		this.ex = ex;
		this.selection = selection;
		setPageComplete(false);
	}
	
	private boolean validate() {
		setMessage(getDescription());
		setPageComplete(false);
		resultText.setText("");
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		
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
		
		Bigraph model;
		
		BigraphXMLImport im = new BigraphXMLImport();
		try {
			im.setInputStream(Project.findFileByPath(null, bigraphPath).getContents());
			model = im.importObject();
		} catch (Exception e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		ModelExport<Bigraph> ex;
		try {
			ex = this.ex.newInstance();
		} catch (Exception e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ex.setModel(model);
			ex.setOutputStream(os);
			ex.exportObject();
		} catch (Exception e) {
			setErrorMessage(e.getLocalizedMessage());
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
		
		UI.setEnabled(true, clipboardButton, saveButton, resultText);
		resultText.setText(result.toString());
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
	
	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(3, false));
		
		ModifyListener sharedModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validate());
			}
		};
		
		Label folderLabel = new Label(root, 0);
		folderLabel.setText("&Bigraph:");
		
		bigraphText = new Text(root, SWT.BORDER);
		bigraphText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		bigraphText.addModifyListener(sharedModifyListener);
		
		Button bigraphButton = new Button(root, SWT.CENTER);
		bigraphButton.setText("&Browse...");
		bigraphButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IResource f = null;
				for (Object i : selection.toArray()) {
					if (i instanceof IResource) {
						f = (IResource)i;
						break;
					}
				}

				if (f == null)
					f = Project.getWorkspaceRoot();
				
				ResourceTreeSelectionDialog d =
					new ResourceTreeSelectionDialog(getShell(),
						Project.getWorkspaceRoot(),
						ResourceTreeSelectionDialog.MODE_FILE,
						"dk.itu.big_red.bigraph");
				/*if (bigraphPath != null)
					d.setInitialSelection(Project.findContainerByPath(null, bigraphPath));*/
				d.setInitialSelection(f);
				d.open();
				IResource result = d.getFirstResult();
				if (result instanceof IFile)
					setBigraphPath(result.getFullPath());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		GridData bigraphButtonLayoutData = new GridData();
		bigraphButtonLayoutData.widthHint = 100;
		bigraphButton.setLayoutData(bigraphButtonLayoutData);
		
		Label signatureLabel = new Label(root, SWT.NONE);
		signatureLabel.setText("&Result:");
		signatureLabel.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, true));
		
		resultText = new Text(root, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData targetTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetTextLayoutData.horizontalSpan = 2;
		resultText.setLayoutData(targetTextLayoutData);
		
		new Label(root, SWT.NONE); /* padding */
		
		Composite group = new Composite(root, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData groupLayoutData = new GridData(SWT.RIGHT, SWT.FILL, true, false);
		groupLayoutData.horizontalSpan = 2;
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
		
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		setControl(root);
	}
}
