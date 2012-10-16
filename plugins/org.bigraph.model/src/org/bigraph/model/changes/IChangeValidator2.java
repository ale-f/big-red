package org.bigraph.model.changes;

import org.bigraph.model.assistants.PropertyScratchpad;

public interface IChangeValidator2 {
	interface Callback {
		void run() throws ChangeRejectedException;
	}
	
	interface Process {
		PropertyScratchpad getScratch();
		void addCallback(Callback c);
	}
	
	boolean tryValidateChange(Process context, IChange change)
			throws ChangeRejectedException;
}
