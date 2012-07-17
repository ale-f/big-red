package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;

import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.Loader;

public abstract class ChangeLoader extends Loader {
	private ChangeGroup cg = new ChangeGroup();
	
	protected void addChange(IChange c) {
		if (c != null)
			cg.add(c);
	}
	
	protected ChangeGroup getChanges() {
		return cg;
	}
	
	protected void executeChanges(IChangeExecutor ex)
			throws LoadFailedException {
		try {
			ex.tryApplyChange(getChanges());
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
	}
}
