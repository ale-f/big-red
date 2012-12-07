package org.bigraph.model.tests;

import org.bigraph.model.Container;
import org.bigraph.model.Edit;
import org.bigraph.model.Layoutable;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.DescriptorExecutorManager;
import org.junit.Test;

import static org.bigraph.model.tests.BigraphDescriptorTests.cdg;

public class EditDescriptorTests {
	@Test
	public void addDescriptor() throws ChangeCreationException {
		Edit ed = new Edit();
		IChangeDescriptor dummy =
				new Layoutable.ChangeRemoveDescriptor(null, null);
		DescriptorExecutorManager.getInstance().tryApplyChange(ed,
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy));
	}
	
	@Test
	public void removeAddedDescriptor() throws ChangeCreationException {
		Edit ed = new Edit();
		IChangeDescriptor dummy =
				new Layoutable.ChangeRemoveDescriptor(null, null);
		DescriptorExecutorManager.getInstance().tryApplyChange(ed, cdg(
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy),
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy)));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentDescriptor() throws ChangeCreationException {
		Edit ed = new Edit();
		IChangeDescriptor dummy =
				new Layoutable.ChangeRemoveDescriptor(null, null);
		DescriptorExecutorManager.getInstance().tryApplyChange(ed,
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeIncorrectDescriptor() throws ChangeCreationException {
		Edit ed = new Edit();
		IChangeDescriptor
			dummy =
				new Layoutable.ChangeRemoveDescriptor(null, null),
			dummy2 =
				new Container.ChangeAddChildDescriptor(null, null);
		DescriptorExecutorManager.getInstance().tryApplyChange(ed, cdg(
				new Edit.ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), 0, dummy),
				new Edit.ChangeDescriptorRemoveDescriptor(
						new Edit.Identifier(), 0, dummy2)));
	}
}
