package raven.utilityBox.preferences.rules;

import raven.utilityBox.preferences.AbstractPreference;

public class ExtractODSToCSVRule extends AbstractPreference {

	/**
	 * The sequence that should delimit columns in the generated CSV
	 */
	private String columnDelimiter;
	/**
	 * The replacement any occurring {@link #columnDelimiter} in the content of the
	 * CSV will be replaced with
	 */
	private String delimiterReplacement;

	@Override
	public void initialize() {
		// TODO

	}

	@Override
	public void initializeWithDefaults() {
		columnDelimiter = "\t";
		delimiterReplacement = "    ";
	}

	/**
	 * Gets the sequence that should be used as the column delimiter in the
	 * generated CSV
	 */
	public String getColumnDelimiter() {
		return columnDelimiter;
	}

	/**
	 * Gets the sequence that should delimit columns in the generated CSV
	 */
	public String getDelimiterReplacement() {
		return delimiterReplacement;
	}

}
