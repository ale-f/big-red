package dk.itu.big_red.editors.assistants;

import org.eclipse.swt.widgets.Canvas;

import dk.itu.big_red.model.Port;

/**
 * PortListeners are used to subscribe to change notifications from a
 * {@link SignatureEditorPolygonCanvas}.
 * 
 * <p>While they <i>are</i> event listeners for a SWT widget (specifically, an
 * extended {@link Canvas}, they're <i>not</i> SWTEventListeners, because that
 * class isn't part of the public API.
 * @author alec
 *
 */
public interface PortListener {
	/**
	 * Instances of this class are sent as a result of ports being added to,
	 * removed from, or moved or renamed on a {@link
	 * SignatureEditorPolygonCanvas}.
	 * @author alec
	 *
	 */
	public class PortEvent {
		/**
		 * The {@link SignatureEditorPolygonCanvas} responsible for generating
		 * this event.
		 */
		SignatureEditorPolygonCanvas source;
		
		/**
		 * A port has been added.
		 */
		public static int ADDED = 1;
		
		/**
		 * A port has been removed.
		 */
		public static int REMOVED = 2;
		
		/**
		 * A port has been moved.
		 */
		public static int MOVED = 3;
		
		/**
		 * The type of change that occurred ({@link #ADDED}, {@link #REMOVED}
		 * or {@link #MOVED}).
		 */
		int type;
		
		/**
		 * The Port which caused this event to be fired.
		 * 
		 * <p>If this property is <code>null</code>, then the event represents
		 * a mass change to all Ports.
		 */
		Port object;
	}
	
	/**
	 * Sent when a point has changed.
	 * @param e a {@link PortEvent}
	 */
	public void portChange(PortEvent e);
}
