package raven.utilityBox.ui;

import java.io.IOException;

import raven.utilityBox.logging.Logger;

/**
 * The abstract base class for the UI elements used in the UtitityBox.<br>
 * What is special about them is that they can be used to represent the data
 * either in a GUI or in the terminal
 * 
 * @author Raven
 *
 */
public abstract class AbstractUtilityBoxUIElement {
	
	/**
	 * The style for this element
	 */
	private int style;
	/**
	 * Indicates whether the UI elements are currently running in terminal mode
	 */
	private static boolean terminalMode = true;
	/**
	 * Indicates whether this element is currently visible
	 */
	private boolean isVisible;
	
	
	public AbstractUtilityBoxUIElement(int style) {
		setStyle(style);
	}
	
	/**
	 * Checks whether the UI elements are currently running in terminal mode
	 */
	public static boolean isTerminalMode() {
		return terminalMode;
	}
	
	/**
	 * Sets the terminal mode for the UI elements
	 * 
	 * @param terminalMode
	 *            Whether to use the terminal output instead of GUI
	 */
	public static void useTerminalMode(boolean terminalMode) {
		AbstractUtilityBoxUIElement.terminalMode = terminalMode;
	}
	
	/**
	 * Gets the style for this element
	 */
	public int getStyle() {
		return style;
	}
	
	/**
	 * Sets the style for this element.<br>
	 * The style system is the same as in the SWT environment
	 * 
	 * @param style
	 *            The new style to use
	 */
	public void setStyle(int style) {
		this.style = style;
		
		if (isTerminalMode()) {
			initializeTerminal();
		} else {
			initializeGUI();
		}
	}
	
	/**
	 * Initializes the graphical user interface components. This function will
	 * be called when this element is not running in terminalMode.
	 */
	protected abstract void initializeGUI();
	
	/**
	 * Initializes the terminal mode. This function will be called when this
	 * element is running in terminalMode.
	 */
	protected abstract void initializeTerminal();
	
	/**
	 * Changes the visibility status of this element
	 * 
	 * @param visible
	 *            The new visibility status
	 */
	public final void setVisible(boolean visible) {
		isVisible = visible;
		
		doSetVisible(visible);
	}
	
	/**
	 * Gets the current visibility status of this element
	 */
	public boolean isVisible() {
		return isVisible;
	}
	
	/**
	 * Changes the visibility of this element internally
	 * 
	 * @param visible
	 *            The new visibility status
	 */
	protected abstract void doSetVisible(boolean visible);
	
	/**
	 * Gets the input the user made after before he pressed enter
	 * 
	 * @return The char-sequence entered by the user inclusively the newline
	 *         character that terminated the input
	 */
	protected String getUserTerminalInput() {
		StringBuilder builder = new StringBuilder();
		
		try {
			char input = (char) System.in.read();
			
			while (input != '\n') {
				if (input == '\r') {
					continue;
				}
				
				builder.append(input);
				
				input = (char) System.in.read();
			}
			
			builder.append(input);
		} catch (IOException e) {
			e.printStackTrace();
			
			Logger.getDefault().log(e, this);
		}
		
		return builder.toString();
	}
	
	/**
	 * Prints out the given message out so that the user can read it
	 * 
	 * @param message
	 *            The message to display
	 * @param append
	 *            Whether to append the message to the previous ones instead of
	 *            diplaying it as a single message
	 */
	public abstract void printOut(String message, boolean append);
	
	/**
	 * "Clears" the terminal by printing a bunch of newlines
	 */
	protected void clearTerminal() {
		System.out.println("\n\n\n\n\n\n\n\n\n\n");
	}
	
}
