package dk.itu.big_red.model.interfaces;

/**
 * Objects implementing INameable are those which have a <i>name</i>, a unique
 * identifier which the user is allowed to change to anything they like.
 * 
 * <p>The identifier is only required to be unique within a document <i>and</i>
 * a class, so two objects within the same document might have the same name,
 * as long as their classes are also different.
 * @author alec
 *
 */
public interface INameable {
	/**
	 * The property name fired when the name changes.
	 */
	public static final String PROPERTY_NAME = "INameableName";

	/**
	 * Gets this object's name.
	 * @return a String
	 */
	public String getName();
	
	/**
	 * Sets this object's name.
	 * <p>If <code>name</code> is <code>null</code>, then this object will be
	 * given a unique name, if it doesn't already have one.
	 * @param name the new name for this object
	 */
	public void setName(String name);
}
