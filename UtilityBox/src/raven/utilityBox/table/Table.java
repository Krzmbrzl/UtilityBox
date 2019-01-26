package raven.utilityBox.table;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Table<T> {

	/**
	 * The amount of columns the untransposed table has
	 */
	private int columns;
	/**
	 * The amount of rows the untransposed table has
	 */
	private int rows;
	/**
	 * Indicates whether this table is currently transposed
	 */
	private boolean isTransposed;
	/**
	 * The actual table holding the data
	 */
	private T[][] dataTable;
	/**
	 * The datatype used in this table
	 */
	private Class<T> dataType;


	public Table(Class<T> type) {
		dataType = type;
	}

	public Table(Class<T> type, T[][] data) {
		this(type);
		setData(data);
	}

	/**
	 * Sets the data of this table
	 * 
	 * @param data
	 *            The data to use for this table
	 */
	@SuppressWarnings("unchecked")
	public void setData(T[][] data) {
		isTransposed = false;
		if (data != null) {
			rows = data.length;
			columns = getColumnCount(data);

			dataTable = (T[][]) Array.newInstance(dataType, rows, 0);

			// make sure each row contains the respective amount of columns
			for (int i = 0; i < rows; i++) {
				dataTable[i] = Arrays.copyOf(data[i], columns);
			}
		} else {
			rows = 0;
			columns = 0;
			dataTable = null;
		}
	}

	/**
	 * Gets the raw-data-array used by this table
	 */
	protected T[][] getData() {
		return dataTable;
	}

	/**
	 * Gets the amount of columns the given table contains. In order to determine
	 * that the array's first dimension is being iterated and the second's length is
	 * checked. The maximum length is the returned column count
	 * 
	 * @param table
	 *            The table to check
	 * @return The column count
	 */
	public static int getColumnCount(Object[][] table) {
		int cols = -1;

		for (Object[] current : table) {
			if (current.length > cols) {
				cols = current.length;
			}
		}

		return cols;
	}

	/**
	 * Transposes this table
	 * 
	 * @throws IllegalAccessException
	 */
	public void transpose() throws IllegalAccessException {
		validateTableAccess();

		isTransposed = true;
	}

	/**
	 * Indicates whether this table is currently transposed
	 */
	public boolean isTransposed() {
		return isTransposed;
	}

	/**
	 * Gets the element at the specified position. Out-of-bound indices are treated
	 * as references to empty elements
	 * 
	 * @param row
	 *            The element's row
	 * @param column
	 *            The element's column
	 * @return The element (may be <code>null</code> to indicate an empty element)
	 * @throws IllegalAccessException
	 */
	public T get(int row, int column) throws IllegalAccessException {
		validateTableAccess();

		if (isTransposed()) {
			return dataTable[column][row];
		} else {
			return dataTable[row][column];
		}
	}

	/**
	 * Gets the row count of this table
	 * 
	 * @throws IllegalAccessException
	 */
	public int getRowCount() throws IllegalAccessException {
		validateTableAccess();

		return isTransposed() ? columns : rows;
	}

	/**
	 * Gets the column count of this table
	 * 
	 * @throws IllegalAccessException
	 */
	public int getColumnCount() throws IllegalAccessException {
		validateTableAccess();

		return isTransposed() ? rows : columns;
	}

	/**
	 * Gets the row with the given index
	 * 
	 * @param index
	 *            The index of the row to obtain
	 * @return The respective row
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public T[] getRow(int index) throws IllegalAccessException {
		validateTableAccess();

		if (index >= getRowCount()) {
			return (T[]) Array.newInstance(dataType, getColumnCount());
		}

		if (isTransposed()) {
			T[] row = (T[]) Array.newInstance(dataType, getColumnCount());

			for (int i = 0; i < rows; i++) {
				row[i] = dataTable[i][index];
			}

			return row;
		} else {
			return Arrays.copyOf(dataTable[index], dataTable[index].length);
		}
	}

	/**
	 * Gets the column with the given index
	 * 
	 * @param index
	 *            The index of the row to obtain
	 * @return The respective column
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public T[] getColumn(int index) throws IllegalAccessException {
		validateTableAccess();

		if (index >= getColumnCount()) {
			return (T[]) Array.newInstance(dataType, getRowCount());
		}

		T[] column = (T[]) Array.newInstance(dataType, getRowCount());

		for (int i = 0; i < getRowCount(); i++) {
			if (isTransposed()) {
				column[i] = dataTable[index][i];
			} else {
				column[i] = dataTable[i][index];
			}
		}

		return column;
	}

	/**
	 * Gets an iterator for iterating over the rows of this table
	 * 
	 * @throws IllegalAccessException
	 */
	public Iterator<T[]> getRowIterator() throws IllegalAccessException {
		validateTableAccess();

		return new Iterator<T[]>() {
			int currentRow;

			@Override
			public boolean hasNext() {
				try {
					return currentRow < getRowCount();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				return false;
			}

			@Override
			public T[] next() {
				currentRow++;

				try {
					return getRow(currentRow - 1);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				return null;
			}
		};
	}

	/**
	 * Gets an iterator for iterating over the columns of this table
	 * 
	 * @throws IllegalAccessException
	 */
	public Iterator<T[]> getColumnIterator() throws IllegalAccessException {
		validateTableAccess();

		return new Iterator<T[]>() {
			int currentColumn;

			@Override
			public boolean hasNext() {
				try {
					return currentColumn < getColumnCount();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				return false;
			}

			@Override
			public T[] next() {
				currentColumn++;

				try {
					return getColumn(currentColumn - 1);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				return null;
			}
		};
	}

	/**
	 * Gets a list of all elements contained in this table. The order of these
	 * elements may not represent their order in the actual table
	 * 
	 * @throws IllegalAccessException
	 */
	public List<T> getElements() throws IllegalAccessException {
		validateTableAccess();

		List<T> elements = new ArrayList<T>();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; i++) {
				elements.add(dataTable[i][j]);
			}
		}

		return elements;
	}

	/**
	 * Checks whether the cell at the specified coordinates is empty
	 * (<code>null</code>
	 * 
	 * @param row
	 *            The cell's row
	 * @param column
	 *            The cell's column
	 * @return Whether or not the cell is empty
	 */
	public boolean isEmpty(int row, int column) {
		if (isValid()) {
			return true;
		}

		try {
			return get(row, column) == null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Checks whether this table is valid
	 */
	public boolean isValid() {
		return dataTable == null || rows == 0 && columns == 0;
	}

	/**
	 * Checks whether this table is empty
	 * 
	 * @throws IllegalAccessException
	 */
	public boolean isEmpty() throws IllegalAccessException {
		validateTableAccess();

		Iterator<T[]> it = (isTransposed() ? getColumnIterator() : getRowIterator());

		while (it.hasNext()) {
			T[] current = it.next();

			for (T currentElement : current) {
				if (currentElement != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Gets the datatype this table consists of
	 */
	public Class<T> getDataType() {
		return dataType;
	}

	/**
	 * Copies the specified sub-table
	 * 
	 * @param rowStart
	 *            The index of the first row that should be copied
	 * @param rowEnd
	 *            The index of the last row that should be copied
	 * @param columnStart
	 *            The index of the first column to copy
	 * @param columnEnd
	 *            The index of the last column to copy
	 * @return The respective sub-table
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public Table<T> copy(int rowStart, int rowEnd, int columnStart, int columnEnd) throws IllegalAccessException {
		validateTableAccess();

		if (rowEnd < rowStart || columnEnd < columnStart) {
			return new Table<T>(dataType);
		}
		if (rowStart < 0 || rowEnd < 0 || columnStart < 0 || columnEnd < 0) {
			throw new IllegalArgumentException("Only positive indices allowed!");
		}

		T[][] copyData = (T[][]) Array.newInstance(dataType, rowEnd - rowStart + 1, 0);

		for (int i = rowStart; i <= rowEnd; i++) {
			if (columnStart < getColumnCount()) {
				copyData[i - rowStart] = Arrays.copyOfRange(getRow(i), columnStart, columnEnd + 1);
			} else {
				copyData[i - rowStart] = (T[]) Array.newInstance(dataType, columnEnd - columnStart + 1);
			}
		}

		return new Table<>(dataType, copyData);
	}

	/**
	 * Creates a copy of this table
	 * 
	 * @throws IllegalAccessException
	 */
	public Table<T> copy() throws IllegalAccessException {
		validateTableAccess();

		return copy(0, getRowCount() - 1, 0, getColumnCount() - 1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (isValid()) {
			return "EmptyTable";
		}

		Iterator<T[]> it = null;
		try {
			it = getRowIterator();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		while (it.hasNext()) {
			builder.append(Arrays.deepToString(it.next()));

			if (it.hasNext()) {
				builder.append("\n");
			}
		}

		return builder.toString();
	}

	/**
	 * Creates a CSV out of this table.
	 * 
	 * @param separator
	 *            The separator-character to be used
	 * @param replacement
	 *            The replacement for the separator in the cell-content
	 * @param emptyReplacement
	 *            The replacement for empty cells
	 * @throws IllegalAccessException
	 */
	public String toCSV(String separator, String replacement, String emptyReplacement) throws IllegalAccessException {
		validateTableAccess();

		StringBuilder builder = new StringBuilder();

		Iterator<T[]> it = getRowIterator();

		while (it.hasNext()) {
			T[] row = it.next();

			for (T currentContent : row) {
				builder.append((currentContent == null ? emptyReplacement : currentContent.toString().replace(separator, replacement).replace("\n", " "))
						+ separator);
			}
			if (row.length > 0) {
				// remove last tab
				builder.deleteCharAt(builder.length() - separator.length());
			}

			builder.append("\n");
		}

		return builder.toString();
	}

	/**
	 * Creates a CSV out of this table. This method will use an empty string to
	 * represent empty cells.
	 * 
	 * @param separator
	 *            The separator-character to be used
	 * @param replacement
	 *            The replacement for the separator in the cell-content
	 * @throws IllegalAccessException
	 */
	public String toCSV(String separator, String replacement) throws IllegalAccessException {
		return toCSV(separator, replacement, "");
	}

	/**
	 * Creates a CSV out of this table. The column separator will be a tab ("\t")
	 * and the row delimiter will be a simple newline ("\n"). Empty cells will be
	 * represented by an empty String
	 * 
	 * @throws IllegalAccessException
	 */
	public String toCSV() throws IllegalAccessException {
		return toCSV("\t", "    ", "");
	}

	/**
	 * Deletes the content of the specified cell by setting it to <code>null</code>
	 * 
	 * @param row
	 *            The cell's row index
	 * @param column
	 *            The cell's column index
	 * @throws IllegalAccessException
	 */
	public void delete(int row, int column) throws IllegalAccessException {
		set(row, column, null);
	}

	/**
	 * Sets the given cell's content
	 * 
	 * @param row
	 *            The cell's row index
	 * @param column
	 *            The cell's column index
	 * @param data
	 *            The data to write into this particular cell
	 * @throws IllegalAccessException
	 */
	public void set(int row, int column, T data) throws IllegalAccessException {
		validateTableAccess();

		if (row < getRowCount() && column < getColumnCount()) {
			if (isTransposed()) {
				dataTable[column][row] = data;
			} else {
				dataTable[row][column] = data;
			}
		}
	}

	/**
	 * Deletes the specified column
	 * 
	 * @param index
	 *            The index of the column to delete
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void deleteColumn(int index) throws IllegalAccessException {
		validateTableAccess();

		if (index < 0 || index >= getColumnCount()) {
			return;
		}

		int firstStart = 0;
		int firstEnd = index - 1;
		int secondStart = index + 1;
		int secondEnd = getColumnCount() - 1;

		Table<T> firstPart = copy(0, getRowCount() - 1, firstStart, firstEnd);
		Table<T> secondPart = copy(0, getRowCount() - 1, secondStart, secondEnd);

		if (firstPart.isValid()) {
			setData(secondPart.getData());
			return;
		}

		if (secondPart.isValid()) {
			setData(firstPart.getData());
			return;
		}

		// merge data
		T[][] firstData = firstPart.getData();
		T[][] secondData = secondPart.getData();

		T[][] mergeData = (T[][]) Array.newInstance(getDataType(), getRowCount(), 0);

		for (int i = 0; i < getRowCount(); i++) {
			int currentCol = 0;

			mergeData[i] = (T[]) Array.newInstance(getDataType(), getColumnCount() - 1);

			for (T currentData : firstData[i]) {
				mergeData[i][currentCol] = currentData;

				currentCol++;
			}
			for (T currentData : secondData[i]) {
				mergeData[i][currentCol] = currentData;

				currentCol++;
			}
		}

		setData(mergeData);
	}

	/**
	 * Deletes the specified row
	 * 
	 * @param index
	 *            The index of the row to delete
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void deleteRow(int index) throws IllegalAccessException {
		validateTableAccess();

		if (index < 0 || index >= getRowCount()) {
			return;
		}

		int firstStart = 0;
		int firstEnd = index - 1;
		int secondStart = index + 1;
		int secondEnd = getRowCount() - 1;

		Table<T> firstPart = copy(firstStart, firstEnd, 0, getColumnCount() - 1);
		Table<T> secondPart = copy(secondStart, secondEnd, 0, getColumnCount() - 1);

		if (firstPart.isValid()) {
			setData(secondPart.getData());
			return;
		}

		if (secondPart.isValid()) {
			setData(firstPart.getData());
			return;
		}

		// merge data
		T[][] firstData = firstPart.getData();
		T[][] secondData = secondPart.getData();

		T[][] mergeData = (T[][]) Array.newInstance(getDataType(), getRowCount() - 1, 0);

		int currentRow = 0;

		for (T[] currentData : firstData) {
			mergeData[currentRow] = currentData;

			currentRow++;
		}
		for (T[] currentData : secondData) {
			mergeData[currentRow] = currentData;

			currentRow++;
		}

		setData(mergeData);
	}

	/**
	 * Validates the access of this table in the current state
	 * 
	 * @throws IllegalAccessException
	 */
	protected void validateTableAccess() throws IllegalAccessException {
		if (isValid()) {
			throw new IllegalAccessException("Can't access empty table!");
		}
	}

	/**
	 * Removes empty rows and columns from this table
	 */
	public void trim() {
		try {
			validateTableAccess();

			List<Integer> removeIndices = new ArrayList<Integer>();

			outerLoop: for (int i = 0; i < getRowCount(); i++) {
				T[] currentRow = getRow(i);

				for (T currentCell : currentRow) {
					if (currentCell != null) {
						continue outerLoop;
					}
				}

				removeIndices.add(i);
			}

			// delete the respective rows
			for (int i = 0; i < removeIndices.size(); i++) {
				deleteRow(removeIndices.get(i) - i);
			}

			removeIndices.clear();

			outerLoop: for (int i = 0; i < getColumnCount(); i++) {
				T[] currentColumn = getColumn(i);

				for (T currentCell : currentColumn) {
					if (currentCell != null) {
						continue outerLoop;
					}
				}

				removeIndices.add(i);
			}

			// delete the respective rows
			for (int i = 0; i < removeIndices.size(); i++) {
				deleteColumn(removeIndices.get(i) - i);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IllegalAccessException {
		String[][] data = new String[][] { new String[] { "firstRow1", "firstRow2", "firstRow3", "firstRow4" },
				new String[] { "secondRow1", "secondRow2", "secondRow3", "secondRow4" },
				new String[] { "thirdRow1", "thirdRow2", "thirdRow3", "thirdRow4" } };

		Table<String> originalTable = new Table<String>(String.class, data);

		System.out.println(originalTable.toCSV());
	}
}
