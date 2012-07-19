package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;

public interface IChangeLoader {
	void addChange(IChange c);
	ChangeGroup getChanges();
}
