package raven.utilityBox.activator;

import java.util.ArrayList;
import java.util.List;

import raven.utilityBox.actions.AbstractAction;
import raven.utilityBox.actions.ExtractODSToCSVAction;
import raven.utilityBox.actions.FilenameFormatAction;
import raven.utilityBox.actions.Mp3FilenameFormatAction;
import raven.utilityBox.interfaces.IAction;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;

public class Activator {

	protected static Activator instance;

	public static void main(String[] args) {
		// Mp3FilenameFormatAction formatAction = new Mp3FilenameFormatAction(
		// new File(System.getProperty("user.home") + "/Music/Ripped/Mp3"));

		// formatAction.run();

		// ExtractODSToCSVAction extractor = new ExtractODSToCSVAction(
		// System.getProperty("user.home") +
		// "/Documents/Programming/TestingResources/TestSpreadsheet.ods", "CSV");


		// extractor.run();

		args = new String[] { "-ExtractODSToCSVAction", "spreadSheet='" + System.getProperty("user.home")
				+ "/Documents/Programming/TestingResources/TestSpreadsheet.ods'", "targetDir=Test" };

		getDefault().processArguments(args);
	}

	/**
	 * Gets the default instance of this activator
	 */
	public static Activator getDefault() {
		if (instance == null) {
			instance = new Activator();
		}

		return instance;
	}

	/**
	 * The list of registered actions
	 */
	protected List<Class<? extends IAction>> actions;


	public Activator() {
		registerActions();
	}

	/**
	 * Processes the given arguments and invokes the respective actions with the
	 * corresponding parameter
	 * 
	 * @param args
	 *            The argument array to process
	 */
	protected void processArguments(String[] args) {
		IAction target = null;
		List<String> parameter = new ArrayList<String>();

		for (String currentArgument : args) {
			if (currentArgument.matches("\".*?\"") || currentArgument.matches("'.*?'")) {
				// remove quotes
				currentArgument = currentArgument.substring(1, currentArgument.length() - 1);
			}

			currentArgument = currentArgument.trim();

			if (currentArgument.isEmpty()) {
				continue;
			}

			if (currentArgument.startsWith("-") && target != null) {
				// run "old" action and prepare for new target
				setParameter(target, parameter);
				target.run();

				target = null;
			}

			if (target == null) {
				// Firstly the target action has to be determined
				if (!currentArgument.startsWith("-")) {
					Logger.getDefault().log(new LogMessage(
							"Expected target-action-definition. Discarding argument \"" + currentArgument + "\"", this,
							LogMessage.SEVERITY_ERROR));

					continue;
				}
				currentArgument = currentArgument.substring(1).trim();

				for (Class<? extends IAction> currentActionClass : actions) {
					if (currentActionClass.getSimpleName().toLowerCase().equals(currentArgument.toLowerCase())) {
						try {
							target = currentActionClass.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();

							Logger.getDefault().log(e, this);
						}

						break;
					}
				}

				// check that a class has been found
				if (target == null) {
					Logger.getDefault().log(new LogMessage("Unknown action \"" + currentArgument + "\"!", this,
							LogMessage.SEVERITY_ERROR));
				}

				continue;
			}

			// At this point the target has been set
			// process key-value arguments
			if (!currentArgument.contains("=")) {
				Logger.getDefault().log(new LogMessage("Expected key-value pair but got \"" + currentArgument + "\"!",
						this, LogMessage.SEVERITY_ERROR));
				continue;
			}

			String key = currentArgument.substring(0, currentArgument.indexOf("=")).trim();
			String value = currentArgument.substring(currentArgument.indexOf("=") + 1).trim();

			if (key.matches("\".*?\"") || key.matches("'.*?'")) {
				key = key.substring(1, key.length() - 1).trim();
			}
			if (value.matches("\".*?\"") || value.matches("'.*?'")) {
				value = value.substring(1, value.length() - 1).trim();
			}

			// make sure there are actual values in key + value
			if (key.isEmpty()) {
				Logger.getDefault().log(
						new LogMessage("Empty key in \"" + currentArgument + "\"!", this, LogMessage.SEVERITY_ERROR));
				continue;
			}
			if (value.isEmpty()) {
				Logger.getDefault().log(
						new LogMessage("Empty value in \"" + currentArgument + "\"!", this, LogMessage.SEVERITY_ERROR));
				continue;
			}

			int index = target.getParameterIndex(key);

			// make sure the key is known
			if (index < 0) {
				Logger.getDefault().log(new LogMessage("Unknown key \"" + key + "\" for " + target.getName() + "!",
						this, LogMessage.SEVERITY_ERROR));
				continue;
			}

			parameter.add(index, value);
		}

		// run last action
		if (target != null) {
			setParameter(target, parameter);
			target.run();
		}
	}

	/**
	 * Sets the given parameters to the given action
	 * 
	 * @param action
	 *            The action to set the parameter to
	 * @param parameter
	 *            The parameter to set
	 */
	private void setParameter(IAction action, List<String> parameter) {
		if (action instanceof AbstractAction) {
			((AbstractAction) action).setRawParameter(parameter.toArray(new String[parameter.size()]));
		} else {
			action.setParameter(parameter.toArray(new String[parameter.size()]));
		}
	}

	/**
	 * Registers all available actions
	 */
	protected void registerActions() {
		actions = new ArrayList<Class<? extends IAction>>();

		actions.add(FilenameFormatAction.class);
		actions.add(Mp3FilenameFormatAction.class);
		actions.add(ExtractODSToCSVAction.class);
	}
}
