package raven.utilityBox.openDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import raven.utilityBox.table.Table;
import raven.utilityBox.table.TableDivider;
import raven.utilityBox.table.TableNullDivider;

public class ODSExtractor {

	/**
	 * Extracts the data from the spreadsheet at the specified path in form of a
	 * {@link Table}. Every sheet inside the spreadsheet will be represented by its
	 * own Table.
	 * 
	 * @param path
	 *            The path to the spreadsheet
	 * @return A List of tables with the data corresponding to the spreadsheet
	 * @throws IOException
	 */
	public static List<Table<String>> extract(String path) throws IOException {
		List<Table<String>> tables = new ArrayList<>();

		File spreadsheetFile = new File(path);
		final SpreadSheet spreadsheet = SpreadSheet.createFromFile(spreadsheetFile);

		for (int k = 0; k < spreadsheet.getSheetCount(); k++) {

			Sheet sheet = spreadsheet.getSheet(k);

			int columns = sheet.getColumnCount();
			int rows = sheet.getRowCount();

			// gather the content of the respective cells
			List<List<String>> data = new ArrayList<List<String>>();

			for (int i = 0; i < rows; i++) {
				List<String> currentRow = new ArrayList<String>();

				for (int j = 0; j < columns; j++) {
					String content = sheet.getCellAt(j, i).getTextValue();
					currentRow.add(content.isEmpty() ? null : content);
				}
				data.add(currentRow);
			}

			String[][] arrayData = new String[data.size()][];

			for (int i = 0; i < data.size(); i++) {
				arrayData[i] = data.get(i).toArray(new String[data.get(i).size()]);
			}

			// create a table out of the gathered data
			tables.add(new Table<>(String.class, arrayData));
		}

		return tables;
	}

	/**
	 * Extracts the data from the spreadsheet at the specified path in form of a
	 * {@link Table}. The extracted table will then be split into its sub-tables
	 * 
	 * @param path
	 *            The path to the spreadsheet
	 * @return All sub-tables corresponding to the spreadsheet
	 * @throws IOException
	 * @throws IllegalAccessException 
	 */
	public static List<Table<String>> extractSubtables(String path) throws IOException, IllegalAccessException {
		List<Table<String>> subTables = new ArrayList<>();

		List<Table<String>> tables = extract(path);

		for (Table<String> currentTable : tables) {
			// divide table into all possible sub-tables
			TableDivider<String> divider = new TableNullDivider<String>(currentTable);
			subTables.addAll(divider.divide());
		}

		return subTables;
	}

	public static void main(String[] args) {
		try {
			List<Table<String>> subtables = extractSubtables(
					System.getProperty("user.home") + "/Documents/Programming/TestingResources/TestSpreadsheet.ods");

			for (Table<String> currenttable : subtables) {
				System.out.println(currenttable.toCSV() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
