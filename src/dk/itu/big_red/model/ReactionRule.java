package dk.itu.big_red.model;

import org.eclipse.core.resources.IFile;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.resources.IFileBackable;

public class ReactionRule extends ModelObject implements IFileBackable {
	private Bigraph redex;
	private ChangeGroup changes;
	
	public static final String PROPERTY_REDEX = "ReactionRuleRedex";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		Bigraph old = this.redex;
		this.redex = redex;
		firePropertyChange(PROPERTY_REDEX, old, redex);
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
