package raven.utilityBox.actions;

import raven.utilityBox.enums.EStatus;
import raven.utilityBox.exceptions.InvalidParameterException;
import raven.utilityBox.interfaces.IAction;
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
	
	
	@Override
	public void setParameter(Object[] args) {
		this.args = args;
		
		if (!validateParameter()) {
			Logger.getDefault()
					.log(new LogMessage("The given parameter are invalid!",
							this, LogMessage.SEVERITY_ERROR));
			
			throw new InvalidParameterException(
					"The given parameter are not valid!");
		}
	}
	
	/**
	 * Checks whether this Action has parameter set
	 */
	public boolean hasParameter() {
		return args != null;
	}
	
	/**
	 * Gets the name of this Action
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the ID of this Action
	 */
	public int getID() {
		return ID;
	}
	
	@Override
	public final EStatus run() {
		Logger.getDefault().log(new LogMessage("Starting " + getName() + "...",
				null, LogMessage.SEVERITY_INFO));
		
		EStatus status = doRun();
		
		Logger.getDefault().log(new LogMessage("Completed " + getName(), null,
				LogMessage.SEVERITY_INFO));
		
		return status;
	}
	
	/**
	 * Executes the code associated with this action
	 * 
	 * @return The status after having completed the code
	 */
	protected abstract EStatus doRun();
	
}
