package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.interfaces.IOuterName;

/**
 * 
 * @author alec
 * @see IOuterName
 */
public class OuterName extends Link implements IOuterName {

	@Override
	public OuterName clone(CloneMap m) {
		return (OuterName)super.clone(m);
	}

	@Override
	public NameType getNameType() {
		return NameType.NAME_ALPHABETIC;
	}

}
