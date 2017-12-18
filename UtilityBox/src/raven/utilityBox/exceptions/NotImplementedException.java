package raven.utilityBox.exceptions;

/**
 * An exception thrown when the requested feature is not yet implemented
 * 
 * @author Raven
 *
 */
public class NotImplementedException extends UtilityBoxCoreException {
	
	private static final long serialVersionUID = 2371458425849789082L;
	
	/**
	 * Creates a new Exception with the given message
	 * 
	 * @param message
	 *            The message for this exception
	 */
	public NotImplementedException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with the given cause
	 * 
	 * @param cause
	 *            The cause of this exception
	 */
	public NotImplementedException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates a new exception
	 * 
	 * @param message
	 *            The message of this exception
	 * @param cause
	 *            The cause of this exception
	 */
	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
