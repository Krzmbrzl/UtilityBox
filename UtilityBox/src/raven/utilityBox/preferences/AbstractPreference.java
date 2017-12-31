package raven.utilityBox.preferences;

/**
 * This class represents an abstract preference and implements the default
 * functionality in terms of saving and accessing
 * 
 * @author Raven
 *
 */
public abstract class AbstractPreference {

	/**
	 * Initializes this preference by loading the respective values
	 */
	public abstract void initialize();

	/**
	 * Initializes the this preference by loading the default values
	 */
	public abstract void initializeWithDefaults();

}
