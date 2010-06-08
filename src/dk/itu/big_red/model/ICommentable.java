package dk.itu.big_red.model;

/**
 * Objects implementing IColourable have a <i>comment</i> that can be
 * manipulated by the user.
 * @author alec
 *
 */
public interface ICommentable {
	/**
	 * The property name implementors should fire (if applicable) when the
	 * comment changes.
	 */
	public static final String PROPERTY_COMMENT = "ICommentableComment";
	
	/**
	 * Returns the current comment for this object.
	 * @return the current comment
	 */
	public String getComment();
	
	/**
	 * Changes this object's comment.
	 * @param comment the new comment
	 */
	public void setComment(String comment);
}
