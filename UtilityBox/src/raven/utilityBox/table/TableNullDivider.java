package raven.utilityBox.table;

public class TableNullDivider<T> extends TableDivider<T> {

	public TableNullDivider(Table<T> table) {
		super(table);
	}

	@Override
	protected boolean isEmpty(Object content) {
		return content == null;
	}

}
