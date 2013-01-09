package org.bigraph.model.tests;

import org.bigraph.model.Container;
import org.bigraph.model.Edit;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.junit.Test;

public class EditDescriptorTests extends DescriptorTestRunner {
	@Test
	public void addDescriptor() throws ChangeCreationException {
		IChangeDescriptor dummy =
				new Container.ChangeRemoveChildDescriptor(null, null);
		run(new Edit(),
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy));
	}
	
	@Test
	public void removeAddedDescriptor() throws ChangeCreationException {
		IChangeDescriptor dummy =
				new Container.ChangeRemoveChildDescriptor(null, null);
		run(new Edit(),
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy),
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentDescriptor() throws ChangeCreationException {
		IChangeDescriptor dummy =
				new Container.ChangeRemoveChildDescriptor(null, null);
		run(new Edit(),
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeIncorrectDescriptor() throws ChangeCreationException {
		IChangeDescriptor
			dummy =
				new Container.ChangeRemoveChildDescriptor(null, null),
			dummy2 =
				new Container.ChangeAddChildDescriptor(null, null);
		run(new Edit(),
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy),
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy2));
	}
}
