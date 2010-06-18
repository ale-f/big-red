package dk.itu.big_red.wizards;

import java.util.Collection;
import java.util.List;

import dk.itu.big_red.editors.BigraphEditor;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;

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

public class ControlSelectionPage extends WizardPage {
	protected IControlSelector parent = null;
	
	protected Bigraph getModel() {
		return ((BigraphEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	public Collection<Control> getControls() {
		return getModel().getSignature().getControls();
	}
	
	protected ControlSelectionPage(String pageName, IControlSelector parent) {
		super(pageName);
		this.parent = parent;
		setPageComplete(false);
		setTitle("Select");
		setMessage("Choose a metaclass.");
	}

	public void setSelectedControl(Control m) {
		parent.setSelectedControl(m);
		if (m != null)
			setPageComplete(true);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		form.setLayout(l);
		
		Label label = new Label(form, SWT.NONE);
		label.setText("&Select a metaclass:");
		
		TreeViewer tree = new TreeViewer(form, SWT.BORDER);
		tree.setLabelProvider(new ControlLabelProvider());
		tree.setContentProvider(new CollectionContentProvider<Control>());
		tree.setInput(getControls());
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Control m = 
					(Control)((ITreeSelection)event.getSelection()).getFirstElement();
				setSelectedControl(m);
			}
		});
		
		setControl(form);
	}
}
