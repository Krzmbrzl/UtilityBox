package raven.utilityBox.actions;

import raven.utilityBox.interfaces.IPreferenceSensitiveObject;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.preferences.AbstractPreference;
import raven.utilityBox.preferences.rules.FilenameFormatterRule;

public abstract class AbstractPreferenceSensitiveAction extends AbstractAction implements IPreferenceSensitiveObject {

	/**
	 * The preferences for this formatter
	 */
	private AbstractPreference preferences;

	@Override
	public void setPreferenceRules(AbstractPreference preferences) {
		if (preferences.getClass().equals(getPreferenceType())) {
			this.preferences = (FilenameFormatterRule) preferences;
		} else {
			Logger.getDefault().log(new LogMessage("Invalid preference type. Switching to default behaviour!", this,
					LogMessage.SEVERITY_WARNING));
		}
	}


	@Override
	public AbstractPreference getPreferenceRules() {
		if (preferences == null) {
			preferences = getDefaultPreferences();
		}

		return preferences;
	}

	/**
	 * Gets the default preferences for this action
	 */
	public abstract AbstractPreference getDefaultPreferences();
}
