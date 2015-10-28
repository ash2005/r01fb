package r01f.persistence.db;

import javax.persistence.EntityManager;

import lombok.experimental.Accessors;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindResult;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForVersionableModelObject;
import r01f.services.interfaces.FindServicesForVersionableModelObject;
import r01f.usercontext.UserContext;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Accessors(prefix="_")
public abstract class DBFindForVersionableModelObjectBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet,
							     						  PK extends DBPrimaryKeyForVersionableModelObject,DB extends DBEntity & DBEntityForModelObject<PK>>
			  extends DBFindForModelObjectBase<O,M,	
			  				 				   PK,DB> 
	       implements FindServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBFindForVersionableModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
											   final EntityManager entityManager,
											   final XMLPropertiesForAppComponent persistenceProps) {
		super(modelObjectType,dbEntityType,
			  entityManager,
			  persistenceProps);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindResult<M> findAllVersions(final UserContext userContext) {
		throw new UnsupportedOperationException("NOT implemented!");
	}
}
