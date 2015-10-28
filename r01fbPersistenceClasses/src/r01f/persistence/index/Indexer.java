package r01f.persistence.index;

import r01f.guids.OID;
import r01f.guids.VersionOID;
import r01f.model.IndexableModelObject;
import r01f.usercontext.UserContext;

/**
 * Interface to be implemented by types in charge to index persistable records
 * @param <M>
 */
public interface Indexer<M extends IndexableModelObject<? extends OID>> {
	/**
	 * Index a model object
	 * @param userContext
	 * @param record
	 */
	public void index(final UserContext userContext,
					  final M record);
	/**
	 * Updates the indexed data for a model record
	 * @param userContext
	 * @param record
	 */
	public void updateIndex(final UserContext userContext,
							final M record);
	
	/**
	 * Deletes the indexed data for a record
	 * @param userContext
	 * @param record
	 * @throws UnsupportedOperationException if the record is versionable
	 */
	public void removeFromIndex(final UserContext userContext,
								final OID oid);
	/**
	 * Deleted the indexed data for a versionable record
	 * @param userContext
	 * @param oid
	 * @param version
	 * @throws UnsupportedOperationException if the record is NOT versionable
	 */
	public void removeFromIndex(final UserContext userContext,
								final OID oid,VersionOID version);
}
