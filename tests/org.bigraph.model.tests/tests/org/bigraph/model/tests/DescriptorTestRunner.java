package org.bigraph.model.tests;

import java.util.Arrays;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class DescriptorTestRunner {
	static ChangeDescriptorGroup cdg(IChangeDescriptor... changes) {
		return new ChangeDescriptorGroup(Arrays.asList(changes));
	}
	
	static void run(Resolver r, IChangeDescriptor... cds)
			throws ChangeCreationException {
		run(r, cdg(cds));
	}
	
	static void run(Resolver r, ChangeDescriptorGroup cdg)
			throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(r, cdg);
	}
}
