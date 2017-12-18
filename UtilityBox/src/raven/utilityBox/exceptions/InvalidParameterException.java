package raven.utilityBox.exceptions;

/**
 * An wexception thrown whenever there is an invalid parameter
 * 
 * @author Raven
 *
 */
public class InvalidParameterException extends UtilityBoxCoreException {
	
	private static final long serialVersionUID = 3111427048839782811L;
	
	/**
	 * Creates a new Exception with the given message
	 * 
	 * @param message
	 *            The message for this exception
	 */
	public InvalidParameterException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with the given cause
	 * 
	 * @param cause
	 *            The cause of this exception
	 */
	public InvalidParameterException(Throwable cause) {
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
	public InvalidParameterException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
