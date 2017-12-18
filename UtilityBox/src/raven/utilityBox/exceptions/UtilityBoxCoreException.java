package raven.utilityBox.exceptions;

public class UtilityBoxCoreException extends RuntimeException {
	
	private static final long serialVersionUID = -5255085724642814835L;
	
	/**
	 * Creates a new Exception with the given message
	 * 
	 * @param message
	 *            The message for this exception
	 */
	public UtilityBoxCoreException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with the given cause
	 * 
	 * @param cause
	 *            The cause of this exception
	 */
	public UtilityBoxCoreException(Throwable cause) {
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
	public UtilityBoxCoreException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
