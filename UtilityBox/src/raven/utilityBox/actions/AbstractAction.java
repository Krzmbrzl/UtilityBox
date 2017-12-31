package raven.utilityBox.actions;

import raven.utilityBox.enums.EStatus;
import raven.utilityBox.exceptions.InvalidParameterException;
import raven.utilityBox.interfaces.IAction;
import raven.utilityBox.interfaces.ITypeConverter;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;

/**
 * An abstract implementation of an {@link IAction}
 * 
 * @author Raven
 *
 */
public abstract class AbstractAction implements IAction {

	/**
	 * The ID of the Action that will be instantiated next
	 */
	private static int NEXT_ID = 0;

	/**
	 * The arguments for this action
	 */
	protected Object[] args;
	/**
	 * The name of this action
	 */
	protected String name;

	/**
	 * The ID of this action
	 */
	protected final int ID;


	/**
	 * Creates a new action with the class name as the name of this action
	 */
	public AbstractAction() {
		this.name = getClass().getSimpleName();
		this.ID = NEXT_ID;

		NEXT_ID++;
	}

	/**
	 * Creates a new instance of this Action
	 * 
	 * @param name
	 *            The name of this Action
	 */
	public AbstractAction(String name) {
		assert (name != null);

		this.name = name;
		this.ID = NEXT_ID;

		NEXT_ID++;
	}

	public abstract Object[] getDefaultParameter();


	@Override
	public void setParameter(Object[] args) {
		if (args == null) {
			// Log and switch to default parameter
			Logger.getDefault().log(new LogMessage("Invalid null parameter! Switching to default parameter...", this,
					LogMessage.SEVERITY_WARNING));

			args = getDefaultParameter();
		}

		Object[] newParams = getDefaultParameter();

		// make sure there are no null-values in between and all parameter are included
		for (int i = 0; i < newParams.length; i++) {
			if (i < args.length || args[i] != null) {
				newParams[i] = args[i];
			}
		}

		if (!validateParameter(args)) {
			Logger.getDefault()
					.log(new LogMessage("The given parameter are invalid!", this, LogMessage.SEVERITY_ERROR));

			throw new InvalidParameterException("The given parameter are not valid!");
		}

		this.args = newParams;

		applyParameter(newParams);
	}

	/**
	 * Sets the parameter for this action. The input array consists of Strings that
	 * will get converted to the respective data type by the class's own
	 * type-converters
	 * 
	 * @param parameter
	 *            The raw String parameter to set
	 */
	public void setRawParameter(String[] parameter) {
		setParameter(generateParameterArray(parameter));
	}

	/**
	 * Checks whether this Action has parameter set
	 */
	public boolean hasParameter() {
		return args != null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getID() {
		return ID;
	}

	@Override
	public final EStatus run() {
		if (args == null && requiresParameter()) {
			Logger.getDefault()
					.log(new LogMessage("Necessary parameter have not been set!", this, LogMessage.SEVERITY_ERROR));

			return EStatus.ERROR;
		}

		Logger.getDefault().log(new LogMessage("Starting " + getName() + "...", null, LogMessage.SEVERITY_INFO));

		EStatus status = doRun();

		Logger.getDefault().log(new LogMessage("Completed " + getName(), null, LogMessage.SEVERITY_INFO));

		return status;
	}

	/**
	 * Executes the code associated with this action.
	 * 
	 * @return The status after having completed the code
	 */
	protected abstract EStatus doRun();

	/**
	 * Applies the provided parameter so that they can be used later on. When
	 * calling this method the respective parameters have already been validated.
	 */
	protected abstract void applyParameter(Object[] args);

	@Override
	public int getParameterIndex(String key) {
		String[] keys = getParameterKeys();

		key = key.toLowerCase();

		for (int i = 0; i < keys.length; i++) {
			if (keys[i].toLowerCase().equals(key)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public Object[] generateParameterArray(String[] inputArray) {
		Object[] defaultArgs = getDefaultParameter();
		ITypeConverter<String, Object>[] converter = getParameterConverters();
		Object[] parameter = new Object[Math.min(inputArray.length, defaultArgs.length)];

		for (int i = 0; i < parameter.length; i++) {
			if (inputArray[i] == null) {
				// use default argument
				parameter[i] = defaultArgs[i];
			} else {
				// use the respective converter
				parameter[i] = converter[i].convert(inputArray[i]);
			}
		}

		return parameter;
	}

	/**
	 * Gets an array of keys this action accepts. The keys have to match the order.
	 * The keys are case-insensitive the actual parameter array
	 */
	protected abstract String[] getParameterKeys();

	/**
	 * Gets an array of converters that will be able to transform a String into the
	 * data type the respective index in the final parameter array needs to
	 */
	protected abstract ITypeConverter<String, Object>[] getParameterConverters();

	/**
	 * Indicates whether this action is dependent on external parameter that have to
	 * be set via {@link #setParameter(Object[])} before calling {@link #run()}
	 */
	public abstract boolean requiresParameter();
}
