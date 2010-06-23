package dk.itu.big_red.wizards;

import dk.itu.big_red.editors.BigraphEditor;
import dk.itu.big_red.model.Bigraph;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

public class PortSelectionPage extends WizardPage {
	protected IPortSelector parent = null;
	
	protected Bigraph getModel() {
		return ((BigraphEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	protected PortSelectionPage(String pageName, IPortSelector parent) {
		super(pageName);
		this.parent = parent;
		setPageComplete(false);
		setTitle("Select");
		setMessage("Choose a port.");
	}

	public void setPort(String p) {
		parent.setSelectedPort(p);
		if (p != null)
			setPageComplete(true);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		form.setLayout(l);
		
		Label label = new Label(form, SWT.NONE);
		label.setText("&Select a port:");
		
		TreeViewer tree = new TreeViewer(form, SWT.BORDER);
		tree.setLabelProvider(new PortLabelProvider());
		tree.setContentProvider(new CollectionContentProvider<String>());
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				String p = 
					(String)((ITreeSelection)event.getSelection()).getFirstElement();
				setPort(p);
			}
		});
		
		setControl(form);
	}
}
