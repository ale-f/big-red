package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;

import dk.itu.big_red.model.load_save.ILoader;

public interface IChangeLoader extends ILoader {
	void addChange(IChange c);
	ChangeGroup getChanges();
}
