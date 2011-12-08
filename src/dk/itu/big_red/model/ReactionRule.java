package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.resources.IFileBackable;

public class ReactionRule extends ModelObject implements IFileBackable {
	private Bigraph redex, reactum;
	private Map<ModelObject, ModelObject> redexToReactum =
		new HashMap<ModelObject, ModelObject>();
	private ChangeGroup changes;
	
	public static final String PROPERTY_REDEX = "ReactionRuleRedex";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		Bigraph old = this.redex;
		this.redex = redex;
		
		reactum = null;
		redexToReactum.clear();
		
		firePropertyChange(PROPERTY_REDEX, old, redex);
	}

	public Bigraph getReactum() {
		if (reactum == null)
			reactum = redex.clone(redexToReactum);
		return reactum;
	}
	
	public Map<ModelObject, ModelObject> getRedexToReactumMap() {
		return redexToReactum;
	}
	
	protected IFile file = null;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public ReactionRule setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	public ChangeGroup getChanges() {
		if (changes == null)
			changes = new ChangeGroup();
		return changes;
	}
}
