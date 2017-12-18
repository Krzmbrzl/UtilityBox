package raven.utilityBox.preferences;

/**
 * This class represents an abstract preference and implements the default
 * functionality in terms of saving and accessing
 * 
 * @author Raven
 *
 */
public abstract class AbstractPreference {
	
	public AbstractPreference() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Initializes this preference by loading the respective values
	 */
	public abstract void initialize();
	
	public abstract void initializeWithDefaults();
	
}
