package raven.utilityBox.interfaces;

import raven.utilityBox.enums.EStatus;

/**
 * An interface describing an Action
 * 
 * @author Raven
 *
 */
public interface IAction {

	/**
	 * Runs this action
	 * 
	 * @return The resulting status of this action
	 */
	public EStatus run();

	/**
	 * Sets the parameters for this action.
	 * 
	 * @param args
	 *            The parameters to pass
	 */
	public void setParameter(Object[] args);

	/**
	 * Validates the set parameter
	 * 
	 * @param args
	 *            the parameter to validate
	 * 
	 * @return Whether the given parameter are valid
	 */
	public boolean validateParameter(Object[] args);

	/**
	 * Gets the parameter index for the specified parameter
	 * 
	 * @param keyValue
	 *            The parameter-key that should be processed
	 * @return The index the value corresponding to this key should take in the
	 *         parameter array passed to this class or <code>-1</code> if none could
	 *         be found
	 */
	public int getParameterIndex(String key);

	/**
	 * Makes sure that each index of the parameter array is of the right data type
	 * by converting all parameter that shouldn't be Strings to the respective types
	 * 
	 * @param inputArray
	 *            The String input-array
	 * @return The generated parameter array that contains the proper types
	 */
	public Object[] generateParameterArray(String[] inputArray);

	/**
	 * Gets the name of this Action
	 */
	public String getName();

	/**
	 * Gets the ID of this Action
	 */
	public int getID();
}
