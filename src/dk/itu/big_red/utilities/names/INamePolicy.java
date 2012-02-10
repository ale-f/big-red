package dk.itu.big_red.utilities.names;

import dk.itu.big_red.utilities.ISafeCloneable;

public interface INamePolicy extends ISafeCloneable {
	public boolean validate(String name);
	
	public String getName(int value);
	
	@Override
	public INamePolicy clone();
}
