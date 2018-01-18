package raven.utilityBox.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Calendar;

import org.eclipse.swt.SWT;

import raven.utilityBox.ui.UserInteraction;

/**
 * A logger for logging program messages
 * 
 * @author Raven
 *
 */
public class Logger {

	/**
	 * The prefix for info messages
	 */
	public static final String INFO_PREFIX = "[INFO]";
	/**
	 * The prefix for warning messages
	 */
	public static final String WARNING_PREFIX = "[WARNING]";
	/**
	 * The prefix for error messages
	 */
	public static final String ERROR_PREFIX = "[ERROR]";

	/**
	 * Indicates whether messages are directly echoed to the user as they appear
	 */
	protected boolean echoMessages;
	/**
	 * The default instance of this logger
	 */
	protected static Logger logger;
	/**
	 * The last cotext that was reported to this logger
	 */
	protected Object currentContext;
	/**
	 * Indicating whether there has already been a log message written to the log
	 * file
	 */
	protected boolean startedLogging;
	/**
	 * The log file
	 */
	protected File logFile;
	/**
	 * The folder to contain the log file
	 */
	protected File logFolder;
	/**
	 * Indicates whether this logger has already been initialized
	 */
	protected boolean initialized;


	/**
	 * Creates a new logger. If you do not explicitly need a new instance use
	 * {@link #getDefault()}
	 */
	public Logger() {
		File programDir = Paths.get(".").toFile();
		logFolder = new File(programDir, "UtilityBox_Logs");
	}

	/**
	 * Initializes this logger
	 */
	protected void initialize() {
		if (logFile == null) {
			if (!logFolder.exists()) {
				logFolder.mkdir();
			}

			logFile = new File(logFolder, LogMessage.dateFormat.format(Calendar.getInstance().getTime()) + ".log");
		}

		try {
			if (!logFile.createNewFile()) {
				// file does already exist -> make it empty
				PrintWriter writer = new PrintWriter(logFile);
				writer.print("");
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();

			throw new RuntimeException("Unable to create log file", e);
		}

		initialized = true;
	}

	/**
	 * Sets the file the log-messages should get written into
	 * 
	 * @param file
	 *            The file to use
	 */
	public void setLogFile(File file) {
		logFile = file;
	}

	/**
	 * Gets the default instance of this logger
	 */
	public static Logger getDefault() {
		if (logger == null) {
			logger = new Logger();
		}

		return logger;
	}

	/**
	 * Specifies whether log messages should directly be echoed to the user as they
	 * appear
	 * 
	 * @param echo
	 *            Whether or not to enable echoing
	 */
	public void echoMessages(boolean echo) {
		echoMessages = echo;
	}

	/**
	 * Logs the given message as an error message without any context
	 * 
	 * @param msg
	 *            The message to log
	 */
	public void log(String msg) {
		log(new LogMessage(msg, null, LogMessage.SEVERITY_ERROR));
	}

	/**
	 * Logs the given <code>Throwable</code> as an error
	 * 
	 * @param t
	 *            The <code>Throwable</code> to log
	 * @param context
	 *            The context of this exception
	 */
	public void log(Throwable t, Object context) {
		StackTraceElement[] stack = t.getStackTrace();
		StackTraceElement lastMethod = stack[stack.length - 2];

		log(new LogMessage(
				"An exception [" + lastMethod.getFileName() + " - " + lastMethod.getMethodName() + "("
						+ lastMethod.getLineNumber() + ")" + "] occured: " + t.getMessage(),
				context, LogMessage.SEVERITY_ERROR));
	}

	/**
	 * Logs the given <code>Throwable</code> as an error with the exception itself
	 * as the context
	 * 
	 * @param t
	 *            The <code>Throwable</code> to log
	 */
	public void log(Throwable t) {
		log(t, t);
	}

	/**
	 * Logs the given {@link LogMessage}
	 * 
	 * @param message
	 *            The message to log
	 */
	public void log(LogMessage message) {
		if (!initialized) {
			initialize();
		}

		try {
			FileWriter writer = new FileWriter(logFile, true);

			String msg = message.getMessage();

			switch (message.getSeverity()) {
			case LogMessage.SEVERITY_INFO:
				msg = INFO_PREFIX + ": " + msg;
				break;

			case LogMessage.SEVERITY_WARNING:
				msg = WARNING_PREFIX + ": " + msg;
				break;

			case LogMessage.SEVERITY_ERROR:
				msg = ERROR_PREFIX + ": " + msg;
				break;
			}

			// add extra message pieces
			msg = ((!startedLogging) ? "" : "\n")
					// add empty line on context change
					+ ((currentContext == message.getContext() || !startedLogging) ? "" : "\n") + message.getTimeStamp()
					+ ": " + msg
					// add context information
					+ ((message.getContext() != null) ? " - (" + message.getContext().getClass().getSimpleName() + ")"
							: "");

			writer.append(msg);
			writer.close();

			if (echoMessages) {
				System.out.println(msg);
			}

			// Indicate that the logging has started
			startedLogging = true;
			currentContext = message.getContext();
		} catch (IOException e) {
			e.printStackTrace();

			// Notify user
			UserInteraction info = new UserInteraction("Failed at writing log message: " + e.getMessage(), SWT.OK);

			info.open(false);
		}
	}
}
