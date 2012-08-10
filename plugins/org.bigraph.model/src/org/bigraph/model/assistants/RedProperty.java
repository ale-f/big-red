package org.bigraph.model.assistants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Fields annotated with <strong>RedProperty</strong> are <i>property
 * names</i>.
 * @author alec
 */
@Target(ElementType.FIELD)
public @interface RedProperty {
	Class<?> fired();
	Class<?> retrieved();
}
