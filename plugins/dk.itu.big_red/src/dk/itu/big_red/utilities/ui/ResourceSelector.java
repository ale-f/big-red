package dk.itu.big_red.utilities.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;

public class ResourceSelector {
	public static interface ResourceListener {
		public void resourceChanged(IResource oldValue, IResource newValue);
	}
	
	private Button button;
	private IContainer container;
	private Mode mode;
	private String[] contentTypes;
	private IResource resource;
	
	public ResourceSelector(Composite c, IContainer k, Mode m, String... cT) {
		button = UI.chain(new Button(c, SWT.PUSH)).text("(none)").done();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});
		
		container = (k != null ? k : Project.getWorkspaceRoot());
		mode = m;
		contentTypes = cT;
	}
	
	public Button getButton() {
		return button;
	}
	
	public void open() {
		ResourceTreeSelectionDialog rtsd =
			new ResourceTreeSelectionDialog(button.getShell(), container, mode,
					contentTypes);
		rtsd.setInitialSelection(getResource());
		rtsd.setBlockOnOpen(true);
		int status = rtsd.open();
		if (status == Dialog.OK ||
		    status == ResourceTreeSelectionDialog.CLEAR)
			setResource(rtsd.getFirstResult());
	}
	
	public IResource getResource() {
		return resource;
	}
	
	public ResourceSelector setResource(IResource resource) {
		if (resource != null) {
			button.setText(
				resource.getFullPath().
					makeRelativeTo(container.getFullPath()).toString());
		} else button.setText("(none)");
		
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
		if ((newResource != null && !newResource.equals(oldResource)) ||
			(oldResource != null && !oldResource.equals(newResource)))
			for (ResourceListener l : getListeners())
				l.resourceChanged(oldResource, newResource);
	}
}
