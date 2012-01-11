package dk.itu.big_red.utilities.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;

public class ResourceSelector {
	public static interface ResourceListener {
		public void resourceChanged(IResource newValue);
	}
	
	private Button button;
	private IProject project;
	private Mode mode;
	private String[] contentTypes;
	private IResource resource;
	
	public ResourceSelector(Composite c, IProject p, Mode m, String... cT) {
		button = UI.newButton(c, SWT.PUSH, "(none)");
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
			for (ResourceListener l : getListeners())
				l.resourceChanged(resource);
		}
	}
	
	public IResource getResource() {
		return resource;
	}
	
	private ArrayList<ResourceListener> listeners =
		new ArrayList<ResourceListener>();
	
	public ResourceSelector addListener(ResourceListener l) {
		listeners.add(l);
		return this;
	}
	
	public ResourceSelector removeListener(ResourceListener l) {
		listeners.remove(l);
		return this;
	}
	
	public List<ResourceListener> getListeners() {
		return listeners;
	}
}
