package r01f.util.types;

public class Objects {
	/**
	 * Checks if there's any not null object in the provided collection
	 * @param objects
	 * @return
	 */
	public static boolean isAnyNotNull(final Object... objects) {
		if (objects == null || objects.length == 0) throw new IllegalArgumentException();
		boolean outAnyNotNull = false;
		for (Object obj : objects) {
			if (obj != null) {
				outAnyNotNull = true;
				break;
			}
		}
		return outAnyNotNull;
	}
	/**
	 * Checks if all objects are all null in the provided collection
	 * @param objects
	 * @return
	 */
	public static boolean areAllNull(final Object... objects) {
		return !Objects.isAnyNotNull(objects);
	}
	/**
	 * Checks if all objects are all NOT null in the provided collection
	 * @param objects
	 * @return
	 */
	public static boolean areAllNOTNull(final Object... objects) {
		if (objects == null || objects.length == 0) throw new IllegalArgumentException();
		boolean outAnyNull = false;
		for (Object obj : objects) {
			if (obj == null) {
				outAnyNull = true;
				break;
			}
		}
		return outAnyNull;
	}
}
