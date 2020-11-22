package edu.mit.simile.knowle.model;

/**
 * @author ryanlee
 */
public class NonExistentResourceException extends Exception {
	private static final long serialVersionUID = -8143194857202193700L;
    private static String _baseMessage = "Resource is not in model: ";

	/**
	 * @param msg  A <code>String</code> message detailing the exception cause.
	 */
	public NonExistentResourceException(String msg) {
		super(_baseMessage + msg);
	}
}
