package raven.utilityBox.interfaces;

/**
 * An interface describing an object that can be used as a validator on a given
 * object
 * 
 * @author Raven
 *
 */
public interface IValidator {
	
	/**
	 * Checks whether the given Object is valid.<br>
	 * The message explaining why the validation has failed can be obtained via
	 * {@link #getValidationErrorMessage()}
	 * 
	 * @param obj
	 *            The object to validate
	 */
	public boolean validate(Object obj);
	
	/**
	 * Gets the error message explaining why the previously validated object is
	 * invalid.<br>
	 * If there has not been a validation yet <code>null</code> is returned. If
	 * the validation was successful an empty String is returned
	 */
	public String getValidationErrorMessage();
}
