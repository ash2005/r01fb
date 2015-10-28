package r01f.model.facets;

import r01f.guids.OID;
import r01f.model.IndexableModelObject;

/**
 * Every {@link IndexableModelObject} object which has an OID should implement this interface
 * @param <O> the oid type
 */
public interface HasIndexedOID<O extends OID>
	     extends ModelObjectFacet {
	
	/**
	 * gets the oid
	 * @return the oid
	 */
	public O getIndexedOid();
}
