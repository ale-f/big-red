package org.bigraph.model.wrapper;

import org.bigraph.model.process.IParticipant;
import org.bigraph.model.process.IParticipantFactory;
import org.bigraph.model.process.IParticipantHost;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

class EclipseParticipantFactory implements IParticipantFactory {
	private final String extensionPoint;
	
	EclipseParticipantFactory(String extensionPoint) {
		this.extensionPoint = extensionPoint;
	}
	
	static boolean shouldAdd(
			Object defaultVariable, IConfigurationElement ice)
			throws CoreException {
		IConfigurationElement[] enablement = ice.getChildren("enablement");
		if (enablement.length == 1) {
			Expression ex = ExpressionConverter.getDefault().perform(
					enablement[0]);
			return (ex.evaluate(
						new EvaluationContext(null, defaultVariable)) ==
					EvaluationResult.TRUE);
		} else return true;
	}
	
	@Override
	public void addParticipants(IParticipantHost host) {
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
				r.getConfigurationElementsFor(extensionPoint)) {
			if ("participant".equals(ice.getName())) {
				try {
					if (shouldAdd(host, ice))
						host.addParticipant((IParticipant)
								ice.createExecutableExtension("class"));
				} catch (CoreException e) {
					e.printStackTrace();
					/* do nothing */
				}
			}
		}
	}
}
