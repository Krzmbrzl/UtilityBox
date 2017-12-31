package raven.utilityBox.interfaces;

public interface ITypeConverter<T, E> {

	/**
	 * Converts the input into the target data type.
	 * 
	 * @param input
	 *            The input to convert
	 * @return The respective target data type representing the input. If no
	 *         conversion could be performed <code>null</code> is being returned
	 */
	public E convert(T input);
}
