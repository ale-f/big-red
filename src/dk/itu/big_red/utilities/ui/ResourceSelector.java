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
		public void resourceChanged(IResource oldValue, IResource newValue);
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
		rtsd.setInitialSelection(getResource());
		rtsd.setBlockOnOpen(true);
		int status = rtsd.open();
		if (status == Dialog.OK) {
			IResource newResource = rtsd.getFirstResult();
			button.setText(newResource.getProjectRelativePath().toString());
			setResource(newResource);
		} else if (status == ResourceTreeSelectionDialog.CLEAR) {
			button.setText("(none)");
			setResource(null);
		}
	}
	
	public IResource getResource() {
		return resource;
	}
	
	public ResourceSelector setResource(IResource resource) {
		IResource oldResource = this.resource;
		this.resource = resource;
		postResourceNotification(oldResource, resource);
		return this;
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
	
	private void postResourceNotification(IResource oldResource, IResource newResource) {
		if ((newResource == null && oldResource != null) ||
			!newResource.equals(oldResource))
			for (ResourceListener l : getListeners())
				l.resourceChanged(oldResource, newResource);
	}
}
