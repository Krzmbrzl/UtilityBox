package raven.utilityBox.actions;

import java.io.File;
import java.io.IOException;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import raven.utilityBox.files.FilenameFormatter;
import raven.utilityBox.files.Mp3Filenameformatter;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.preferences.AbstractPreference;
import raven.utilityBox.preferences.rules.Mp3FilenameFormatterRule;

public class Mp3FilenameFormatAction extends FilenameFormatAction {

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
	public Mp3FilenameFormatAction(File file, boolean includeSubdirectories, int maxSubLevels) {
		super(file, includeSubdirectories, maxSubLevels);
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
	public Mp3FilenameFormatAction(File file, boolean includeSubdirectories) {
		super(file, includeSubdirectories);
	}

	/**
	 * Creates this file renamer on the given file. See {@link #defaultArgs} for
	 * what the default values for the unspecified fields is.
	 * 
	 * @param file
	 *            The file or directory to work on
	 */
	public Mp3FilenameFormatAction(File file) {
		super(file);
	}

	public Mp3FilenameFormatAction() {
		// empty constructor
	}

	@Override
	public boolean processFileType(String type) {
		return type.toLowerCase().equals("mp3");
	}

	@Override
	public FilenameFormatter getFormatter(File file) {
		try {
			return new Mp3Filenameformatter(file, (Mp3FilenameFormatterRule) getPreferenceRules());
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();

			Logger.getDefault().log(e, this);

			throw new IllegalArgumentException("Can't instanitate mp3 filename formatter!", e);
		}
	}

	@Override
	public Class<?> getPreferenceType() {
		return Mp3FilenameFormatterRule.class;
	}

	@Override
	public AbstractPreference getDefaultPreferences() {
		AbstractPreference preferences = new Mp3FilenameFormatterRule();
		preferences.initializeWithDefaults();

		return preferences;
	}
}
