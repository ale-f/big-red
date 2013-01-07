package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.INamePolicy;

final class NamedModelObjectHandler {
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
