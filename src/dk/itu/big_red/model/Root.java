package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILayoutable;

public class Root extends Thing {
	@Override
	public Thing clone() throws CloneNotSupportedException {
		return new Root()._overwrite(this);
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	private int number;
	
	public void setNumber(int number) {
		String oldNumber = Integer.toString(number);
		this.number = number;
		listeners.firePropertyChange(PROPERTY_RENAME, oldNumber, Integer.toString(number));
	}
	
	public int getNumber() {
		return number;
	}
}
