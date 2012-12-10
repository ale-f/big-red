package org.bigraph.model.tests;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.junit.Test;

public class SpecDescriptorTests extends DescriptorTestRunner {
	private static SimulationSpec constructSpec()
			throws ChangeCreationException {
		ReactionRule
			rr1 = new ReactionRule(),
			rr2 = new ReactionRule();
		Signature s = new Signature();
		Bigraph b = new Bigraph();
		
		SimulationSpec ss = new SimulationSpec();
		SimulationSpec.Identifier ssI = new SimulationSpec.Identifier();
		run(ss,
				new SimulationSpec.ChangeSetModelDescriptor(
						ssI, null, b),
				new SimulationSpec.ChangeSetSignatureDescriptor(
						ssI, null, s),
				new SimulationSpec.ChangeAddRuleDescriptor(
						ssI, 0, rr2),
				new SimulationSpec.ChangeAddRuleDescriptor(
						ssI, 0, rr1));
		return ss;
	}
	
	@Test
	public void buildSpec() throws ChangeCreationException {
		constructSpec();
	}
}
