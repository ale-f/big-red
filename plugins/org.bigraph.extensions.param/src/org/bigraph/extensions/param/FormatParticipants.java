package org.bigraph.extensions.param;

import org.bigraph.model.Control;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.loaders.LoaderNotice;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.savers.IXMLSaver;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;
import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;

public abstract class FormatParticipants {
	private FormatParticipants() {}
	
	/**
	 * The XML namespace for the parameterised control extensions.
	 */
	public static final String
			XMLNS = "http://bigraph.org/xmlns/2012/bigraph-extension-param";
	
	public static class Decorator implements IXMLSaver.Decorator {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}

		@Override
		public void decorate(ModelObject object, Element el) {
			if (object instanceof Control) {
				Control c = (Control)object;
				
				INamePolicy parameterPolicy =
						ParameterUtilities.getParameterPolicy(c);
				String policyName = null;
				if (parameterPolicy instanceof LongNamePolicy) {
					policyName = "LONG";
				} else if (parameterPolicy instanceof StringNamePolicy) {
					policyName = "STRING";
				} else if (parameterPolicy instanceof BooleanNamePolicy) {
					policyName = "BOOLEAN";
				}
				if (policyName != null)
					el.setAttributeNS(XMLNS, "param:type", policyName);
			} else if (object instanceof Node) {
				String parameter =
						ParameterUtilities.getParameter((Node)object);
				if (parameter != null)
					el.setAttributeNS(XMLNS, "param:value", parameter);
			}
		}
	}
	
	public static class Undecorator implements IXMLLoader.Undecorator {
		IXMLLoader loader;
		
		@Override
		public void setHost(IParticipantHost host) {
			if (host instanceof IXMLLoader)
				loader = (IXMLLoader)host;
		}

		@Override
		public void undecorate(ModelObject object, Element el) {
			if (object instanceof Control) {
				Control c = (Control)object;
				
				String parameter = getAttributeNS(el, XMLNS, "type");
				if (parameter == null)
					parameter = getAttributeNS(el, SIGNATURE, "parameter");
				if (parameter != null) {
					INamePolicy n = null;
					if (parameter.equals("LONG")) {
						n = new LongNamePolicy();
					} else if (parameter.equals("STRING")) {
						n = new StringNamePolicy();
					} else if (parameter.equals("BOOLEAN")) {
						n = new BooleanNamePolicy();
					}
					if (n != null)
						loader.addChange(
								new ParameterUtilities.ChangeParameterPolicyDescriptor(
										c.getIdentifier(loader.getScratch()),
										null, n));
				}
			} else if (object instanceof Node) {
				Node n = (Node)object;
				INamePolicy policy =
						ParameterUtilities.getParameterPolicy(n.getControl());
				
				String parameter = getAttributeNS(el, XMLNS, "value");
				if (parameter == null)
					parameter = getAttributeNS(el, BIGRAPH, "parameter");
				
				IChangeDescriptor ch = null;
				 /* FIXME - details */
				if (parameter != null && policy == null) {
					loader.addNotice(LoaderNotice.Type.WARNING,
							"Spurious parameter value ignored.");
				} else if (parameter == null && policy != null) {
					loader.addNotice(LoaderNotice.Type.WARNING,
							"Default parameter value assigned.");
					ch = ParameterUtilities.changeParameterDescriptor(
							n.getIdentifier(loader.getScratch()),
							null, policy.get(0));
				} else if (parameter != null && policy != null) {
					ch = ParameterUtilities.changeParameterDescriptor(
							n.getIdentifier(loader.getScratch()),
							null, parameter);
				}
				
				if (ch != null)
					loader.addChange(ch);
			}
		}

		@Override
		public void finish() {
			/* do nothing */
		}
	}
}
