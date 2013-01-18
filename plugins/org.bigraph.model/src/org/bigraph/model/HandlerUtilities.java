package org.bigraph.model;

import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.process.IParticipantHost;

abstract class HandlerUtilities {
	private HandlerUtilities() {}
	
	abstract static class HandlerImpl
			implements IStepExecutor, IStepValidator {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
	}
	
	abstract static class DescriptorHandlerImpl
			implements IDescriptorStepExecutor, IDescriptorStepValidator {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
	}
	
	static void checkAddBounds(
			IChangeDescriptor cd, List<?> l, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size();
		if (position < 0 || position > l.size())
			throw new ChangeCreationException(cd,
					"" + position + " is not a valid position");
	}
	
	static void checkRemove(
			IChangeDescriptor cd, List<?> l, Object o, int position)
			throws ChangeCreationException {
		checkRemoveBounds(cd, l, position);
		checkRemoveObject(cd, l, o, position);
	}
	
	static void checkRemoveBounds(
			IChangeDescriptor cd, List<?> l, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size() - 1;
		if (position < 0 || position >= l.size())
			throw new ChangeCreationException(cd,
					"" + position + " is not a valid position");
	}
	
	static void checkRemoveObject(
			IChangeDescriptor cd, List<?> l, Object o, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size() - 1;
		Object p = l.get(position);
		if (o != null ? !o.equals(p) : p != null)
			throw new ChangeCreationException(cd,
					"" + o + " is not at position " + position);
	}

	static <V> void checkName(
			PropertyScratchpad context, IChange c, V object,
			Namespace<? extends V> ns, String cdt)
			throws ChangeRejectedException {
		String rationale = checkNameCore(context, object, ns, cdt);
		if (rationale != null)
			throw new ChangeRejectedException(c, rationale);
	}

	static String checkNameCore(
			PropertyScratchpad context, Object object,
			Namespace<?> ns, String cdt) {
		if (cdt == null || cdt.length() == 0)
			return "Names cannot be empty";
		if (ns == null)
			return null;
		INamePolicy p = ns.getPolicy();
		String mcdt = (p != null ? p.normalise(cdt) : cdt);
		if (mcdt == null)
			return "\"" + cdt + "\" is not a valid name for " + object;
		Object current = ns.get(context, mcdt);
		if (current != null && current != object)
			return "Names must be unique";
		return null;
	}
}
