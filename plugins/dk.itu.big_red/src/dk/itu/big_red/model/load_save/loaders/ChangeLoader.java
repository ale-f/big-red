package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.LoadFailedException;

import dk.itu.big_red.model.load_save.IChangeLoader;
import dk.itu.big_red.model.load_save.Loader;

public abstract class ChangeLoader extends Loader implements IChangeLoader {
	private ChangeGroup cg = new ChangeGroup();
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	@Override
	public void addChange(IChange c) {
		if (c != null) {
			cg.add(c);
			c.simulate(scratch);
		}
	}
	
	@Override
	public ChangeGroup getChanges() {
		return cg;
	}
	
	@Override
	public PropertyScratchpad getScratch() {
		return scratch;
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
