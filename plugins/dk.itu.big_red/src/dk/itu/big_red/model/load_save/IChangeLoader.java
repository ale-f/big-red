package dk.itu.big_red.model.load_save;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;


public interface IChangeLoader extends ILoader {
	void addChange(IChange c);
	ChangeGroup getChanges();
	PropertyScratchpad getScratch();
}
