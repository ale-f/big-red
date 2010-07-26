package dk.itu.big_red.editors.assistants;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Canvas;

/**
 * PointListeners are used to subscribe to change notifications from a
 * {@link SignatureEditorPolygonCanvas}.
 * 
 * <p>While they <i>are</i> event listeners for a SWT widget (specifically, an
 * extended {@link Canvas}, they're <i>not</i> SWTEventListeners, because that
 * class isn't part of the public API.
 * @author alec
 *
 */
public interface PointListener {
	/**
	 * Instances of this class are sent as a result of points being added or
	 * removed from a {@link SignatureEditorPolygonCanvas}.
	 * @author alec
	 *
	 */
	public class PointEvent {
		/**
		 * The {@link SignatureEditorPolygonCanvas} responsible for generating
		 * this event.
		 */
		SignatureEditorPolygonCanvas source;
		
		/**
		 * The Point just added to the polygon, if any.
		 * 
		 * <p>If both <code>added</code> and {@link #removed} are null, then
		 * all points have been removed.
		 */
		Point added;
		
		/**
		 * The Point just removed from the polygon, if any.
		 * 
		 * <p>If both {@link #added} and <code>removed</code> are null, then
		 * all points have been removed.
		 */
		Point removed;
	}
	
	/**
	 * Sent when a point is added or removed.
	 * <p>If <i>all</i> points are removed, then both <code>e.added</code> and
	 * <code>e.removed</code> will be <code>null</code>.
	 * @param e a {@link PointEvent}
	 */
	public void pointChange(PointEvent e);
}
