package dk.itu.big_red.wizards.export.assistants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
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
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.Saver.OptionDescriptor;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.UI;
import dk.itu.big_red.wizards.export.BigraphExportWizard;

public class WizardBigraphExportPage extends WizardPage {
	private ResourceSelector bigraphSelector;
	private Text resultText;
	private Button clipboardButton, saveButton;
	
	private Label optionsLabel;
	private Composite optionsGroup;
	
	private IStructuredSelection selection = null;
	
	@Override
	public BigraphExportWizard getWizard() {
		return (BigraphExportWizard)super.getWizard();
	}
	
	public WizardBigraphExportPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.selection = selection;
		setPageComplete(false);
	}
	
	private boolean tryToLoadModel() {
		getWizard().setSource(null);
		
		if (bigraphSelector.getResource() == null) {
			setErrorMessage("Bigraph is empty.");
			return false;
		}
		
		IResource bigraph = bigraphSelector.getResource();
		String bT = bigraph.getProjectRelativePath().toString();
		if (!(bigraph instanceof IFile)) {
			setErrorMessage("'" + bT + "' must be a bigraph.");
			return false;
		} else {
			IContentType t;
			try {
				t = ((IFile)bigraph).getContentDescription().getContentType();
			} catch (CoreException e) {
				t = null;
			}
			if (t == null || !t.getId().equals(Bigraph.CONTENT_TYPE)) {
				setErrorMessage("'" + bT + "' must be a bigraph.");
				return false;
			}
		}
		
		try {
			getWizard().setSource((Bigraph)Loader.fromFile((IFile)bigraph));
		} catch (Exception e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
	
	private boolean tryToExport() {
		Saver ex = getWizard().getExporter();
		IOAdapter ad = new IOAdapter();
		try {
			ex.setModel(getWizard().getSource());
			ex.setOutputStream(ad.getOutputStream());
			ex.exportObject();
		} catch (Exception e) {
			String message = e.getLocalizedMessage();
			setErrorMessage(message != null ? message :
				"An unknown error (of type '" + e + "') occurred.");
			e.printStackTrace();
			return false;
		}
		
		resultText.setText(IOAdapter.readString(ad.getInputStream()));
		return true;
	}
	
	private boolean validate() {
		setMessage(getDescription());
		setPageComplete(false);
		resultText.setText("");
		UI.setEnabled(false, clipboardButton, saveButton, resultText, optionsGroup);
		
		if (!tryToLoadModel())
			return false;

		if (!tryToExport())
			return false;
		
		UI.setEnabled(true, clipboardButton, saveButton, resultText, optionsGroup);
		setPageComplete(true);
		setErrorMessage(null);
		return true;
	}
	
	public IResource getBigraph() {
		return bigraphSelector.getResource();
	}
	
	public void reset() {
		resultText.setText("");
		bigraphSelector.setResource(null);
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		setErrorMessage(null);
		setPageComplete(false);
		
		IResource r = getSelectedResource(selection);
		if (r != null)
			bigraphSelector.setResource(r);
		
		populateOptionGroup();
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(3, false));
		
		UI.chain(new Label(root, 0)).text("&Bigraph:").done();
		
		bigraphSelector =
			new ResourceSelector(root, null, Mode.FILE, Bigraph.CONTENT_TYPE);
		bigraphSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				setPageComplete(validate());
			}
		});
		GridData selectorLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		selectorLayoutData.horizontalSpan = 2;
		bigraphSelector.getButton().setLayoutData(selectorLayoutData);
		
		Label signatureLabel = UI.chain(new Label(root, SWT.NONE)).text("&Result:").done();
		signatureLabel.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, true));
		
		resultText = new Text(root, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData targetTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetTextLayoutData.horizontalSpan = 2;
		resultText.setLayoutData(targetTextLayoutData);
		
		optionsLabel = UI.chain(new Label(root, SWT.NONE)).text("&Options:").done();
		optionsLabel.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, true));
		
		optionsGroup = new Composite(root, SWT.NONE);
		optionsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		groupLayoutData.horizontalSpan = 2;
		optionsGroup.setLayoutData(groupLayoutData);
		
		UI.setVisible(false, optionsLabel, optionsGroup);
		
		Composite group = new Composite(root, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		groupLayoutData = new GridData(SWT.RIGHT, SWT.TOP, true, false);
		groupLayoutData.horizontalSpan = 3;
		group.setLayoutData(groupLayoutData);
		
		clipboardButton = UI.chain(new Button(group, SWT.NONE)).text("Copy to clipboard").done();
		clipboardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UI.setClipboardText(resultText.getText());
				setMessage("Copied to the clipboard.", IMessageProvider.INFORMATION);
			}
		});
		
		saveButton = UI.chain(new Button(group, SWT.NONE)).text("Save...").done();
		saveButton.addSelectionListener(new SelectionAdapter() {
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
		});
		
		UI.setEnabled(false, clipboardButton, saveButton, resultText);
		setControl(root);
	}

	public void populateOptionGroup() {
		for (Control c : optionsGroup.getChildren())
			c.dispose();
		
		final Saver exporter = getWizard().getExporter();
		
		List<OptionDescriptor> options = exporter.getOptions();
		
		if (options.size() != 0) {	
			for (final OptionDescriptor od : options) {
				Object ov = exporter.getOption(od.getID());
				if (ov instanceof Boolean) {
					final Button b = UI.chain(new Button(optionsGroup, SWT.CHECK)).text(od.getDescription()).done();
					b.setSelection((Boolean)ov);
					b.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							exporter.setOption(od.getID(), b.getSelection());
							if (validate())
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

	private static IResource getSelectedResource(IStructuredSelection selection) {
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object i = it.next();
			if (i instanceof IResource) {
				return (IResource)i;
			} else if (i instanceof EditPart) {
				Object j = ((EditPart)i).getModel();
				if (j instanceof ModelObject)
					return ((ModelObject)j).getFile();
			}
		}
		return null;
	}
}
