package raven.utilityBox.actions;

import java.io.File;
import java.io.FileOutputStream;
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
	 * The spread-sheet to extract the data from
	 */
	private String spreadSheetPath;
	/**
	 * The target directory the generated CSVs should be stored in
	 */
	protected File csvTargetDir;

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
			ExtractODSToCSVRule rules = (ExtractODSToCSVRule) getPreferenceRules();

			List<Table<String>> subTables = new TableNullDivider<String>(ODSExtractor.extract(spreadSheetPath))
					.divide();

			int tableCounter = 0;

			Logger.getDefault().log(new LogMessage("Detected " + subTables.size() + " sub-tables in " + spreadSheetPath,
					LogMessage.SEVERITY_INFO));

			if (!csvTargetDir.exists()) {
				csvTargetDir.mkdirs();
			}

			for (Table<String> currentTable : subTables) {
				String name = extractNameAndFormat(currentTable);

				if (name == null) {
					name = "Table" + tableCounter;
				}

				// append file extension
				name = name + ".csv";

				// write the table to file
				File targetFile = new File(csvTargetDir, name);

				targetFile.createNewFile();

				FileOutputStream out = new FileOutputStream(targetFile);

				out.write(currentTable.toCSV(rules.getColumnDelimiter(), rules.getDelimiterReplacement()).getBytes());

				out.close();

				Logger.getDefault()
						.log(new LogMessage(
								"Wrote content of sub-table " + tableCounter + " to " + targetFile.getAbsolutePath(),
								LogMessage.SEVERITY_INFO));

				tableCounter++;
			}

		} catch (IOException | IllegalAccessException e) {
			e.printStackTrace();

			Logger.getDefault().log(e);

			return EStatus.ERROR;
		}

		return EStatus.OK;
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

		if (args.length != 2) {
			Logger.getDefault()
					.log(new LogMessage("Expected the parameter array to contain 2 elements (got " + args.length + ")!",
							this, LogMessage.SEVERITY_ERROR));

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
	}

	@Override
	public Object[] getDefaultParameter() {
		return new Object[] { null, USE_SAME_DIR };
	}

	@Override
	protected String[] getParameterKeys() {
		return new String[] { "spreadSheet", "targetDir" };
	}

	@Override
	protected ITypeConverter<String, Object>[] getParameterConverters() {
		@SuppressWarnings("unchecked")
		ITypeConverter<String, Object>[] converter = (ITypeConverter<String, Object>[]) Array
				.newInstance(ITypeConverter.class, 2);

		converter[0] = new ITypeConverter<String, Object>() {

			@Override
			public Object convert(String input) {
				return input;
			}
		};

		converter[1] = converter[0];

		return converter;
	}

	@Override
	public boolean requiresParameter() {
		return true;
	}

}
