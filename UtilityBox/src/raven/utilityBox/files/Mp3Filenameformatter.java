package raven.utilityBox.files;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import raven.utilityBox.exceptions.FileDoesNotExistsException;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.preferences.rules.Mp3FilenameFormatterRule;
import raven.utilityBox.ui.UserInteraction;

public class Mp3Filenameformatter extends FilenameFormatter {

	/**
	 * The seperator used in order to seperate artist and track title
	 */
	public static final String SEPERATOR = "-";
	/**
	 * The prefix for temporary files created by this program
	 */
	public static final String TEMP_FILE_PREFIX = "tmp_file_raven_utitlity_box_";

	/**
	 * The title of the song as specified in the file name
	 */
	protected String fileTitle;
	/**
	 * The artist of the song as specified in the file name
	 */
	protected String fileArtist;
	/**
	 * The title of the song as specified in the mp3 tag of the file
	 */
	protected String tagTitle;
	/**
	 * The artist of the song as specified by the mp3 tag of the file
	 */
	protected String tagArtist;
	/**
	 * The mp3-file
	 */
	protected Mp3File mp3;
	/**
	 * Indicates whether the tag information have to be adapted to the file
	 * information
	 */
	protected boolean adaptTagInformation;


	public Mp3Filenameformatter(File file, Mp3FilenameFormatterRule rules)
			throws UnsupportedTagException, InvalidDataException, IOException {
		super(file, rules);

		if (!fileExtension.toLowerCase().equals("mp3")) {
			throw new IllegalArgumentException("The given file is not an mp3-file (determined by file extension)!");
		}

		mp3 = new Mp3File(file);
	}

	@Override
	public void format() {
		super.format();

		collectInformation();

		if (!fileNameNeedsFurtherFormatting() || informationAreSimplySwitched()) {
			return;
		}

		useTagOrFileInformation();

		// assemble the new file name out of the gathered information
		formattedFileName = new StringBuilder(fileArtist + " " + SEPERATOR + " " + fileTitle);

		// indicate that tag information have to be adapted
		adaptTagInformation = true;
	}

	@Override
	public boolean rename() throws FileDoesNotExistsException {
		if (getPreferences().migrateID3v1Information()) {
			if (migrateID3v1Information()) {
				adaptTagInformation = true;
			}
		}

		if (getPreferences().deleteID3v1Tags() && mp3.hasId3v1Tag()) {
			mp3.setId3v1Tag(null);
			adaptTagInformation = true;
		}

		if (getPreferences().clearAlbumCoverArt()) {
			if (mp3.hasId3v2Tag() && mp3.getId3v2Tag().getAlbumImage() != null) {
				mp3.getId3v2Tag().clearAlbumImage();
				adaptTagInformation = true;
			}
		}

		if (getPreferences().writeTags() && adaptTagInformation) {
			// rename file and update tag information
			return renameAndWriteTags();
		} else {
			// simply rename the file
			return super.rename();
		}
	}

	/**
	 * Migrates all information out of the ID3v1 tags into the ID3v2 tags when it is
	 * not already specified there
	 */
	protected boolean migrateID3v1Information() {
		if (!mp3.hasId3v1Tag()) {
			return false;
		}

		ID3v1 oldTag = mp3.getId3v1Tag();

		if (!mp3.hasId3v2Tag()) {
			mp3.setId3v2Tag(new ID3v24Tag());
		}

		ID3v2 newTag = mp3.getId3v2Tag();

		boolean changedSomething = false;

		if (newTag.getAlbum() == null) {
			newTag.setAlbum(oldTag.getAlbum());
			changedSomething = true;
		}
		if (newTag.getComment() == null) {
			newTag.setComment(oldTag.getComment());
			changedSomething = true;
		}
		String genre = oldTag.getGenreDescription();
		if (newTag.getGenreDescription() == null && !genre.toLowerCase().equals("unknown")) {
			newTag.setGenreDescription(genre);
			changedSomething = true;
		}
		if (newTag.getTrack() == null) {
			newTag.setTrack(oldTag.getTrack());
			changedSomething = true;
		}
		if (newTag.getYear() == null) {
			newTag.setYear(oldTag.getYear());
			changedSomething = true;
		}
		if (newTag.getGenre() < 0) {
			newTag.setGenre(oldTag.getGenre());
			changedSomething = true;
		}

		return changedSomething;
	}

	/**
	 * Renames the file and writes the artist and the title in it's mp3 tags
	 * 
	 * @return Whether the operation was successfull
	 */
	protected boolean renameAndWriteTags() {
		ID3v2 tag;
		if (mp3.hasId3v2Tag()) {
			tag = mp3.getId3v2Tag();
		} else {
			tag = new ID3v24Tag();
			mp3.setId3v2Tag(tag);
		}

		tag.setTitle(fileTitle);
		tag.setArtist(fileArtist);

		try {
			File tempFile = new File(file.getParentFile(), TEMP_FILE_PREFIX + file.getName());

			mp3.save(tempFile.getAbsolutePath());

			file.delete();
			file = tempFile;

			File targetFile = getRenamedFile(tempFile);

			if (checkRename(targetFile)) {
				try {
					return doRename(targetFile);
				} catch (FileDoesNotExistsException e) {
					e.printStackTrace();

					return false;
				}
			} else {
				// name the temp file back to it's original name
				tempFile.renameTo(new File(tempFile.getParentFile(),
						originalFilename + ((getFileExtension() != null) ? getFileExtension() : "")));
				return false;
			}
		} catch (NotSupportedException | IOException e) {
			e.printStackTrace();

			Logger.getDefault()
					.log(new LogMessage("Failed at renaming file: " + e.getMessage(), this, LogMessage.SEVERITY_ERROR));
		}

		return false;

	}

	/**
	 * Asks the user which of the information he/she wants to use for this song
	 */
	protected void useTagOrFileInformation() {
		if (tagArtist != null && !tagArtist.isEmpty() && !fileArtist.equals(tagArtist)) {
			if (fileArtist.toLowerCase().equals(tagArtist.toLowerCase())) {
				// use the case as specified in the file
				return;
			}

			if (tagTitle.replace(" ", "").equals(fileTitle.replace(" ", ""))) {
				// they only differ in spacing -> assume formatted file artist contains correct
				// spacing
				return;
			}

			UserInteraction question = new UserInteraction(
					"There are 2 possible artists for the file \"" + file.getAbsolutePath() + "\":\n" + "file: \""
							+ fileArtist + "\" - tag: \"" + tagArtist + "\"\n"
							+ "Do you want to keep the one specified by the file "
							+ "(\"No\" will automatically take the other one)?",
					SWT.YES | SWT.NO, true, fileNameValidator);

			if (question.open() == SWT.NO) {
				fileArtist = tagArtist;
			} else {
				if (question.customUserInputEntered()) {
					fileArtist = question.getCustomUserInput();
				}
			}
		}

		if (tagTitle != null && !tagTitle.isEmpty() && !fileTitle.equals(tagTitle)) {
			if (fileTitle.toLowerCase().equals(tagTitle.toLowerCase())) {
				// use the case as specified in the file
				return;
			}

			if (tagTitle.replace(" ", "").equals(fileTitle.replace(" ", ""))) {
				// they only differ in spacing -> assume formatted file title contains correct
				// spacing
				return;
			}

			UserInteraction question = new UserInteraction("There are 2 possible titles for the file \""
					+ file.getAbsolutePath() + "\":\n" + "file: \"" + fileTitle + "\" - tag: \"" + tagTitle + "\"\n"
					+ "Do you want to keep the one specified by the file "
					+ "(\"No\" will automatically take the other one)?\n"
					+ "Alternatively you can specify a new title.", SWT.YES | SWT.NO, true, fileNameValidator);

			if (question.open() == SWT.NO) {
				fileTitle = tagTitle;
			} else {
				if (question.customUserInputEntered()) {
					fileTitle = question.getCustomUserInput();
				}
			}
		}
	}

	/**
	 * Checks whether the information in the file name are simlply switched
	 */
	protected boolean informationAreSimplySwitched() {
		if (titleAndArtistAreSwitched()) {
			// switch title and artist in the file name
			formattedFileName = new StringBuilder(fileArtist + " " + SEPERATOR + " " + fileTitle);

			return true;
		}

		if (titleAndArtistAreSwitchedCaseInsensitive()) {
			// switch title and artist in the file name
			formattedFileName = new StringBuilder(fileArtist + " " + SEPERATOR + " " + fileTitle);

			// Adapt tags on rename
			adaptTagInformation = true;

			return true;
		}

		return false;
	}

	/**
	 * Checks whether the file name needs further formatting
	 */
	protected boolean fileNameNeedsFurtherFormatting() {
		if (!file.getName().equals(fileArtist + " " + SEPERATOR + " " + fileTitle + "." + getFileExtension())) {
			return true;
		}

		if (fileTitle.equals(tagTitle) && fileArtist.equals(tagArtist)) {
			// The file is already in a perfect format
			return false;
		}

		if (informationMatchCaseInsensitive()) {
			// Only adapt tags on rename
			adaptTagInformation = true;

			return false;
		}

		return true;
	}

	/**
	 * Checks whether the title and the artist are simply in the wrong order in the
	 * file name
	 */
	protected boolean titleAndArtistAreSwitched() {
		return fileArtist.equals(tagTitle) && fileTitle.equals(tagArtist);
	}

	/**
	 * Checks whether the title and the artist are simply in the wrong order in the
	 * file name
	 */
	protected boolean titleAndArtistAreSwitchedCaseInsensitive() {
		if (tagArtist == null || tagTitle == null) {
			return false;
		}

		return tagTitle.toLowerCase().equals(fileArtist.toLowerCase())
				&& tagArtist.toLowerCase().equals(fileTitle.toLowerCase());
	}

	/**
	 * Checks whether the tag and the file information are the same when ignoring
	 * case
	 */
	protected boolean informationMatchCaseInsensitive() {
		if (tagArtist == null || tagTitle == null) {
			return false;
		}

		return tagArtist.toLowerCase().equals(fileArtist.toLowerCase())
				&& tagTitle.toLowerCase().equals(fileTitle.toLowerCase());
	}

	/**
	 * Collects the necessary information that are needed in order to format the
	 * file name properly
	 */
	protected void collectInformation() {
		getTagInformation();

		StringBuilder artist;
		// check whether the file name does already specify artist + title
		if (formattedFileName.indexOf(SEPERATOR) >= 0) {
			if (formattedFileName.indexOf(" " + SEPERATOR + " ") >= 0) {
				// prefer the seperator surrounded by spaces
				artist = new StringBuilder(
						formattedFileName.substring(0, formattedFileName.indexOf(" " + SEPERATOR + " ")).trim());
				fileTitle = formattedFileName.substring(formattedFileName.indexOf(" " + SEPERATOR + " ") + 3).trim();
			} else {
				artist = new StringBuilder(formattedFileName.substring(0, formattedFileName.indexOf(SEPERATOR)).trim());
				fileTitle = formattedFileName.substring(formattedFileName.indexOf(SEPERATOR) + 1).trim();
			}
		} else {
			// Use file name as title and the tag artist as the artist
			fileTitle = formattedFileName.toString().trim();
			if (tagArtist == null) {
				artist = new StringBuilder();
			} else {
				artist = new StringBuilder(tagArtist);
			}

			// format artist if needed
			if (rules.transformFirstLettersToUpperCase()) {
				transformFirstLettersToUpperCase(artist);
			}
			if (rules.checkBracketContent()) {
				checkBracketContent(artist);
			}
		}

		if (fileArtist == null || fileArtist.isEmpty()) {
			fileArtist = artist.toString();
		} else {
			// a feat. has been found before
			fileArtist = artist + " " + fileArtist;
		}
	}

	/**
	 * Gets all necessary information out of the mp3 tags and stores them in the
	 * respective variables
	 */
	protected void getTagInformation() {
		tagTitle = getTagTitle();
		tagArtist = getTagArtist();

		if (tagTitle != null) {
			tagTitle = tagTitle.trim();
		}

		if (tagArtist != null) {
			tagArtist = tagArtist.trim();
		}
	}

	/**
	 * Gets the title of the song as specified in the mp3 tag of the song. If there
	 * is an ID3v1 and an ID3v2 tag the on of the ID3v2 tag will be read
	 */
	protected String getTagTitle() {
		if (mp3.hasId3v2Tag()) {
			return mp3.getId3v2Tag().getTitle();
		}

		if (mp3.hasId3v1Tag()) {
			return mp3.getId3v1Tag().getTitle();
		}

		return null;
	}

	/**
	 * Gets the artist of the song as specified in the mp3 tag of the song. If there
	 * is an ID3v1 and an ID3v2 tag the on of the ID3v2 tag will be read
	 */
	protected String getTagArtist() {
		if (mp3.hasId3v2Tag()) {
			return mp3.getId3v2Tag().getArtist();
		}

		if (mp3.hasId3v1Tag()) {
			return mp3.getId3v1Tag().getArtist();
		}

		return null;
	}

	@Override
	public Mp3FilenameFormatterRule getPreferences() {
		return (Mp3FilenameFormatterRule) rules;
	}

	@Override
	protected boolean preserveBracket(String bracketContent, boolean caseSensitive) {
		// Check if any featuring information have to be stripped out of this
		// bracket
		if (getPreferences().checkFeaturing()) {
			for (String currentFeat : getPreferences().getFeaturingIndicators()) {
				String contentCopy;
				if (!caseSensitive) {
					currentFeat = currentFeat.toLowerCase();
					contentCopy = bracketContent.toLowerCase();
				} else {
					contentCopy = bracketContent;
				}

				// try to match a featuring pattern
				if (Pattern.compile("\\b" + currentFeat + "\\b").matcher(contentCopy).find()) {
					// strip the featuring out of the content (without brackets)
					processFeaturing(bracketContent.substring(1, bracketContent.length() - 1), currentFeat,
							caseSensitive);

					continue;
				}
			}
		}

		// determine whether to preserve this bracket
		return super.preserveBracket(bracketContent, caseSensitive);
	}

	/**
	 * Processes the mentioning of a featuring in the given content
	 * 
	 * @param content
	 *            The content to process (Without unnecessary brackets)
	 * @param indicator
	 *            The respective featuring indicator
	 * @param caseSensitive
	 *            Indicates whether the processing should be done case-sensitively
	 */
	protected void processFeaturing(String content, String indicator, boolean caseSensitive) {
		Matcher matcher = Pattern.compile("\\b" + ((caseSensitive) ? indicator : indicator.toLowerCase()) + "\\b")
				.matcher((caseSensitive) ? content : content.toLowerCase());

		if (matcher.find()) {
			String featuring = content.substring(matcher.end()).trim();

			while (featuring.startsWith(".")) {
				featuring = featuring.substring(1).trim();
			}

			featuring = getFeaturingIndicatorCase(indicator) + " " + featuring;

			featuring = formatFeaturing(featuring);

			if (fileArtist == null || matcher.start() > 0) {
				// There is no artist yet or the bracket specifies the complete
				// artist(s)
				fileArtist = featuring;
			} else {
				fileArtist += " " + featuring;
			}
		}
	}

	/**
	 * Gets the respwctive featuring indicator in the proper case
	 * 
	 * @param indicator
	 *            The indicator to search for
	 * @return The indicator in the proper case
	 */
	protected String getFeaturingIndicatorCase(String indicator) {
		indicator = indicator.toLowerCase();

		if (getPreferences().checkFeaturing()) {
			for (String currentIndicator : getPreferences().getFeaturingIndicators()) {
				if (currentIndicator.toLowerCase().equals(indicator)) {
					return currentIndicator;
				}
			}
		}

		return null;
	}

	/**
	 * Formats a featuring statement
	 * 
	 * @param featuring
	 *            The statement to format
	 * @return The formatted statement
	 */
	protected String formatFeaturing(String featuring) {
		if (rules.transformFirstLettersToUpperCase()) {
			StringBuilder featureBuilder = new StringBuilder(featuring);

			super.transformFirstLettersToUpperCase(featureBuilder);

			featuring = featureBuilder.toString();
		}

		for (String currentIndicator : getPreferences().getFeaturingIndicators()) {
			Matcher matcher = Pattern.compile("\\b" + Pattern.quote(currentIndicator) + "\\b")
					.matcher(featuring.toLowerCase());

			while (matcher.find()) {
				// write the indicator in proper case
				featuring = featuring.substring(0, matcher.start()) + currentIndicator
						+ featuring.substring(matcher.end());
			}

			// follow the dot rule
			if (getPreferences().endFeaturingWithDot()) {
				featuring = featuring.replaceAll("\\b" + Pattern.quote(currentIndicator) + "\\s",
						currentIndicator + ". ");
			} else {
				featuring = featuring.replaceAll("\\b" + Pattern.quote(currentIndicator) + "\\.", currentIndicator);
			}
		}

		return featuring;
	}

	@Override
	protected void logRename(String oldName, String newName, String directory) {
		// Log the old file name without the temp-file-prefix
		super.logRename(oldName.replace(TEMP_FILE_PREFIX, ""), newName, directory);
	}

	@Override
	protected void transformFirstLettersToUpperCase(StringBuilder builder) {
		super.transformFirstLettersToUpperCase(builder);

		if (getPreferences().checkFeaturing()) {
			String formatted = formatFeaturing(builder.toString());
			builder.setLength(0);
			builder.append(formatted);
		}
	}
}
