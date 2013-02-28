package dk.itu.big_red.editors.rule;

import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

abstract class DescriptorUtilities {
	private DescriptorUtilities() {}
	
	static ChangeDescriptorGroup linearise(IChangeDescriptor cd) {
		return linearise(cd, null);
	}
	
	static ChangeDescriptorGroup linearise(
			IChangeDescriptor cd, ChangeDescriptorGroup cdg) {
		if (cdg == null)
			cdg = new ChangeDescriptorGroup();
		if (cd instanceof IChangeDescriptor.Group) {
			for (IChangeDescriptor i : (IChangeDescriptor.Group)cd)
				linearise(i, cdg);
		} else cdg.add(cd);
		return cdg;
	}
}
