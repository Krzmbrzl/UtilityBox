package raven.utilityBox.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import raven.utilityBox.enums.EStatus;
import raven.utilityBox.interfaces.ITypeConverter;
import raven.utilityBox.logging.LogMessage;
import raven.utilityBox.logging.Logger;
import raven.utilityBox.openDocument.ODSExtractor;
import raven.utilityBox.preferences.AbstractPreference;
import raven.utilityBox.preferences.rules.ExtractODSToCSVRule;
import raven.utilityBox.table.Table;
import raven.utilityBox.table.TableNullDivider;

/**
 * This action will extract the data out of a .ods spreadsheet, will subdivide
 * it into different data-sets (sub-tables - see {@link TableNullDivider}) and
 * then written into .csv files
 * 
 * @author Raven
 *
 */
public class ExtractODSToCSVAction extends AbstractPreferenceSensitiveAction {

	/**
	 * The name of the file containing the info about previous runs of this action
	 */
	public static final String TIMESTAMP_FILE = ".timestamp";
	/**
	 * The shortcut indicating the home-directory of the current user
	 */
	public static final String HOME = "$$HOME$$";

	/**
	 * The spread-sheet to extract the data from
	 */
	private String spreadSheetPath;
	/**
	 * The target directory the generated CSVs should be stored in
	 */
	protected File csvTargetDir;
	/**
	 * Indicating whether this action should check whether there have been changes
	 * since it ran the last time on this spreadsheet
	 */
	protected boolean checkTimestamp;
	/**
	 * The replacement to use for empty cells
	 */
	protected String emptyCellReplacement;
	/**
	 * Indicates that the transposed tables should also be generated
	 */
	protected boolean addTransposed;

	/**
	 * The flag to use for the second argument in order to indicate that the
	 * generated csv files should be put into the same directory as the original
	 * spreadsheet is
	 */
	public static final String USE_SAME_DIR = "!<<<UseSameDir>>>!";


	/**
	 * Creates a new instance of this action. The generated CSVs will be stored in
	 * the same directory as the spreadsheet.
	 * 
	 * @param path
	 *            The path to the spreadsheet to use
	 */
	public ExtractODSToCSVAction(String path) {
		setParameter(new Object[] { path });
	}

	/**
	 * Creates a new instance of this action.
	 * 
	 * @param path
	 *            The path to the spreadsheet to use
	 * @param dir
	 *            The path to the directory the generated CSVs should be stored in.
	 *            If this is a relative path it will use the directory of the
	 *            spreadsheet as the starting directory.
	 */
	public ExtractODSToCSVAction(String path, String dir) {
		setParameter(new Object[] { path, dir });
	}

	public ExtractODSToCSVAction() {
		// empty constructor
	}

	public ExtractODSToCSVAction(File target) {
		setParameter(new Object[] { target.getAbsolutePath() });
	}

	@Override
	protected EStatus doRun() {
		try {
			if (!csvTargetDir.exists()) {
				csvTargetDir.mkdirs();
			}

			if (checkTimestamp && !checkTimestamp()) {
				Logger.getDefault()
						.log(new LogMessage(
								"Aborting action as the original spreadsheet has not been modified since the last run",
								this, LogMessage.SEVERITY_INFO));
				return EStatus.OK;
			}

			ExtractODSToCSVRule rules = (ExtractODSToCSVRule) getPreferenceRules();

			List<Table<String>> subTables = ODSExtractor.extractSubtables(spreadSheetPath);

			int tableCounter = 0;

			Logger.getDefault().log(new LogMessage("Detected " + subTables.size() + " sub-tables in " + spreadSheetPath, this,
					LogMessage.SEVERITY_INFO));

			for (Table<String> currentTable : subTables) {
				String name = extractNameAndFormat(currentTable);

				if (name == null) {
					name = "Table" + tableCounter;
				}

				// append file extension
				name = name + ".csv";

				for (int i = 0; i < (addTransposed ? 2 : 1); i++) {
					if (i == 1) {
						// its the transposed table's turn
						name = name.replace(".csv", "_t.csv");
						currentTable.transpose();
					}
					// write the table to file
					File targetFile = new File(csvTargetDir, name);

					targetFile.createNewFile();

					FileOutputStream out = new FileOutputStream(targetFile);
					
					String content = currentTable
							.toCSV(rules.getColumnDelimiter(), rules.getDelimiterReplacement(), emptyCellReplacement);
						
					// escape all percent signs that have not been escaped already
					content = content.replaceAll("(^|[^\\\\]|(?:^|[^\\\\])(?:\\\\\\\\)+)(%)", "$1\\\\$2");

					out.write(content.getBytes());

					out.close();

					Logger.getDefault()
							.log(new LogMessage("Wrote content of" + (i == 1 ? " transposed" : "") + " sub-table "
									+ tableCounter + " to " + targetFile.getAbsolutePath(),this, LogMessage.SEVERITY_INFO));
				}

				tableCounter++;
			}

		} catch (IOException | IllegalAccessException e) {
			e.printStackTrace();

			Logger.getDefault().log(e, this);

			return EStatus.ERROR;
		}

		return EStatus.OK;
	}

	/**
	 * Checks whether this action even has to run (by checking the timestamp)
	 * 
	 * @return Whether this action should run
	 */
	protected boolean checkTimestamp() {
		File timeStamp = new File(csvTargetDir, TIMESTAMP_FILE);
		File spreadSheet = new File(spreadSheetPath);
		boolean runAction = false;

		try {
			if (!timeStamp.exists()) {
				// Create the file
				timeStamp.createNewFile();

				// write respective info in it
				FileOutputStream out = new FileOutputStream(timeStamp);

				out.write(("\"" + spreadSheetPath.replace(System.getProperty("user.home"), HOME) + "\" - "
						+ System.currentTimeMillis()).getBytes());

				out.close();

				return true;
			}

			// check if there already is any entry and determine whether to run this action
			StringBuilder builder = new StringBuilder();

			BufferedReader reader = new BufferedReader(new FileReader(timeStamp));
			String currentLine = null;
			boolean currentSpreadSheetListed = false;

			while ((currentLine = reader.readLine()) != null) {
				String spreadSheetPath = currentLine.substring(1, currentLine.indexOf("\" - ")).replace(HOME,
						System.getProperty("user.home"));

				if (this.spreadSheetPath.equals(spreadSheetPath)) {
					currentSpreadSheetListed = true;

					// It is the same reference -> check the timestamp
					int index = currentLine.indexOf("\" - ") + 4;
					long stamp = Long.parseLong(currentLine.substring(index));

					if (stamp < spreadSheet.lastModified()) {
						runAction = true;

						// update timestamp
						currentLine = currentLine.replace(String.valueOf(stamp),
								String.valueOf(System.currentTimeMillis()));
					}
				}

				// add line to buffer
				builder.append(currentLine + "\n");
			}

			reader.close();

			if (!runAction && currentSpreadSheetListed) {
				// No need to re-write the timestamp-file
				return false;
			}

			if (!currentSpreadSheetListed) {
				// add entry for the current spreadSheet
				builder.append("\"" + spreadSheetPath.replace(System.getProperty("user.home"), HOME) + "\" - "
						+ System.currentTimeMillis());

				// action obviously has to run
				runAction = true;
			}

			FileOutputStream out = new FileOutputStream(timeStamp);

			out.write(builder.toString().trim().getBytes());

			out.close();
		} catch (IOException e) {
			e.printStackTrace();

			Logger.getDefault().log(e, this);
		}

		return runAction;
	}

	/**
	 * Tries to extract a name for the given table as specified in the first row or
	 * column. An entry is considered a name if it is the only entry in the
	 * respective row/column.
	 * 
	 * If this method finds a name it will remove the respective row/column and will
	 * trim the table so that it won't contain any completely empty rows/tables
	 * 
	 * @param table
	 *            The table whose name should be obtained
	 * @return The name of the table or <code>null</code> if none could be found
	 * @throws IllegalAccessException
	 */
	protected String extractNameAndFormat(Table<String> table) throws IllegalAccessException {
		String name = extractNameFromData(table.getRow(0));

		if (name != null) {
			table.deleteRow(0);
			table.trim();

			name = name.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");

			return name;
		}

		name = extractNameFromData(table.getColumn(0));

		if (name != null) {
			table.deleteColumn(0);
			table.trim();

			return name;
		}

		return null;
	}

	/**
	 * Extracts the name of the respective data set out of a data row/column. A name
	 * is an non-null entry inside a row/column otherwise filled with null-elements
	 * 
	 * @param dataSet
	 *            The row/column to search for the name
	 * @return The name or <code>null</code> if none could be found
	 */
	private String extractNameFromData(String[] dataSet) {
		String name = null;

		if (dataSet.length < 2) {
			return null;
		}

		for (String currentData : dataSet) {
			if (currentData != null) {
				if (name != null) {
					return null;
				} else {
					name = currentData;
				}
			}
		}

		// format name
		if (name != null) {
			// remove slashes
			name = name.replaceAll("[/\\\\]", "").trim();

			// replace blanks with underscores
			name = name.replace(" ", "_");

			if (name.isEmpty()) {
				// it's not a useful name
				name = null;
			}
		}

		return name;
	}

	@Override
	public Class<?> getPreferenceType() {
		return ExtractODSToCSVRule.class;
	}

	@Override
	public AbstractPreference getDefaultPreferences() {
		AbstractPreference preferences = new ExtractODSToCSVRule();
		preferences.initializeWithDefaults();

		return preferences;
	}

	@Override
	public boolean validateParameter(Object[] args) {
		if (args == null) {
			Logger.getDefault()
					.log(new LogMessage("The parameter array must not be null!", this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		if (args.length != getDefaultParameter().length) {
			Logger.getDefault()
					.log(new LogMessage("Expected the parameter array to contain " + getDefaultParameter().length
							+ " elements (got " + args.length + ")!", this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		if (!(args[0] instanceof String)) {
			Logger.getDefault().log(
					new LogMessage("Expected first argument to be of type String!", this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		File targetFile = new File((String) args[0]);

		if (!targetFile.exists()) {
			Logger.getDefault().log(new LogMessage("The specified path doesn't exist! (" + args[0] + ")", this,
					LogMessage.SEVERITY_ERROR));

			return false;
		}

		if (!targetFile.isFile()) {
			Logger.getDefault().log(new LogMessage("The specified path does not point to a file! (" + args[0] + ")",
					this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		String extension = (targetFile.getName().contains(".")
				? targetFile.getName().substring(targetFile.getName().lastIndexOf(".") + 1)
				: "");

		if (!extension.toLowerCase().equals("ods")) {
			Logger.getDefault()
					.log(new LogMessage("The given file is not a .ods spreadsheet!", this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		if (!(args[1] instanceof String)) {
			Logger.getDefault().log(
					new LogMessage("Expected second argument to be of type String!", this, LogMessage.SEVERITY_ERROR));

			return false;
		}

		String csvTarget = (String) args[1];

		if (csvTarget.equals(USE_SAME_DIR)) {
			// use the directory the original spreadsheet is in for storing the generated
			// CSVs
			args[1] = targetFile.getParent();
			csvTarget = (String) args[1];
		}

		if (!(args[2] instanceof Boolean)) {
			Logger.getDefault()
					.log(new LogMessage("Expected third argument to be of type Boolean (got \"" + args[2] + "\")!",
							this, LogMessage.SEVERITY_ERROR));
		}

		if (!(args[4] instanceof Boolean)) {
			Logger.getDefault()
					.log(new LogMessage("Expected fifth argument to be of type Boolean (got \"" + args[4] + "\")!",
							this, LogMessage.SEVERITY_ERROR));
		}

		return true;
	}

	@Override
	protected void applyParameter(Object[] args) {
		spreadSheetPath = (String) args[0];

		File csvTarget = new File((String) args[1]);

		if (!csvTarget.isAbsolute()) {
			// if a relative path is specified interpret it starting at the directory the
			// spreadsheet is in
			csvTarget = new File(new File((String) args[0]).getParentFile(), (String) args[1]);
		}

		this.csvTargetDir = csvTarget;

		this.checkTimestamp = (Boolean) args[2];

		this.emptyCellReplacement = (String) args[3];

		this.addTransposed = (Boolean) args[4];
	}

	@Override
	public Object[] getDefaultParameter() {
		return new Object[] { null, USE_SAME_DIR, true, "", false };
	}

	@Override
	protected String[] getParameterKeys() {
		return new String[] { "spreadSheet", "targetDir", "checkTimestamp", "replaceEmpty", "addTransposed" };
	}

	@Override
	protected ITypeConverter<String, Object>[] getParameterConverters() {
		@SuppressWarnings("unchecked")
		ITypeConverter<String, Object>[] converter = (ITypeConverter<String, Object>[]) Array
				.newInstance(ITypeConverter.class, 5);

		converter[0] = new ITypeConverter<String, Object>() {

			@Override
			public Object convert(String input) {
				return input;
			}
		};

		converter[1] = converter[0];

		converter[2] = new ITypeConverter<String, Object>() {

			@Override
			public Object convert(String input) {
				return Boolean.parseBoolean(input);
			}
		};

		converter[3] = converter[0];

		converter[4] = converter[2];

		return converter;
	}

	@Override
	public boolean requiresParameter() {
		return true;
	}

}
