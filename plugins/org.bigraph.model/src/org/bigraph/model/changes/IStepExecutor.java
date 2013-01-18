package org.bigraph.model.changes;

import org.bigraph.model.process.IParticipant;

public interface IStepExecutor extends IParticipant {
	boolean executeChange(IChange change_);
}
