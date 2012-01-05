package dk.itu.big_red.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import dk.itu.big_red.util.resources.ResourceTreeSelectionDialog;

public class ResourceSelector {
	private Button button;
	private IProject project;
	private int mode;
	private String[] contentTypes;
	private IResource resource;
	
	public ResourceSelector(Composite c, IProject p, int m, String... cT) {
		button = new Button(c, SWT.PUSH);
		button.setText("(none)");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});
		
		project = p;
		mode = m;
		contentTypes = cT;
	}
	
	public Button getButton() {
		return button;
	}
	
	public void open() {
		ResourceTreeSelectionDialog rtsd =
			new ResourceTreeSelectionDialog(button.getShell(), project, mode,
					contentTypes);
		rtsd.setBlockOnOpen(true);
		int status = rtsd.open();
		if (status == Dialog.OK) {
			resource = rtsd.getFirstResult();
			button.setText(resource.getProjectRelativePath().toString());
		}
	}
	
	public IResource getResource() {
		return resource;
	}
}
