package r01f.services.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.marshalling.HasModelObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.persistence.db.HasEntityManagerProvider;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


@Accessors(prefix="_")
public abstract class CorePersistenceServiceBase
 	   		  extends CoreServiceBase 
 	   	   implements HasEntityManagerProvider,
 	   	   			  HasModelObjectsMarshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link EntityManager} provider
	 */
	@Inject
	@Getter protected Provider<EntityManager> _entityManagerProvider;	
	/**
	 * Marshaller used to serialize / de-serialize java objects
	 */
	@Inject @ModelObjectsMarshaller
	@Getter protected Marshaller _modelObjectsMarshaller;
	/**
	 * The {@link XMLProperties} for the db layer
	 * (this properties are set at BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase type)
	 */ 
	@Inject @XMLPropertiesComponent("dbpersistence")
	@Getter protected XMLPropertiesForAppComponent _persistenceProperties;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EntityManager getFreshNewEntityManager() {
		EntityManager outEntityManager = _entityManagerProvider.get();
		
		// TODO needs some research... really must have to call clear?? (see http://stackoverflow.com/questions/9146239/auto-cleared-sessions-with-guice-persist)
		outEntityManager.clear();	// BEWARE that the EntityManagerProvider reuses EntityManager instances and those instances
									// could have cached entity instances... discard them all
		outEntityManager.setFlushMode(FlushModeType.COMMIT);
		return outEntityManager;
	}
}
