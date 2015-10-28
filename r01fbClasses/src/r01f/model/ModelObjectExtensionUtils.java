package r01f.model;

import r01f.exceptions.Throwables;

public class ModelObjectExtensionUtils {
	/**
	 * Checks if an extensible model object is extended
	 * @param obj
	 */
	public static void checkExtension(final Object obj) {
		if (obj instanceof ExtensibleModelObject) {
			ExtensibleModelObject<?> extensible = (ExtensibleModelObject<?>)obj;
			if (extensible.getExtension() == null) throw new IllegalStateException(Throwables.message("The extensible model object {} is NOT extended: check that weaving is being taking place!",
																					  				  obj.getClass()));
		} else {
			throw new IllegalStateException(Throwables.message("The object {} is NOT an instance of {}",obj.getClass(),ExtensibleModelObject.class));
		}
	}
}
