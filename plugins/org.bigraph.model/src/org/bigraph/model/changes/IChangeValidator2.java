package org.bigraph.model.changes;

import org.bigraph.model.assistants.PropertyScratchpad;

public interface IChangeValidator2 {
	interface Process {
		PropertyScratchpad getScratch();
	}
	
	boolean tryValidateChange(Process context, IChange change)
			throws ChangeRejectedException;
}
