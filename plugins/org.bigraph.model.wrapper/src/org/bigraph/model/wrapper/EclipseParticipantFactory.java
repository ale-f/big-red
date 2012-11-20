package org.bigraph.model.wrapper;

import org.bigraph.model.process.IParticipantFactory;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

abstract class EclipseParticipantFactory implements IParticipantFactory {
	protected static boolean shouldAdd(
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
}
