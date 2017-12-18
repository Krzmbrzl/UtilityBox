package raven.utilityBox.interfaces;

import raven.utilityBox.preferences.AbstractPreference;

/**
 * This interface decribes an object whose behaviour can be specified via
 * preferences
 * 
 * @author Raven
 *
 */
public interface IPreferenceSensitiveObject {
	
	/**
	 * Sets the preference for this object. The given preference has to be an
	 * instance of {@link #getPreferenceType()}
	 * 
	 * @param preference
	 *            The respective preference to use
	 */
	public void setPreferenceRules(AbstractPreference preference);
	
	/**
	 * Gets the preference type this object expects
	 */
	public Class<?> getPreferenceType();
	
	/**
	 * Gets the set preferences
	 */
	public AbstractPreference getPreferenceRules();
}
