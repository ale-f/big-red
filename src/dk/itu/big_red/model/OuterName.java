package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IOuterName;

public class OuterName extends Link implements IOuterName {

	@Override
	public OuterName clone() {
		return new OuterName();
	}

}
