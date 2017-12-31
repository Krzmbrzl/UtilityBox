package raven.utilityBox.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TableDivider<T> {
	/**
	 * The table to divide
	 */
	private Table<T> mainTable;


	public TableDivider(Table<T> table) {
		mainTable = table;
	}


	/**
	 * Checks whether a cell with the given content is considered as an empty cell
	 * 
	 * @param content
	 *            The content to be evaluated
	 * @return Whether the given content should be considered as "empty"
	 */
	protected abstract boolean isEmpty(Object content);


	/**
	 * Divides the table into its sub-tables (delimited by empty columns and rows)
	 * 
	 * @return A list of all sub-tables
	 * @throws IllegalAccessException 
	 */
	public List<Table<T>> divide() throws IllegalAccessException {
		return doDivide(mainTable);
	}

	/**
	 * Divides the table into its sub-tables (delimited by empty columns and rows)
	 * 
	 * @param table
	 *            The table to divide
	 * @return A list of all sub-tables
	 * @throws IllegalAccessException 
	 */
	protected List<Table<T>> doDivide(Table<T> table) throws IllegalAccessException {

		List<Table<T>> subTables = new ArrayList<Table<T>>();

		List<Integer> emptyColumns = getEmpties(table.getColumnIterator());
		List<Integer> emptyRows = getEmpties(table.getRowIterator());

		// split at empty columns first
		List<Table<T>> firstCut = new ArrayList<>();
		for (int i = 0; i < emptyColumns.size(); i++) {
			firstCut.add(table.copy(0, table.getRowCount() - 1, ((i == 0) ? 0 : emptyColumns.get(i - 1) + 1),
					emptyColumns.get(i) - 1));
		}
		// add last section
		if (emptyColumns.size() > 0) {
			firstCut.add(table.copy(0, table.getRowCount() - 1, emptyColumns.get(emptyColumns.size() - 1) + 1,
					table.getColumnCount() - 1));
		} else {
			firstCut.add(table.copy(0, table.getRowCount() - 1, 0, table.getColumnCount() - 1));
		}


		// now split the sub-tables at the empty rows
		List<Table<T>> secondCut = new ArrayList<>();
		for (Table<T> currentTable : firstCut) {
			if (currentTable.isValid()) {
				continue;
			}

			for (int i = 0; i < emptyRows.size(); i++) {
				secondCut.add(currentTable.copy(((i == 0) ? 0 : emptyRows.get(i - 1) + 1), emptyRows.get(i) - 1, 0,
						currentTable.getColumnCount() - 1));
			}
			// add last section
			if (emptyRows.size() > 0) {
				secondCut.add(currentTable.copy(emptyRows.get(emptyRows.size() - 1) + 1, currentTable.getRowCount() - 1,
						0, currentTable.getColumnCount() - 1));
			} else {
				secondCut.add(
						currentTable.copy(0, currentTable.getRowCount() - 1, 0, currentTable.getColumnCount() - 1));
			}
		}

		// filter out empty tables and check if further dividing is necessary
		Iterator<Table<T>> it = secondCut.iterator();
		while (it.hasNext()) {
			Table<T> currentTable = it.next();

			if (currentTable.isValid()) {
				it.remove();
			} else {
				emptyColumns = getEmpties(currentTable.getColumnIterator());
				emptyRows = getEmpties(currentTable.getRowIterator());

				if (emptyColumns.size() > 0 || emptyRows.size() > 0) {
					// table can be divided further
					subTables.addAll(doDivide(currentTable));
				} else {
					subTables.add(currentTable);
				}
			}
		}


		return subTables;
	}

	/**
	 * Gets the indices of the empty data sets within the iterated data set
	 * 
	 * @param it
	 *            The iterator to iterate through the data set
	 * @return A list of indices of the empty segments
	 */
	protected List<Integer> getEmpties(Iterator<T[]> it) {
		List<Integer> empties = new ArrayList<Integer>();

		int emptyIndex = -1;
		outerLoop: while (it.hasNext()) {
			emptyIndex++;

			T[] column = it.next();

			for (T currentContent : column) {
				if (!isEmpty(currentContent)) {
					// There is a non-empty element in this column -> can't divide in this column
					continue outerLoop;
				}
			}
			// the column is empty
			empties.add(emptyIndex);
		}

		return empties;
	}
}
