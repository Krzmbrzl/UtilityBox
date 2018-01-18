package raven.utilityBox.logging;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A log message containing a message, a context and a severity
 * 
 * @author Raven
 *
 */
public class LogMessage {
	
	/**
	 * Constant for an error severity
	 */
	public static final int SEVERITY_ERROR = 2;
	/**
	 * Constant for a warning severity
	 */
	public static final int SEVERITY_WARNING = 1;
	/**
	 * Constant for a information severity
	 */
	public static final int SEVERITY_INFO = 0;
	/**
	 * The date formatter used by this logger
	 */
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd_hh-mm-ss");
	/**
	 * A field holding the ID for the next <code>LogMessage</code>
	 */
	protected static int NEW_ID = 0;
	
	/**
	 * The message to log
	 */
	protected String message;
	/**
	 * The context of this message
	 */
	protected Object context;
	/**
	 * The severity of this message
	 */
	protected int severity;
	/**
	 * The ID of this <code>LogMessage</code>
	 */
	protected int ID;
	/**
	 * The timestamp indicating when this log message has occured
	 */
	protected String timeStamp;
	
	
	/**
	 * Creates a new log message
	 * 
	 * @param message
	 *            The message to log (A timestamp will be preappended to this
	 *            message). May not be empty
	 * @param context
	 *            The context in which the message occured. This is important
	 *            for message overwriting and cancelling. May be
	 *            <code>null</code>
	 * @param severity
	 *            The severity of the message
	 */
	public LogMessage(String message, Object context, int severity) {
		assert (message != null);
		assert (!message.isEmpty());
		
		// store message with time stamp
		this.message = message;
		this.timeStamp = dateFormat.format(Calendar.getInstance().getTime());
		this.context = context;
		this.severity = severity;
		this.ID = NEW_ID;
		
		NEW_ID++;
	}
	
	/**
	 * Creates a new log message with {@link #SEVERITY_WARNING}
	 * 
	 * @param message
	 *            The message to log (A timestamp will be preappended to this
	 *            message). May not be empty
	 * @param context
	 *            The context in which the message occured. This is important
	 *            for message overwriting and cancelling. May be
	 *            <code>null</code>
	 */
	public LogMessage(String message, Object context) {
		this(message, context, SEVERITY_WARNING);
	}
	
	/**
	 * Creates a new log message with no context
	 * 
	 * @param message
	 *            The message to log (A timestamp will be preappended to this
	 *            message). May not be empty
	 * @param severity
	 *            The severity of the message.
	 */
	public LogMessage(String message, int severity) {
		this(message, null, severity);
	}
	
	/**
	 * Creates a new log message with no context and {@link #SEVERITY_WARNING}
	 * 
	 * @param message
	 *            The message to log (A timestamp will be preappended to this
	 *            message). May not be empty
	 */
	public LogMessage(String message) {
		this(message, null, SEVERITY_WARNING);
	}
	
	/**
	 * Gets the message of this <code>LogMessage</code>
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Gets the context of this <code>LoMessage</code>
	 */
	public Object getContext() {
		return context;
	}
	
	/**
	 * Gets the severity of this <code>LogMessage</code>
	 */
	public int getSeverity() {
		return severity;
	}
	
	/**
	 * Gets the ID of this <code>LogMessage</code>
	 */
	public int getID() {
		return ID;
	}
	
	/**
	 * Gets the time stamp of this log message
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		
		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}
		
		LogMessage compareMessage = (LogMessage) obj;
		
		if (ID != compareMessage.getID()) {
			return false;
		}
		
		if (!message.equals(compareMessage.getMessage())) {
			return false;
		}
		
		if (context == null) {
			if (compareMessage.getContext() != null) {
				return false;
			}
		} else {
			if (!context.equals(compareMessage.getContext())) {
				return false;
			}
		}
		
		if (severity != compareMessage.getSeverity()) {
			return false;
		}
		
		return true;
	}
	
}
