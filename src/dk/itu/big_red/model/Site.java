package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.pure.ISite;

public class Site extends Root implements ISite {
	@Override
	public boolean canContain(ILayoutable child) {
		return false;
	}
	
	public Thing clone() throws CloneNotSupportedException {
		return new Site()._overwrite(this);
	}
}
