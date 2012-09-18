package dk.itu.big_red.wizards.creation.assistants;

import org.bigraph.model.Signature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;

public class WizardNewAgentCreationPage extends WizardPage {
	private IContainer selectorRoot = null;
	
	private IFile signature;
	private IContainer folder;
	
	private IResource initialSelection;
	
	protected Text nameText = null;
	
	public WizardNewAgentCreationPage(
			String pageName, IStructuredSelection selection) {
		super(pageName);
		setPageComplete(false);
		
		Object o = selection.getFirstElement();
		if (o instanceof IResource && !(o instanceof IWorkspaceRoot)) {
			initialSelection = (IResource)o;
			if (o instanceof IContainer)
				setFolder((IContainer)o);
		}
		updateSelectorRoot();
	}

	private final void updateSelectorRoot() {
		if (initialSelection != null) {
			selectorRoot = initialSelection.getProject();
		} else {
			IResource
				folder = getFolder(),
				signature = getSignature();
			if (folder == null && signature == null) {
				selectorRoot = null;
			} else {
				selectorRoot =
						(folder != null ? folder : signature).getProject();
			}
		}
		if (folderSelector != null)
			folderSelector.setContainer(selectorRoot);
		if (signatureSelector != null)
			signatureSelector.setContainer(selectorRoot);
	}
	
	private ResourceSelector folderSelector, signatureSelector;
	
	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(2, false));
		
		ModifyListener sharedModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		};
		
		new Label(root, SWT.NONE).setText("&Parent folder:");
		
		folderSelector =
				new ResourceSelector(root, selectorRoot, Mode.CONTAINER);
		folderSelector.setResource(getFolder());
		folderSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (newValue instanceof IContainer) {
					setFolder((IContainer)newValue);
				} else setFolder(null);
				updateSelectorRoot();
			}
		});
		folderSelector.getButton().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(root, SWT.NONE).setText("&Signature:");
		
		signatureSelector =
				new ResourceSelector(root, selectorRoot, Mode.FILE,
						Signature.CONTENT_TYPE);
		signatureSelector.setResource(getSignature());
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (newValue instanceof IFile) {
					setSignature((IFile)newValue);
				} else setSignature(null);
				updateSelectorRoot();
			}
		});
		signatureSelector.getButton().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(root, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(
				new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		
		new Label(root, SWT.NONE).setText("&Name:");
		
		nameText = new Text(root, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		nameText.addModifyListener(sharedModifyListener);
		
		setControl(root);
	}
	
	private void setFolder(IContainer c) {
		folder = c;
		validate();
	}
	
	public IContainer getFolder() {
		return folder;
	}
	
	private void setSignature(IFile f) {
		signature = f;
		validate();
	}
	
	public IFile getSignature() {
		return signature;
	}
	
	public String getFileName() {
		String s = nameText.getText().trim();
		return (!s.endsWith(".bigraph-agent") ? s + ".bigraph-agent" : s);
	}
	
	private boolean validate() {
		setPageComplete(false);
		
		if (getFolder() == null) {
			setErrorMessage("Parent folder is empty.");
			return false;
		} else if (folder instanceof IWorkspaceRoot) {
			setErrorMessage("Parent is not a folder.");
			return false;
		}

		if (getSignature() == null) {
			setErrorMessage("Signature is empty.");
			return false;
		} else {
			IContentType t;
			try {
				t = getSignature().getContentDescription().getContentType();
			} catch (CoreException e) {
				t = null;
			}
			if (t == null || !t.getId().equals(Signature.CONTENT_TYPE)) {
				setErrorMessage("Signature has the wrong content type.");
				return false;
			}
		}
		
		String nT = nameText.getText().trim();
		
		if (nT.length() == 0) {
			setErrorMessage("Name is empty.");
			return false;
		}
		
		String proposedFileName = getFileName();
		
		IPath p = folder.getFullPath().makeRelative();
		if (!p.isValidSegment(proposedFileName)) {
			setErrorMessage("Name contains invalid characters.");
			return false;
		} else {
			p.append(proposedFileName);
			if (Project.findFileByPath(null, p) != null) {
				setErrorMessage("Name already exists.");
				return false;
			}
		}
		
		setPageComplete(true);
		setErrorMessage(null);
		return true;
	}
}
