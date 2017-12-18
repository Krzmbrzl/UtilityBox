package raven.utilityBox.actions;

import java.io.File;
import raven.utilityBox.enums.EStatus;
import raven.utilityBox.exceptions.FileDoesNotExistsException;
import raven.utilityBox.files.FilenameFormatter;
import raven.utilityBox.interfaces.IPreferenceSensitiveObject;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.preferences.AbstractPreference;
import raven.utilityBox.preferences.rules.FilenameFormatterRule;
import raven.utilityBox.util.ArrayUtil;

/**
 * This action will format and rename the given filename. If the given file is a
 * directory the files in that directory will be renamed
 * 
 * @author Raven
 *
 */
public class FilenameFormatAction extends AbstractAction
		implements IPreferenceSensitiveObject {
	
	/**
	 * The default arguments for this action
	 */
	protected Object[] defaultArgs = { null, false, -1 };
	
	/**
	 * The root file this renamer is working on
	 */
	protected File rootFile;
	/**
	 * Indicates whether or not sub-directories should be included in the
	 * process
	 */
	protected boolean includeSubDirectories;
	/**
	 * Indicates how many levels of sub-directories should be included. -1 means
	 * all.
	 */
	protected int maxSubLevel;
	/**
	 * The preferences for this formatter
	 */
	protected FilenameFormatterRule preferences;
	
	
	/**
	 * Creates this file renamer on the given file
	 * 
	 * @param file
	 *            The file or directory to work on
	 * @param includeSubdirectories
	 *            Whether or not to include sub-directories in the process
	 * @param maxSubLevels
	 *            How many levels of sub-directories should be included in the
	 *            process. -1 means all available sub-directories
	 */
	public FilenameFormatAction(File file, boolean includeSubdirectories,
			int maxSubLevels) {
		setParameter(
				new Object[] { file, includeSubdirectories, maxSubLevels });
	}
	
	/**
	 * Creates this file renamer on the given file. See {@link #defaultArgs} for
	 * what the default values for the unspecified field is.
	 * 
	 * @param file
	 *            The file or directory to work on
	 * @param includeSubdirectories
	 *            Whether or not to include sub-directories in the process
	 */
	public FilenameFormatAction(File file, boolean includeSubdirectories) {
		setParameter(new Object[] { file, includeSubdirectories });
	}
	
	/**
	 * Creates this file renamer on the given file. See {@link #defaultArgs} for
	 * what the default values for the unspecified fields is.
	 * 
	 * @param file
	 *            The file or directory to work on
	 */
	public FilenameFormatAction(File file) {
		setParameter(new Object[] { file });
	}
	
	@Override
	public EStatus doRun() {
		if (rootFile.isDirectory()) {
			formatFileNamesInDirectory(rootFile, 0);
		} else {
			if (rootFile.isFile()) {
				formatFileName(rootFile);
			} else {
				// cancel job because of an unexpected behaviour
				Logger.getDefault()
						.log(new LogMessage(
								"Unexpected file type (neither file nor directory!",
								this, LogMessage.SEVERITY_ERROR));
				
				return EStatus.CANCEL;
			}
		}
		
		return EStatus.OK;
	}
	
	/**
	 * Formats the names of files contained in the given directory
	 * 
	 * @param dir
	 *            The directory to process
	 * @param currentSubLevel
	 *            The sub-level of the given directory relative to the
	 *            {@link #rootFile}
	 */
	protected void formatFileNamesInDirectory(File dir, int currentSubLevel) {
		for (File currentFile : dir.listFiles()) {
			if (currentFile.isFile()) {
				formatFileName(currentFile);
			} else {
				if (includeSubDirectories && currentFile.isDirectory()
						&& (maxSubLevel < 0 || currentSubLevel < maxSubLevel)) {
					// process files in this directory as well
					formatFileNamesInDirectory(currentFile, ++currentSubLevel);
				}
			}
		}
	}
	
	/**
	 * Formats the given file's name
	 * 
	 * @param file
	 *            The file whose name should be formatted
	 */
	protected void formatFileName(File file) {
		String fileExtension = file.getName();
		if (fileExtension.contains(".")) {
			fileExtension = fileExtension
					.substring(fileExtension.lastIndexOf(".") + 1);
		} else {
			fileExtension = "";
		}
		
		if (processFileType(fileExtension)) {
			System.out.println(file.getAbsolutePath());
			
			FilenameFormatter formatter = getFormatter(file);
			
			formatter.format();
			
			try {
				formatter.rename();
			} catch (FileDoesNotExistsException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets the formatter that should be used
	 */
	protected FilenameFormatter getFormatter(File file) {
		return new FilenameFormatter(file,
				(FilenameFormatterRule) getPreferenceRules());
	}
	
	/**
	 * Checks whether a file of the given type (determined by it's file
	 * extension) will be processed
	 * 
	 * @param type
	 *            The file extension to check (without leading dot)
	 */
	public boolean processFileType(String type) {
		return true;
	}
	
	@Override
	public boolean validateParameter() {
		if (args == null) {
			Logger.getDefault()
					.log(new LogMessage("The parameter array must not be null!",
							this, LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		if (args.length < 3) {
			Logger.getDefault()
					.log(new LogMessage("Expected 3 parameter but only got "
							+ args.length + "!", this,
							LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		if (!(args[0] instanceof File)) {
			Logger.getDefault()
					.log(new LogMessage("The first parameter has to be a file!",
							this, LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		File file = (File) args[0];
		
		if (!file.exists()) {
			Logger.getDefault()
					.log(new LogMessage("The given file does not exist!", this,
							LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		if (!file.canRead() || !file.canWrite()) {
			Logger.getDefault()
					.log(new LogMessage("The given file is not accessible!",
							this, LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		if (!(args[1] instanceof Boolean)) {
			Logger.getDefault()
					.log(new LogMessage(
							"The second parameter has to be a boolean!", this,
							LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		if (!(args[2] instanceof Integer)) {
			Logger.getDefault()
					.log(new LogMessage(
							"The third parameter has to be an integer!", this,
							LogMessage.SEVERITY_ERROR));
			
			return false;
		}
		
		return true;
	}
	
	@Override
	public void setParameter(Object[] args) {
		if (args == null) {
			// Log and switch to default parameter
			Logger.getDefault()
					.log(new LogMessage(
							"Invalid null parameter! Swithcing to default parameter...",
							this, LogMessage.SEVERITY_WARNING));
			
			args = defaultArgs;
		}
		
		// make sure all fields are included
		if (args.length < defaultArgs.length) {
			Object[] newArgs = ArrayUtil.cloneArray(defaultArgs);
			
			// copy given fields
			for (int i = 0; i < args.length; i++) {
				newArgs[i] = args[i];
			}
			
			args = newArgs;
		}
		
		super.setParameter(args);
		
		// store the arguments in the given object variables
		applyParameter();
	}
	
	/**
	 * Applies the given paramter by storing them into thge respective
	 * variables. This method gets called right after
	 * {@link #setParameter(Object[])} has been called and the correctness of
	 * the parameter has been assured
	 */
	protected void applyParameter() {
		rootFile = (File) args[0];
		includeSubDirectories = (boolean) args[1];
		maxSubLevel = (int) args[2];
	}
	
	@Override
	public void setPreferenceRules(AbstractPreference preferences) {
		if (preferences.getClass().equals(getPreferenceType())) {
			this.preferences = (FilenameFormatterRule) preferences;
		} else {
			Logger.getDefault()
					.log(new LogMessage(
							"Invalid preference type. Switching to default behaviour!",
							this, LogMessage.SEVERITY_WARNING));
		}
	}
	
	@Override
	public Class<?> getPreferenceType() {
		return FilenameFormatterRule.class;
	}
	
	@Override
	public AbstractPreference getPreferenceRules() {
		if (preferences == null) {
			preferences = new FilenameFormatterRule();
			preferences.initializeWithDefaults();
		}
		
		return preferences;
	}
	
}
