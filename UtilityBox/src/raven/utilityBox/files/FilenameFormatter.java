package raven.utilityBox.files;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;

import raven.utilityBox.exceptions.FileDoesNotExistsException;
import raven.utilityBox.interfaces.IValidator;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.preferences.rules.FilenameFormatterRule;
import raven.utilityBox.ui.UserInteraction;

public class FilenameFormatter {
	
	/**
	 * An array containg another array for every bracket type (0=opener ;
	 * 1=closer)
	 */
	public static final char[][] BRACKETS = { { '(', ')' }, { '[', ']' },
			{ '{', '}' } };
	
	/**
	 * The music file whose name should be formatted
	 */
	protected File file;
	/**
	 * The rules for formatting the file name
	 */
	protected FilenameFormatterRule rules;
	/**
	 * The original filename (without file extension) before any formatting was
	 * applied
	 */
	protected final String originalFilename;
	/**
	 * The file extension of the music file
	 */
	protected final String fileExtension;
	/**
	 * The formatted file name
	 */
	protected StringBuilder formattedFileName;
	/**
	 * The validator used in order to validate a file name
	 */
	protected IValidator fileNameValidator;
	
	
	public FilenameFormatter(File file, FilenameFormatterRule rules) {
		assert (file.exists());
		
		this.file = file;
		this.rules = rules;
		
		String fileName = file.getName();
		if (fileName.contains(".")) {
			fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
			originalFilename = fileName.substring(0, fileName.lastIndexOf("."));
		} else {
			fileExtension = "";
			originalFilename = fileName;
		}
		
		fileNameValidator = new IValidator() {
			private boolean passedValidation;
			private Object validationObject;
			
			@Override
			public boolean validate(Object obj) {
				validationObject = obj;
				
				String msg = getValidationErrorMessage();
				
				return (msg == null) ? false : msg.isEmpty();
			}
			
			@Override
			public String getValidationErrorMessage() {
				if (passedValidation) {
					return "";
				}
				if (validationObject == null) {
					return null;
				}
				
				if (validationObject instanceof String) {
					if (((String) validationObject).contains(File.separator)) {
						return "Invalid character \"" + File.separator
								+ "\" in name!";
					} else {
						return "";
					}
				} else {
					return "Can only validate Strings";
				}
			}
		};
	}
	
	/**
	 * Starts formatting the filename of this file
	 */
	public void format() {
		if (formattedFileName == null) {
			if (rules.transformLettersToLowercaseBeforeFormatting()) {
				if (rules.trimFilename()) {
					formattedFileName = new StringBuilder(
							originalFilename.toLowerCase().trim());
				} else {
					formattedFileName = new StringBuilder(
							originalFilename.toLowerCase());
				}
			} else {
				if (rules.trimFilename()) {
					formattedFileName = new StringBuilder(
							originalFilename.trim());
				} else {
					formattedFileName = new StringBuilder(originalFilename);
				}
			}
		}
		
		if (rules.checkBracketContent()) {
			checkBracketContent(formattedFileName);
		}
		
		if (rules.specifiesPrefixRegex()) {
			removePrefix();
		}
		
		if (rules.transformFirstLettersToUpperCase()) {
			transformFirstLettersToUpperCase(formattedFileName);
		}
		
		if (rules.trimFilename()) {
			// remove leading, trailing WS and double blanks
			formattedFileName = new StringBuilder(formattedFileName.toString()
					.trim().replaceAll("\\s\\s+", " "));
		}
	}
	
	/**
	 * Replaces the filesystem seperator sequence in the formatted name with
	 * either a slash or a backslash
	 */
	protected void replaceFileSystemSeperator() {
		// prevent accidental creations of folders
		String notSeperator = (File.separator.equals("\\")) ? "/" : "\\";
		formattedFileName = new StringBuilder(formattedFileName.toString()
				.replaceAll(File.separator, notSeperator));
	}
	
	/**
	 * @return The formatted filename without the file extension or
	 *         <code>null</code> if {@link #format()} has not yet been called
	 */
	public String getFormattedFilename() {
		return (formattedFileName == null) ? null
				: formattedFileName.toString();
	}
	
	/**
	 * @return The file extension of the file that is being formatted
	 */
	public String getFileExtension() {
		return fileExtension;
	}
	
	/**
	 * Renames the file to the formatted name after checking whether the new
	 * file does already exist.<br>
	 * {@link #format()} has to called before!
	 * 
	 * @return Whether or not the file has been renamed
	 * @throws FileDoesNotExistsException
	 *             If the specified file does no longer exist
	 */
	public boolean rename() throws FileDoesNotExistsException {
		assert (formattedFileName != null);
		
		replaceFileSystemSeperator();
		
		if (!originalFilename.equals(formattedFileName.toString())) {
			File targetFile = getRenamedFile(file);
			
			if (checkRename(targetFile)) {
				// rename the file
				return doRename(targetFile);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Creates a new file with the formatted file name
	 * 
	 * @param originFile
	 *            The file to "rename"
	 * @return The new file with the formatted name
	 */
	protected File getRenamedFile(File originFile) {
		return new File(originFile.getParentFile(), formattedFileName.toString()
				+ ((fileExtension.isEmpty()) ? "" : "." + fileExtension));
	}
	
	/**
	 * Checks whether there are conflicts when creating the given file
	 * 
	 * @param file
	 *            The file to check
	 * @return Whether or not to rename the given file
	 */
	protected boolean checkRename(File file) {
		if (file.exists()) {
			// file with the new name does already exist
			Logger.getDefault()
					.log(new LogMessage(
							"The file \"" + file.getAbsolutePath()
									+ "\" does already exist",
							this, LogMessage.SEVERITY_WARNING));
			
			if (rules.allowOverwriteOfExistingFilesOnRename()) {
				int response = SWT.YES;
				
				if (rules.askBeforeOverwritingFiles()) {
					UserInteraction interact = new UserInteraction(
							"Do you want to overwrite the file \""
									+ file.getAbsolutePath() + "\"?",
							SWT.YES | SWT.NO);
					
					response = interact.open();
				}
				
				if (response != SWT.YES) {
					Logger.getDefault()
							.log(new LogMessage(
									"Cancelled rename of \""
											+ file.getAbsolutePath() + "\"",
									this, LogMessage.SEVERITY_INFO));
					return false;
				}
			} else {
				Logger.getDefault()
						.log(new LogMessage(
								"Skipping rename of \"" + file.getAbsolutePath()
										+ "\" as overwriting files is disabled",
								this, LogMessage.SEVERITY_INFO));
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Renames the {@link #file} to the given targetFile
	 * 
	 * @param targetFile
	 *            The new target file the {@link #file} should be renamed to
	 * @return Whether or not the renaming was successfull
	 * @throws FileDoesNotExistsException
	 *             If the {@link #file} does no longer exist
	 */
	protected boolean doRename(File targetFile)
			throws FileDoesNotExistsException {
		if (!file.exists()) {
			throw new FileDoesNotExistsException("The file \""
					+ file.getAbsolutePath() + "\" does not exist!");
		}
		
		if (file.renameTo(targetFile)) {
			logRename(file.getName(), targetFile.getName(),
					targetFile.getParent());
			
			return true;
		} else {
			Logger.getDefault()
					.log(new LogMessage(
							"Failed at renaming \"" + file.getName() + "!",
							this, LogMessage.SEVERITY_ERROR));
			
			return false;
		}
	}
	
	/**
	 * Logs the rename of the given files
	 * 
	 * @param oldName
	 *            The old file name
	 * @param newName
	 *            The new file name
	 * @param directory
	 *            The directory of the renamed file
	 */
	protected void logRename(String oldName, String newName, String directory) {
		Logger.getDefault()
				.log(new LogMessage(
						"Renamed \"" + oldName + "\" to \"" + newName
								+ "\" in \"" + directory + "\"",
						this, LogMessage.SEVERITY_INFO));
	}
	
	/**
	 * Transforms the first letter in every word into an uppercase letter
	 * 
	 * @param builder
	 *            The <code>StrinBuilder</code> to use as a source string
	 */
	protected void transformFirstLettersToUpperCase(StringBuilder builder) {
		boolean newWord = true;
		
		for (int i = 0; i < builder.length(); i++) {
			char currentChar = builder.charAt(i);
			
			if (newWord && Character.isAlphabetic(currentChar)) {
				// transform charcter too uppercase as it is at a word's start
				builder.setCharAt(i, Character.toUpperCase(currentChar));
			}
			
			// check whether the current character is the last one in a word
			if (rules.isWordDelimiter(currentChar)) {
				newWord = true;
			} else {
				newWord = false;
			}
		}
	}
	
	/**
	 * Checks the different brackets for their content
	 * 
	 * @param builder
	 *            The <code>StringBuilder</code> to use as a source string
	 */
	protected void checkBracketContent(StringBuilder builder) {
		for (char[] currentBracket : (rules.useSpecifiedBracketsOnly())
				? rules.getBracketsToFormat() : BRACKETS) {
			Pattern pattern = Pattern.compile(Pattern
					.quote(String.valueOf(currentBracket[0])) + ".*?"
					+ (Pattern.quote(String.valueOf(currentBracket[1]))));
			
			Matcher matcher = pattern.matcher(builder);
			
			while (matcher.find()) {
				String bracketContent = builder.substring(matcher.start(),
						matcher.end());
				
				if (!preserveBracket(bracketContent,
						rules.bracketContentIsCaseSensitive())) {
					// The bracket has to be deleted
					builder.delete(matcher.start(), matcher.end());
					
					matcher = pattern.matcher(builder);
				}
			}
		}
	}
	
	/**
	 * Checks whether the bracket with the given content should be preserved
	 * 
	 * @param bracketContent
	 *            The content of the bracket
	 * @param caseSensitive
	 *            Whether the braacket content search is done case-sensitively
	 * @return Whether or not to preserve this bracker
	 */
	protected boolean preserveBracket(String bracketContent,
			boolean caseSensitive) {
		if (!caseSensitive) {
			bracketContent = bracketContent.toLowerCase();
		}
		
		boolean wholeWord = rules.searchBracketContentForWholeWords();
		
		for (String currentBracketContent : rules.getAllowedBracketContent()) {
			if (!caseSensitive) {
				currentBracketContent = currentBracketContent.toLowerCase();
			}
			
			if (wholeWord) {
				// only search for whole words
				if (bracketContent.matches(".*\\b"
						+ Pattern.quote(currentBracketContent) + "\\b.*")) {
					// skip this bracket as it has to be preserved
					// because of it's content
					return true;
				}
			} else {
				// also search for word parts
				if (bracketContent.contains(currentBracketContent)) {
					// skip this bracket as it has to be preserved
					// because of it's content
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Removes all prefixes from the file name that match one of the given
	 * prefix regex from the preferences
	 */
	protected void removePrefix() {
		for (String currentRegex : rules.getPrefixRegexToRemove()) {
			Matcher matcher = Pattern.compile(currentRegex)
					.matcher(formattedFileName);
			
			if (matcher.find() && matcher.start() == 0) {
				// remove prefix
				String name = formattedFileName.substring(matcher.end());
				
				if (rules.trimFilename()) {
					name = name.trim();
				}
				
				formattedFileName = new StringBuilder(name);
			}
		}
	}
	
	
	/**
	 * Gets the preferences for this formatter
	 */
	public FilenameFormatterRule getPreferences() {
		return rules;
	}
	
}
