package org.bigraph.model.savers;

import java.util.List;

import org.bigraph.model.savers.Saver.ISaverOption;

public interface ISaver {
	void addOption(ISaverOption o);
	List<? extends ISaverOption> getOptions();
}
