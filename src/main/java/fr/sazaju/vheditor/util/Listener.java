package fr.sazaju.vheditor.util;

/**
 * Generic {@link Listener}.
 * 
 * Rather than providing a single listener class for many types of events, with
 * one method for each type, it is more modular to provide one single listener
 * class per type of event and to make as many classes as needed. Indeed, this
 * pattern provides a single method with a generic name and allows to have a
 * generic {@link Listener} class to extend . This allows to avoid the usual
 * implement-one-and-let-others-empty, which makes the code quite heavy and
 * confusing.
 * 
 * @author sazaju
 * 
 */
public interface Listener {
	/**
	 * Function to execute if the event corresponding to this {@link Listener}
	 * is happening.
	 */
	public void eventGenerated();
}
