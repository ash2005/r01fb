package r01f.model.facets;

import r01f.guids.OID;

/**
 * Every model object which has an ID should implement this interface
 * @param <O> the id type
 */
public interface HasID<O extends OID>
	     extends ModelObjectFacet {
	
	/**
	 * gets the id
	 * @return the id
	 */
	public O getId();
	/**
	 * Sets the id
	 * @param id the id
	 */
	public void setId(O id);
	/**
	 * Sets the id with no guarantee that a {@link ClassCastException} is thrown 
	 * if the provided id does not match the expected type
	 * @param id
	 */
	public void unsafeSetId(final OID id);
}
