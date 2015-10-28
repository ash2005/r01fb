package r01f.services.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.persistence.db.HasEntityManagerProvider;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


@Accessors(prefix="_")
public abstract class CorePersistenceServiceBase
 	   		  extends CoreServiceBase 
 	   	   implements HasEntityManagerProvider {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link EntityManager} provider
	 */
	@Inject
	@Getter protected Provider<EntityManager> _entityManagerProvider;	
	
	@Override
	public EntityManager getFreshNewEntityManager() {
		EntityManager outEntityManager = _entityManagerProvider.get();
		
		// TODO needs some research... really must have to call clear?? (see http://stackoverflow.com/questions/9146239/auto-cleared-sessions-with-guice-persist)
		outEntityManager.clear();	// BEWARE that the EntityManagerProvider reuses EntityManager instances and those instances
									// could have cached entity instances... discard them all
		outEntityManager.setFlushMode(FlushModeType.COMMIT);
		return outEntityManager;
	}
	/**
	 * The {@link XMLProperties} for the db layer
	 */
	@Inject  @XMLPropertiesComponent("persistence") 
	@Getter protected XMLPropertiesForAppComponent _persistenceProperties;
	
}
