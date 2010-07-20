package dk.itu.big_red.editors.assistants.resources;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import dk.itu.big_red.util.Project;

/**
 * SpecificResourceChangeListeners are special cases of {@link
 * IResourceChangeListener} which only care about a few specific resources.
 * @author alec
 *
 */
public abstract class SpecificResourceChangeListener implements
		IResourceChangeListener {
	ArrayList<IResource> resources = new ArrayList<IResource>();
	
	/**
	 * Default constructor; calls {@link #registerAsListener()}, then {@link
	 * #init()}.
	 */
	public SpecificResourceChangeListener() {
		registerAsListener();
		init();
	}
	
	/**
	 * Called when the constructor is otherwise finished.
	 * <p>Subclasses should call {@link #addResource(IResource)} in this method
	 * to add the resources they're interested in.
	 */
	protected abstract void init();
	
	/**
	 * Registers this object with the workspace to receive {@link
	 * IResourceChangeEvent#POST_CHANGE} events.
	 */
	protected void registerAsListener() {
		Project.getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.POST_CHANGE);
	}
	
	/**
	 * Unregisters this object from the workspace, no longer receiving
	 * resource change events.
	 */
	protected void unregisterAsListener() {
		Project.getWorkspace().removeResourceChangeListener(this);
	}
	
	/**
	 * Adds an {@link IResource} to this listener.
	 * <p>
	 * @param r
	 */
	public void addResource(IResource r) {
		if (r != null)
			resources.add(r);
	}
	
	/**
	 * Removes the given {@link IResource} from this listener. (Does nothing if
	 * the given resource was never added to this listener in the first place.)
	 * @param r an IResource
	 */
	public void removeResource(IResource r) {
		if (r != null)
			resources.remove(r);
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		for (IResource i : resources) {
			IResourceDelta d = 
				event.getDelta().findMember(i.getFullPath());
			if (d != null)
				resourceChanged(i, d);
		}
	}

	/**
	 * Notifies this listener that the given {@link IResource} has changed. The
	 * {@link IResourceDelta} describes the change.
	 * @param resource an IResource
	 * @param change an IResourceDelta
	 */
	abstract public void resourceChanged(IResource resource, IResourceDelta change);
}
