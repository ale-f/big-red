package org.bigraph.model.changes.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;

public class ChangeDescriptorGroup
		implements IChangeDescriptor, Iterable<IChangeDescriptor> {
	private List<IChangeDescriptor> cds =
			new ArrayList<IChangeDescriptor>();
	
	public void prepend(IChangeDescriptor one) {
		cds.add(0, one);
	}
	
	public void add(IChangeDescriptor one) {
		cds.add(one);
	}
	
	public void add(IChangeDescriptor... many) {
		for (IChangeDescriptor one : many)
			cds.add(one);
	}
	
	public void add(Collection<? extends IChangeDescriptor> many) {
		cds.addAll(many);
	}
	
	public IChangeDescriptor head() {
		return cds.get(0);
	}
	
	public ChangeDescriptorGroup tail() {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		cdg.cds = cds.subList(1, size());
		return cdg;
	}
	
	@Override
	public ChangeDescriptorGroup clone() {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		cdg.cds = new ArrayList<IChangeDescriptor>(cds);
		return cdg;
	}
	
	public void remove(IChangeDescriptor one) {
		cds.remove(one);
	}
	
	public IChangeDescriptor remove(int position) {
		return cds.remove(position);
	}
	
	@Override
	public Iterator<IChangeDescriptor> iterator() {
		return cds.iterator();
	}
	
	public IChangeDescriptor get(int position) {
		return cds.get(position);
	}
	
	public IChangeDescriptor set(int position, IChangeDescriptor cd) {
		return cds.set(position, cd);
	}
	
	@Override
	public boolean equals(Object obj_) {
		return
			Layoutable.safeClassCmp(this, obj_) &&
			Layoutable.safeEquals(cds, ((ChangeDescriptorGroup)obj_).cds);
	}
	
	@Override
	public int hashCode() {
		return Layoutable.compositeHashCode(ChangeDescriptorGroup.class, cds);
	}
	
	@Override
	public ChangeGroup createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		ChangeGroup cg = new ChangeGroup();
		context = new PropertyScratchpad(context);
		for (IChangeDescriptor one : cds) {
			Change ch = one.createChange(context, r);
			cg.add(ch);
			ch.simulate(context);
		}
		return cg;
	}
	
	public void clear() {
		cds.clear();
	}
	
	public int size() {
		return cds.size();
	}
	
	@Override
	public String toString() {
		return cds.toString();
	}
}