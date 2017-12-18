package raven.utilityBox.util;

/**
 * A class containing methods for working with arrays
 * 
 * @author Raven
 *
 */
public class ArrayUtil {
	
	/**
	 * Returns a clone of the given array
	 * 
	 * @param source
	 *            The array to copy
	 * @return The copy of the given source array
	 */
	public static Object[] cloneArray(Object[] source) {
		Object[] copy;
		
		synchronized (source) {
			copy = new Object[source.length];
			
			for (int i = 0; i < source.length; i++) {
				copy[i] = source[i];
			}
		}
		
		return copy;
		
	}
	
}
