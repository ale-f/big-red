package dk.itu.big_red.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class BigraphBuilder extends IncrementalProjectBuilder {
	class DeltaVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				break;
			case IResourceDelta.REMOVED:
				break;
			case IResourceDelta.CHANGED:
				break;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "(DeltaVisitor for " + BigraphBuilder.this + ")";
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		@Override
		public boolean visit(IResource resource) {
			return true;
		}
		
		@Override
		public String toString() {
			return "(ResourceVisitor for " + BigraphBuilder.this + ")";
		}
	}

	public static final String BUILDER_ID =
			"dk.itu.big_red.utilities.resources.builder.BigraphBuilder";

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		getProject().accept(new ResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		delta.accept(new DeltaVisitor());
	}
	
	@Override
	public String toString() {
		return "(BigraphBuilder@" + Integer.toString(hashCode(), 16) +
				" for " + getProject() + ")";
	}
}
