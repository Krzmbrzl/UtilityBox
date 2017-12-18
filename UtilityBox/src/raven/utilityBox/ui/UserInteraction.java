package raven.utilityBox.ui;

import org.eclipse.swt.SWT;

import raven.utilityBox.exceptions.NotImplementedException;
import raven.utilityBox.interfaces.IValidator;

/**
 * A class providing support for user interaction
 * 
 * @author Raven
 *
 */
public class UserInteraction extends AbstractUtilityBoxUIElement {
	
	public static final char YES = 'y';
	public static final char NO = 'n';
	public static final char CANCEL = 'c';
	public static final char OK = '\n';
	
	/**
	 * The message to display
	 */
	protected String message;
	/**
	 * The String representing a user interaction
	 */
	protected String userInteraction;
	/**
	 * The return status by opening this interaction
	 */
	protected int returnStatus;
	/**
	 * Indicates whether this element should accept custom input (String) from
	 * the user as well
	 */
	private boolean acceptCustomInput;
	/**
	 * The custom input typed in by the user
	 */
	protected String customUserInput;
	/**
	 * The validator that is used for validating the custom user input
	 */
	private IValidator validator;
	
	
	/**
	 * Creates a new <code>UserInteraction</code> that can be used for
	 * displaying information to the user and getting a user's reaction to them
	 * 
	 * @param message
	 *            The message to display
	 * @param style
	 *            The style to use for this element. Can be YES, NO, CANCEL or
	 *            OK as defined in {@link SWT}
	 * @param acceptCustomInput
	 *            Whether or not the user is allowed to specify a custom input
	 * @param validator
	 *            The validator that should be used in order to validate the
	 *            custom user input
	 */
	public UserInteraction(String message, int style, boolean acceptCustomInput,
			IValidator validator) {
		super(style);
		
		assert (message != null);
		
		this.message = message;
		acceptCustomUserInput(acceptCustomInput);
		setCustomUserInputValidator(validator);
		returnStatus = -1;
	}
	
	/**
	 * Creates a new <code>UserInteraction</code> that can be used for
	 * displaying information to the user and getting a user's reaction to
	 * them.<br>
	 * This version will not allow custom input from the user
	 * 
	 * @param message
	 *            The message to display
	 * @param style
	 *            The style to use for this element. Can be YES, NO, CANCEL or
	 *            OK as defined in {@link SWT}
	 */
	public UserInteraction(String message, int style) {
		this(message, style, false, null);
	}
	
	/**
	 * Opens this user interaction
	 */
	public int open() {
		doSetVisible(true);
		
		return returnStatus;
	}
	
	/**
	 * Opens this user interaction
	 * 
	 * @param suspend
	 *            Indicates whether the calling thread should be suspended
	 */
	public void open(boolean suspend) {
		if (!suspend) {
			// TODO make asynchron
		} else {
			open();
		}
	}
	
	@Override
	protected void initializeGUI() {
		throw new NotImplementedException("The GUI is not yet implemented!");
	}
	
	@Override
	protected void initializeTerminal() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void doSetVisible(boolean visible) {
		if (isTerminalMode()) {
			if (visible) {
				showTerminalVersion();
			} else {
				clearTerminal();
			}
		} else {
			throw new NotImplementedException(
					"The GUI for this element is not yet implemented!");
		}
	}
	
	/**
	 * "Opens" the terminal representation of this element
	 */
	protected void showTerminalVersion() {
		printOut(message, false);
		
		if (acceptsCustomInput()) {
			if (userInteraction == null) {
				userInteraction = "Type in your answer";
			} else {
				userInteraction += "; Custom input is also enabled";
			}
		}
		
		if (userInteraction != null) {
			char charInput = (char) -1;
			String userInput = null;
			
			while (!vaildateAnswerCharacter(charInput)
					&& !(usesCustomUserInputValidator()
							&& validateInput(userInput))) {
				printOut(userInteraction, true);
				userInput = getUserTerminalInput().trim();
				
				if (userInput.isEmpty()) {
					// user has typed in enter without any other arguments
					userInput = "\n";
				}
				
				if (userInput.length() > 1) {
					continue;
				}
				
				if (userInput.length() == 1) {
					charInput = userInput.charAt(0);
				}
			}
			
			if (!processReturnStatus(charInput)) {
				customUserInput = userInput;
			}
		}
	}
	
	protected boolean validateInput(String input) {
		if (!usesCustomUserInputValidator()) {
			// no validation for the complete input
			return true;
		}
		
		if (input == null) {
			// initial input does not need any checking
			return false;
		}
		
		if (getCustomUserInputValidator().validate(input)) {
			if (isTerminalMode()) {
				// Make sure the user didn't accidentally typed in the given
				// input
				printOut("If you are sure you want to use \"" + input
						+ "\" as your input type " + YES
						+ " and confirm with enter", true);
				
				String checkInput = getUserTerminalInput().trim();
				
				if (checkInput.equals(String.valueOf(YES))) {
					return true;
				} else {
					printOut("Cancelled input \"" + input + "\"\n", true);
					return false;
				}
			} else {
				// No further validation needed
				return true;
			}
		} else {
			printOut(getCustomUserInputValidator().getValidationErrorMessage(),
					true);
			return false;
		}
	}
	
	/**
	 * Checks whether the given character is a valid answer to the statement
	 * 
	 * @param c
	 *            The character typed in by the user
	 */
	protected boolean vaildateAnswerCharacter(char c) {
		if (c == YES && (getStyle() & SWT.YES) == SWT.YES) {
			return true;
		}
		
		if (c == NO && (getStyle() & SWT.NO) == SWT.NO) {
			return true;
		}
		
		if (c == CANCEL && (getStyle() & SWT.CANCEL) == SWT.CANCEL) {
			return true;
		}
		
		if ((c == '\n' || c == '\r') && (getStyle() & SWT.OK) == SWT.OK) {
			return true;
		}
		
		return false;
	}
	
	protected boolean processReturnStatus(char userInput) {
		switch (userInput) {
			case YES:
				returnStatus = SWT.YES;
				return true;
			case NO:
				returnStatus = SWT.NO;
				return true;
			case CANCEL:
				returnStatus = SWT.CANCEL;
				return true;
			case OK:
				returnStatus = SWT.OK;
				return true;
			default:
				returnStatus = -1;
				return false;
		}
	}
	
	@Override
	public void setStyle(int style) {
		super.setStyle(style);
		
		// clear interaction text
		userInteraction = null;
		
		if ((style & SWT.YES) == SWT.YES) {
			userInteraction = YES + "=yes";
		}
		
		if ((style & SWT.NO) == SWT.NO) {
			if (userInteraction != null) {
				userInteraction += "; " + NO + "=no";
			} else {
				userInteraction = NO + "=no";
			}
		}
		
		if ((style & SWT.CANCEL) == SWT.CANCEL) {
			if (userInteraction != null) {
				userInteraction += "; " + CANCEL + "=cancel";
			} else {
				userInteraction = CANCEL + "=cancel";
			}
		}
		
		if ((style & SWT.OK) == SWT.OK) {
			if (userInteraction != null) {
				userInteraction += "; Press enter to continue";
			} else {
				userInteraction = NO + "=no";
			}
		}
	}
	
	/**
	 * Check whether this element accepts custom user input that can then be
	 * accessed by {@link #getCustomUserInput()}
	 */
	public boolean acceptsCustomInput() {
		return acceptCustomInput;
	}
	
	/**
	 * Defines whether this element does accept custom user input
	 * 
	 * @param accept
	 *            Whether to accept it
	 */
	public void acceptCustomUserInput(boolean accept) {
		acceptCustomInput = accept;
	}
	
	/**
	 * Gets the input the user has typed in
	 */
	public String getCustomUserInput() {
		return customUserInput;
	}
	
	/**
	 * Checks whether this element does contain custom user input
	 */
	public boolean customUserInputEntered() {
		return getCustomUserInput() != null;
	}
	
	/**
	 * Gets the validator used for validating the custom user input
	 */
	protected IValidator getCustomUserInputValidator() {
		return validator;
	}
	
	/**
	 * Sets the validator that should be used in order to validate the input the
	 * user types in
	 * 
	 * @param validator
	 *            The <code>IValidator</code> to use
	 */
	public void setCustomUserInputValidator(IValidator validator) {
		this.validator = validator;
	}
	
	/**
	 * Checks whether a validator will be used for custom user input.<br>
	 * This method does also return <code>false</code> if
	 * {@link #acceptsCustomInput()} returns <code>false</code>
	 */
	public boolean usesCustomUserInputValidator() {
		return acceptsCustomInput() && getCustomUserInputValidator() != null;
	}
	
	@Override
	public void printOut(String message, boolean append) {
		if (isTerminalMode()) {
			if (!append) {
				clearTerminal();
			}
			
			System.out.println(message);
		}
	}
}
