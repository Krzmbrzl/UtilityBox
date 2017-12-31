package raven.utilityBox.preferences.rules;

public class Mp3FilenameFormatterRule extends FilenameFormatterRule {
	
	/**
	 * An array of prefixes indicating a featuring
	 */
	protected String[] featuringIndicator;
	/**
	 * Indicates whether the {@link #featuringIndicator} should be terminated by
	 * a dot
	 */
	protected boolean endFeaturingWithDot;
	/**
	 * Indicating whether the program should (re)write the tags of the mp3
	 */
	protected boolean writeTags;
	/**
	 * Indicates whether the old ID3v1 tags should be kept instead of deleted
	 */
	protected boolean keepId3v1Tags;
	/**
	 * Indicates whether the information in the old ID3v1 tags should be
	 * migrated into the new ID3v2 tags when they are not yet specified there
	 */
	protected boolean migrateId3v1TagInformation;
	/**
	 * Indicates whether album cover arts should be cleared
	 */
	protected boolean clearAlbumCoverArt;
	
	
	public Mp3FilenameFormatterRule() {
		super();
	}
	
	@Override
	public void initialize() {
		// TODO
	}
	
	@Override
	public void initializeWithDefaults() {
		super.initializeWithDefaults();
		
		allowedBracketContent = new String[] { "mix", "edit", "version",
				"mashup", "intro", "bootleg", "unplugged", "cover", "explicit" };
		prefixesToRemove = new String[] { "^\\s*[0-9]+\\s+-?" };
		featuringIndicator = new String[] { "feat", "vs" };
		endFeaturingWithDot = true;
		writeTags = true;
		keepId3v1Tags = false;
		migrateId3v1TagInformation = true;
		clearAlbumCoverArt = true;
	}
	
	/**
	 * Gets an array of indicators for a featuring
	 */
	public String[] getFeaturingIndicators() {
		return featuringIndicator;
	}
	
	/**
	 * Checks whether featuring should be taken into consideration
	 */
	public boolean checkFeaturing() {
		return getFeaturingIndicators() != null;
	}
	
	/**
	 * Indicates whether the featuring prefixes should be terminated with a dot
	 */
	public boolean endFeaturingWithDot() {
		return endFeaturingWithDot;
	}
	
	/**
	 * Checks whether the program should (re)write the tags of the mp3
	 */
	public boolean writeTags() {
		return writeTags;
	}
	
	/**
	 * Checks whether the old ID3v1 tags should be deleted
	 */
	public boolean deleteID3v1Tags() {
		return !keepId3v1Tags;
	}
	
	/**
	 * Checks whether information that is only specified in the ID3v1 tags
	 * should be migrated into the new ID3v2 tags
	 */
	public boolean migrateID3v1Information() {
		return migrateId3v1TagInformation;
	}
	
	/**
	 * Checks whether the album cover art should be cleared
	 */
	public boolean clearAlbumCoverArt() {
		return clearAlbumCoverArt;
	}
}
