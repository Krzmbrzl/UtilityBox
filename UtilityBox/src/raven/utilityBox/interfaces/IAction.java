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
	 * @return Whether the given parameter are valid
	 */
	public boolean validateParameter();
}
