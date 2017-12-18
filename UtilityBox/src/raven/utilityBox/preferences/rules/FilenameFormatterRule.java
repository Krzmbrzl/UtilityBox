package raven.utilityBox.preferences.rules;

import raven.utilityBox.preferences.AbstractPreference;

public class FilenameFormatterRule extends AbstractPreference {
	
	/**
	 * Indicates whether every first letter in a word should be capitalized
	 */
	protected boolean capitalizeEveryFirstLetter;
	/**
	 * Indicates whether all letters should be set to lowercase before starting
	 * formatting
	 */
	protected boolean lowercaseLettersBeforeFormatting;
	/**
	 * Defines the contents a bracket may have
	 */
	protected String[] allowedBracketContent;
	/**
	 * Indicates whether the {@link #allowedBracketContent} have to be contained
	 * as whole words
	 */
	protected boolean bracketContentWholeWord;
	/**
	 * Indicates whether the words or prefixes in brackets should be searched
	 * case-sensitively
	 */
	protected boolean bracketContentCaseSensitve;
	/**
	 * Defines a set of characters that should be considered as a word delimiter
	 */
	protected char[] additionalWordDelimiters;
	/**
	 * Indicates whether non-alphabetic characters should be considered a word
	 * delimiter
	 */
	protected boolean nonAlphabeticCharactersAreWordDelimiters;
	/**
	 * A set of characters that should not be interpreted as word delimiters
	 * (useful when {@link #considerNonAlphabeticCharactersWordDelimiter()} is
	 * turned on
	 */
	protected char[] nonWordDelimiters;
	/**
	 * Defines the brackets that should be checked for their content during
	 * formatting
	 */
	protected char[][] bracketsToFormat;
	/**
	 * Indicates whether existing files with the formatted name should be
	 * overwritten
	 */
	protected boolean allowOverwriteOfExistingFiles;
	/**
	 * Indicates whether the user should be asked before overwrinting a file
	 */
	protected boolean askBeforeOverwritingFile;
	/**
	 * Indicates whether the filename should be trimmed
	 */
	protected boolean trim;
	/**
	 * A list of prefixes to remove specified as regular expressions
	 */
	protected String[] prefixesToRemove;
	
	
	public FilenameFormatterRule() {
		
	}
	
	@Override
	public void initialize() {
		// TODO
	}
	
	@Override
	public void initializeWithDefaults() {
		capitalizeEveryFirstLetter = true;
		lowercaseLettersBeforeFormatting = true;
		bracketContentWholeWord = false;
		bracketContentCaseSensitve = false;
		nonAlphabeticCharactersAreWordDelimiters = true;
		allowOverwriteOfExistingFiles = true;
		askBeforeOverwritingFile = true;
		trim = true;
		nonWordDelimiters = new char[] { '\'' };
	}
	
	/**
	 * Indicates whether every first letter in a word should be set to upper
	 * case
	 */
	public boolean transformFirstLettersToUpperCase() {
		return capitalizeEveryFirstLetter;
	}
	
	/**
	 * Indicates whether all letters should be set to lowercase before starting
	 * formatting
	 */
	public boolean transformLettersToLowercaseBeforeFormatting() {
		return lowercaseLettersBeforeFormatting;
	}
	
	/**
	 * Indicates whether the {@link #getAllowedBracketContent()} has to be
	 * contained as whole words
	 */
	public boolean searchBracketContentForWholeWords() {
		return bracketContentWholeWord;
	}
	
	/**
	 * Indicates whether the words or prefixes in brackets should be searched
	 * case-sensitively
	 */
	public boolean bracketContentIsCaseSensitive() {
		return bracketContentCaseSensitve;
	}
	
	/**
	 * Gets content fragments a bracket may contain
	 */
	public String[] getAllowedBracketContent() {
		return allowedBracketContent;
	}
	
	/**
	 * Whether or not the brackets should be checked for their content
	 */
	public boolean checkBracketContent() {
		return getAllowedBracketContent() != null;
	}
	
	/**
	 * Gets all additional characters that should be considered as a word
	 * delimiter
	 */
	public char[] getAdditionalWordDelimiter() {
		return additionalWordDelimiters;
	}
	
	/**
	 * Indicates whether additional word delimiters should be used
	 */
	public boolean definesAdditionalWordDelimiter() {
		return getAdditionalWordDelimiter() != null;
	}
	
	/**
	 * Indictes whether non-alphabetic characters should be considered a word
	 * delimiter
	 */
	public boolean considerNonAlphabeticCharactersWordDelimiter() {
		return nonAlphabeticCharactersAreWordDelimiters;
	}
	
	/**
	 * Gets the brackets that should be checked for their content during
	 * formatting.<br>
	 * Every sub-array has the following format: <b>< OpeningBracket,
	 * ClosingBracket ></b>
	 */
	public char[][] getBracketsToFormat() {
		return bracketsToFormat;
	}
	
	/**
	 * Indicates whether {@link #getBracketsToFormat()} should be used in order
	 * to retrieve the bracket pairs to fomrat the name
	 */
	public boolean useSpecifiedBracketsOnly() {
		return getBracketsToFormat() != null;
	}
	
	/**
	 * Indicates whether existing files with the formatted name should be
	 * overwritten
	 */
	public boolean allowOverwriteOfExistingFilesOnRename() {
		return allowOverwriteOfExistingFiles;
	}
	
	/**
	 * Indicates whether the user should be asked before overwrinting a file
	 */
	public boolean askBeforeOverwritingFiles() {
		return askBeforeOverwritingFile;
	}
	
	/**
	 * Indicates whether the filename should be trimmed
	 */
	public boolean trimFilename() {
		return trim;
	}
	
	/**
	 * Gets the set of characters that should not be interpreted as wird
	 * delimiters
	 */
	public char[] getNonWordDelimiters() {
		return nonWordDelimiters;
	}
	
	/**
	 * Checks whether this rule specifies a set of characters that hsould not be
	 * interpreted as word delimiters
	 */
	public boolean specifiesNonWordDelimiters() {
		return getNonWordDelimiters() != null;
	}
	
	/**
	 * Checks whether the given character is a word delimiter
	 * 
	 * @param c
	 *            The character to check
	 */
	public boolean isWordDelimiter(char c) {
		if (specifiesNonWordDelimiters()) {
			for (char current : getNonWordDelimiters()) {
				if (c == current) {
					return false;
				}
			}
		}
		
		if (definesAdditionalWordDelimiter()) {
			for (char current : getAdditionalWordDelimiter()) {
				if (c == current) {
					return true;
				}
			}
		}
		
		if (considerNonAlphabeticCharactersWordDelimiter()
				&& !Character.isAlphabetic(c)) {
			return true;
		}
		
		return Character.isWhitespace(c);
	}
	
	/**
	 * Gets the prefixes that should be removed specified as a regular
	 * expression
	 */
	public String[] getPrefixRegexToRemove() {
		return prefixesToRemove;
	}
	
	/**
	 * Checks whether there are prefix regex given taht should be removed if
	 * matched
	 */
	public boolean specifiesPrefixRegex() {
		return getPrefixRegexToRemove() != null;
	}
}
