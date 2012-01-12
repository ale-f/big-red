package dk.itu.big_red.editors.signature;

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
	 * Instances of this class are sent as a result of points being added to,
	 * removed from, or moved on a {@link SignatureEditorPolygonCanvas}.
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
		 * A point has been added.
		 */
		public static int ADDED = 1;
		
		/**
		 * A point has been removed.
		 */
		public static int REMOVED = 2;
		
		/**
		 * A point has been moved.
		 */
		public static int MOVED = 3;
		
		/**
		 * The type of change that occurred ({@link #ADDED}, {@link #REMOVED}
		 * or {@link #MOVED}).
		 */
		int type;
		
		/**
		 * The Point which caused this event to be fired.
		 */
		Point object;
	}
	
	/**
	 * Sent when a point has changed.
	 * @param e a {@link PointEvent}
	 */
	public void pointChange(PointEvent e);
}
