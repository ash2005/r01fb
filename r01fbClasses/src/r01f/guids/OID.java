package r01f.guids;

import java.io.Serializable;

import r01f.types.CanBeRepresentedAsString;

/**
 * Models an oid
 */
public interface OID 
         extends Serializable,
     			 Comparable<OID>,
     			 Cloneable,
     			 CanBeRepresentedAsString {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final int OID_LENGTH = 50;
	public static final String STATIC_FACTORY_METHOD_NAME = "forId";
	public static final String STATIC_SUPPLIER_METHOD_NAME = "supply";
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks if this oid is the same as the provided one
	 * (its similar to equals method but more 'semantic')
	 * @param other
	 * @return true if the two oids are the same oid
	 */
	public <O extends OID> boolean is(final O other);
	/**
	 * Checks if this oid is NOT equal to the provided one
	 * @param other
	 * @return
	 */
	public <O extends OID> boolean isNOT(final O other);
    /**
     * Checks if this oid is included in the provided collection
     * @param oids
     * @return true if the oid is included in the collection, false otherwise
     */
	public <O extends OID> boolean isContainedIn(final O... oids);
    /**
     * Checks if this oid is NOT included in the provided collection
     * @param oids
     * @return true if the oid is NOT included in the collection, false otherwise
     */
	public <O extends OID> boolean isNOTContainedIn(final O... oids);
    /**
     * Checks if this oid is included in the provided collection
     * @param oids
     * @return true if the oid is included in the collection, false otherwise
     */
    public <O extends OID> boolean isContainedIn(final Iterable<O> oids);
    /**
     * Checks if this oid NOT is included in the provided collection
     * @param oids
     * @return true if the oid is NOT included in the collection, false otherwise
     */
    public <O extends OID> boolean isNOTContainedIn(final Iterable<O> oids);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the oid typed
	 */
	public <O extends OID> O cast();
}
