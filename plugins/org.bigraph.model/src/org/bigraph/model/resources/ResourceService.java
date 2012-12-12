package org.bigraph.model.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;

public class ResourceService {
	public interface ResourceRecord {
		ModelObject getObject() throws LoadFailedException;
	}
	
	private static final class Holder {
		private static final ResourceService INSTANCE = new ResourceService();
	}
	
	private HashMap<IFileWrapper, FileRecord> records =
			new HashMap<IFileWrapper, FileRecord>();
	
	private static final IFileWrapper getFile(ModelObject m) {
		return FileData.getFile(m);
	}
	
	private final class FileRecord implements ResourceRecord {
		private final IFileWrapper file;
		private LoadFailedException lastError;
		private ModelObject object;
		private Set<FileRecord>
			dependencies,
			dependents = new HashSet<FileRecord>();
		
		private FileRecord(IFileWrapper file) {
			this.file = file;
			update();
		}
		
		public IFileWrapper getFile() {
			return file;
		}
		
		@Override
		public ModelObject getObject() throws LoadFailedException {
			if (lastError != null)
				throw lastError;
			return object;
		}
		
		private Set<FileRecord> calculateDependencies() {
			if (lastError != null)
				return Collections.emptySet();
			Set<FileRecord> deps = new HashSet<FileRecord>();
			
			if (object instanceof Bigraph) {
				Bigraph b = (Bigraph)object;
				deps.add(getRecord(b.getSignature()));
			} else if (object instanceof Signature) {
				Signature s = (Signature)object;
				for (Signature i : s.getSignatures())
					deps.add(getRecord(i));
			} else if (object instanceof ReactionRule) {
				ReactionRule r = (ReactionRule)object;
				deps.add(getRecord(r.getRedex().getSignature()));
			} else if (object instanceof SimulationSpec) {
				SimulationSpec ss = (SimulationSpec)object;
				deps.add(getRecord(ss.getModel()));
				deps.add(getRecord(ss.getSignature()));
				for (ReactionRule i : ss.getRules())
					deps.add(getRecord(ResourceService.getFile(i)));
			}
			deps.remove(null);
			
			return deps;
		}
		
		private Set<FileRecord> getDependents() {
			return dependents;
		}
		
		private LoadFailedException load() {
			try {
				object = file.load();
				lastError = null;
			} catch (LoadFailedException lfe) {
				object = null;
				lastError = lfe;
			}
			return lastError;
		}
		
		private void update() {
			try {
				/* If loading fails, preserve the old dependencies -- perhaps
				 * one of them will be able to save us */
				if (load() != null)
					return;
				
				Set<FileRecord> newDeps = calculateDependencies();
				if (!newDeps.equals(dependencies)) {
					if (dependencies != null) {
						Set<FileRecord> droppedDeps =
								new HashSet<FileRecord>(dependencies);
						droppedDeps.removeAll(newDeps);
						for (FileRecord i : droppedDeps)
							i.getDependents().remove(this);
						dependencies.clear();
						dependencies = null;
					}
					
					for (FileRecord i : newDeps)
						i.getDependents().add(this);
					
					System.out.println("Dependency set of " + file +
							" updated: " + newDeps);
					dependencies = newDeps;
				}
			} finally {
				for (FileRecord i : getDependents())
					i.update();
			}
		}
		
		@Override
		public String toString() {
			return "FileRecord(" + getFile() + ")";
		}
	}
	
	public static ResourceService getInstance() {
		return Holder.INSTANCE;
	}
	
	public void update(IFileWrapper resource) {
		getRecord(resource).update();
	}
	
	protected FileRecord getRecord(ModelObject mo) {
		return getRecord(FileData.getFile(mo));
	}
	
	protected FileRecord getRecord(IFileWrapper resource) {
		if (resource == null)
			return null;
		FileRecord r = records.get(resource);
		if (r == null)
			records.put(resource, r = new FileRecord(resource));
		return r;
	}
	
	public ModelObject getResource(IFileWrapper resource)
			throws LoadFailedException {
		return getResource(resource, ModelObject.class);
	}
	
	public <T extends ModelObject> T getResource(
			IFileWrapper resource, Class<? extends T> klass)
			throws LoadFailedException {
		ModelObject mo = getRecord(resource).getObject();
		if (mo instanceof Bigraph) {
			mo = ((Bigraph)mo).clone();
		} else if (mo instanceof Signature) {
			mo = ((Signature)mo).clone();
		} else if (mo instanceof ReactionRule) {
			mo = ((ReactionRule)mo).clone();
		} else if (mo instanceof SimulationSpec) {
			mo = ((SimulationSpec)mo).clone();
		} else mo = null;
		return ModelObject.require(mo, klass);
	}
}
