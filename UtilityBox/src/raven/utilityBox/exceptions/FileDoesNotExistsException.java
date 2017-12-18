package raven.utilityBox.exceptions;

/**
 * An exception thrown whenever a file is expected to exist but doesn't
 * 
 * @author Raven
 *
 */
public class FileDoesNotExistsException extends UtilityBoxException {
	
	private static final long serialVersionUID = 4434555576572240729L;
	
	/**
	 * Creates a new Exception with the given message
	 * 
	 * @param message
	 *            The message for this exception
	 */
	public FileDoesNotExistsException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with the given cause
	 * 
	 * @param cause
	 *            The cause of this exception
	 */
	public FileDoesNotExistsException(Throwable cause) {
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
	public FileDoesNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
