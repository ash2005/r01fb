package r01f.persistence.db;

import javax.inject.Provider;
import javax.persistence.EntityManager;


/**
 * Interface for objects that holds an {@link EntityManager}{@link Provider} 
 */
public interface HasEntityManagerProvider {
	/**
	 * @return an entity manager provider
	 */
	public Provider<EntityManager> getEntityManagerProvider();
	
	/**
	 * Uses the {@link EntityManager} {@link Provider} to get a fresh new
	 * {@link EntityManager} instance
	 * @return
	 */
	public EntityManager getFreshNewEntityManager();
}
