package dk.itu.big_red.editors.rule;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Container.ChangeAddChild;
import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Layoutable.ChangeRemove;
import org.bigraph.model.Layoutable.ChangeRemoveDescriptor;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ChangeExtendedDataDescriptor;
import org.bigraph.model.Point.ChangeConnect;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnect;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
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
		if (cd instanceof ChangeDescriptorGroup) {
			for (IChangeDescriptor i : (ChangeDescriptorGroup)cd)
				linearise(i, cdg);
		} else cdg.add(cd);
		return cdg;
	}
	
	/**
	 * Converts an {@link IChange} that hasn't been applied yet into an {@link
	 * IChangeDescriptor}.
	 * @param c the {@link IChange} to convert
	 * @return an {@link IChangeDescriptor}, or <code>null</code> in the event
	 * of a conversion error
	 * @see #createDescriptor(PropertyScratchpad, IChange)
	 */
	static IChangeDescriptor createDescriptor(IChange c) {
		return createDescriptor(null, c);
	}
	
	/**
	 * Converts an {@link IChange} into an {@link IChangeDescriptor}.
	 * @param context a {@link PropertyScratchpad} representing an appropriate
	 * initial model state; can be <code>null</code>
	 * @param c the {@link IChange} to convert
	 * @return an {@link IChangeDescriptor}, or <code>null</code> in the event
	 * of a conversion error
	 * @see #createDescriptor(IChange)
	 */
	static IChangeDescriptor createDescriptor(
			PropertyScratchpad context, IChange c) {
		IChangeDescriptor chd = null;
		if (c instanceof ChangeGroup) {
			ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
			context = new PropertyScratchpad(context);
			for (IChange ch : (ChangeGroup)c) {
				chd = createDescriptor(context, ch);
				if (chd != null) {
					cdg.add(chd);
				} else {
					cdg.clear();
					return null;
				}
			}
			/* All changes will have been simulated */
			return cdg;
		} else if (c instanceof ChangeExtendedData) {
			ChangeExtendedData ch = (ChangeExtendedData)c;
			Layoutable creator = (Layoutable)ch.getCreator();
			Object oldValue = creator.getExtendedData(context, ch.key);
			chd = new ChangeExtendedDataDescriptor(
					creator.getIdentifier(context),
					ch.key, oldValue, ch.newValue,
					ch.validator, ch.normaliser);
		} else if (c instanceof ChangeRemove) {
			ChangeRemove ch = (ChangeRemove)c;
			chd = new ChangeRemoveDescriptor(
					ch.getCreator().getIdentifier(context),
					ch.getCreator().getParent(context).getIdentifier(context));
		} else if (c instanceof ChangeConnect) {
			ChangeConnect ch = (ChangeConnect)c;
			chd = new ChangeConnectDescriptor(
					ch.getCreator().getIdentifier(context),
					ch.link.getIdentifier(context));
		} else if (c instanceof ChangeDisconnect) {
			ChangeDisconnect ch = (ChangeDisconnect)c;
			chd = new ChangeDisconnectDescriptor(
					ch.getCreator().getIdentifier(context),
					ch.getCreator().getLink(context).getIdentifier(context));
		} else if (c instanceof ChangeAddChild) {
			ChangeAddChild ch = (ChangeAddChild)c;
			chd = new ChangeAddChildDescriptor(
					ch.getCreator().getIdentifier(context),
					/* The new child's name should be null at this point */
					ch.child.getIdentifier(context).getRenamed(ch.name));
		} else if (c instanceof BoundDescriptor) {
			chd = ((BoundDescriptor)c).getDescriptor();
		}
		if (context != null)
			c.simulate(context);
		return chd;
	}
}
