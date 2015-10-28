package r01f.persistence.db.entities;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityTransformsToAndFromModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.usercontext.UserContext;

/**
 * Marker interface for JPA Entity
 * @param <R> the {@link PersistableModelObject} that is represented by this entity
 */
public interface DBEntityForModelObject<PK extends DBPrimaryKeyForModelObject> 
	     extends DBEntity,
	     		 DBEntityTransformsToAndFromModelObject {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the oid
	 */
	public String getOid();
	/**
	 * @param oid the oid
	 */
	public void setOid(String oid);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the primary key for the dbEntity
	 */
	public PK getDBEntityPrimaryKey();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public abstract <M extends PersistableModelObject<? extends OID>> void fromModelObject(final UserContext userContext,
										 												   final M modelObj);
	@Override
	public abstract <M extends PersistableModelObject<? extends OID>> M toModelObject(final UserContext userContext);
}
