package dk.itu.big_red.wizards.export.assistants;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.editors.assistants.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.util.Project;
import dk.itu.big_red.util.Types;
import dk.itu.big_red.util.UI;

public class WizardBigraphTikZExportPage extends WizardPage {
	private Text bigraphText, targetText;
	private IPath bigraphPath;
	private String targetString;
	
	private IStructuredSelection selection = null;
	
	public WizardBigraphTikZExportPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.selection = selection;
		setPageComplete(false);
	}
	
	private boolean validate() {
		setPageComplete(false);
		
		String bT = bigraphText.getText();
		targetString = targetText.getText();
		bigraphPath = new Path(bT);
		IPath targetPath = new Path(targetString);
		
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
		
		if (targetString.length() == 0 || targetPath.segmentCount() == 0) {
			setErrorMessage("Target file is empty.");
			return false;
		}
		
		setPageComplete(true);
		setErrorMessage(null);
		return true;
	}
	
	private void setBigraphPath(IPath path) {
		bigraphPath = path;
		bigraphText.setText(path.makeRelative().toString());
	}
	
	private void setTargetPath(String path) {
		targetString = path;
		targetText.setText(path);
	}
	
	public IPath getBigraphPath() {
		return bigraphPath;
	}
	
	public String getTargetPath() {
		return targetString;
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
				IContainer f = null;
				for (Object i : selection.toArray()) {
					if (i instanceof IFolder)
						f = (IFolder)i;
					else if (i instanceof IResource)
						f = ((IResource)i).getParent();
					else if (i instanceof IAdaptable)
						f = (IFolder)((IAdaptable)i).getAdapter(IFolder.class);
					
					if (f != null)
						break;
				}

				if (f == null)
					f = Project.getWorkspaceRoot();
				
				ResourceTreeSelectionDialog d =
					new ResourceTreeSelectionDialog(getShell(),
						Project.getWorkspaceRoot(),
						ResourceTreeSelectionDialog.MODE_FILE,
						"dk.itu.big_red.bigraph");
				if (bigraphPath != null)
					d.setInitialSelection(Project.findContainerByPath(null, bigraphPath));
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
		signatureLabel.setText("&Target file:");
		
		targetText = new Text(root, SWT.BORDER);
		targetText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		targetText.addModifyListener(sharedModifyListener);
		
		Button targetButton = new Button(root, SWT.NONE);
		targetButton.setText("B&rowse...");
		targetButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog d = UI.getFileDialog(SWT.SAVE);
				d.setFilterExtensions(new String[] {
					"*.tex"
				});
				d.setFilterNames(new String[] {
					"LaTeX documents (*.tex)"
				});
				d.setFileName(targetString);
				
				String result = d.open();
				if (result != null)
					setTargetPath(result);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		GridData targetButtonLayoutData = new GridData();
		targetButtonLayoutData.widthHint = 100;
		targetButton.setLayoutData(targetButtonLayoutData);
		
		setControl(root);
	}
}
