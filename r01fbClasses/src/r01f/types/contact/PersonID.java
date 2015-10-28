package r01f.types.contact;

import r01f.guids.OIDTyped;

/**
 * Models a person identity card numer (spanish dni or social security number)
 */
public interface PersonID 
		 extends OIDTyped<String> {
	/**
	 * @return true if the id is valid
	 */
	public boolean isValid();
}
