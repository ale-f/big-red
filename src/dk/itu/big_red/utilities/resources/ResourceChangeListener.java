package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

public class ResourceChangeListener implements IResourceChangeListener {
	private void handleDelta(IResourceDelta delta) {
		System.out.println(delta);
		
		for (IResourceDelta d : delta.getAffectedChildren())
			handleDelta(d);
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;
		
		IResourceDelta delta = event.getDelta();
		if (delta != null)
			handleDelta(delta);
	}
}
