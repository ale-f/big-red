package dk.itu.big_red.wizards.export.assistants;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.utilities.ui.UI;
import dk.itu.big_red.wizards.export.BigraphExportWizard;

public class WizardBigraphExportSelectorPage extends WizardPage {
	@Override
	public BigraphExportWizard getWizard() {
		return (BigraphExportWizard)super.getWizard();
	}
	
	public WizardBigraphExportSelectorPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Export bigraph to text format");
		setMessage("Select an export format.");
	}

	public void setSelectedExporter(IConfigurationElement m) {
		getWizard().setExporter(m);
		setPageComplete(true);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		form.setLayout(l);
		
		UI.newLabel(form, SWT.NONE, "&Select an export format:");
		
		TreeViewer tree = new TreeViewer(form, SWT.BORDER);
		tree.setLabelProvider(new ImportExportLabelProvider());
		tree.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IConfigurationElement[])
					return (IConfigurationElement[])inputElement;
				else return null;
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		tree.setInput(RedPlugin.getConfigurationElementsFor("dk.itu.big_red.export"));
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IConfigurationElement e = 
					(IConfigurationElement)((ITreeSelection)event.getSelection()).getFirstElement();
				setSelectedExporter(e);
			}
		});
		
		tree.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (isPageComplete())
					getContainer().showPage(getNextPage());
			}
		});
		
		setControl(form);
	}
}