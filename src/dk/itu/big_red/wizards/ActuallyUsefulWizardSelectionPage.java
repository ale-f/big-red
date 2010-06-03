package dk.itu.big_red.wizards;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ActuallyUsefulWizardSelectionPage extends WizardSelectionPage {
	private List<SubWizard> wizards = null;
	private String topLabel = null;
	private String originalMessage = null;
	
	protected ActuallyUsefulWizardSelectionPage(String pageName, List<SubWizard> wizards, String title, String message, String topLabel) {
		super(pageName);
		setTitle(title);
		originalMessage = message;
		setMessage(message);
		setWizards(wizards);
		setTopLabel(topLabel);
	}

	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		form.setLayout(l);
		
		Label label = new Label(form, SWT.NONE);
		label.setText(getTopLabel());
		
		TreeViewer tree = new TreeViewer(form, SWT.BORDER);
		tree.setLabelProvider(new SubWizardLabelProvider());
		tree.setContentProvider(new CollectionContentProvider<SubWizard>());
		tree.setInput(wizards);
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				SubWizard wiz =
					(SubWizard)((ITreeSelection)event.getSelection()).getFirstElement();
				setSelectedNode(wiz);
				if (wiz.getSummary() != null)
					setMessage(wiz.getSummary());
				else setMessage(originalMessage);
			}
		});
		
		setControl(form);
	}

	public List<SubWizard> getWizards() {
		return wizards;
	}

	public void setWizards(List<SubWizard> wizards) {
		this.wizards = wizards;
	}

	public void setTopLabel(String topLabel) {
		this.topLabel = topLabel;
	}

	public String getTopLabel() {
		return topLabel;
	}

}
