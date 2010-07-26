package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILayoutable;

public class Site extends Root {
	@Override
	public boolean canContain(ILayoutable child) {
		return false;
	}
	
	public Thing clone() throws CloneNotSupportedException {
		return new Site()._overwrite(this);
	}
}
