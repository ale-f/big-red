package dk.itu.big_red.model.interfaces;

/**
 * Objects implementing IColourable have a <i>comment</i> that can be
 * manipulated by the user.
 * @author alec
 *
 */
public interface ICommentable extends IPropertyChangeNotifier {
	/**
	 * The property name fired when the comment changes.
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
