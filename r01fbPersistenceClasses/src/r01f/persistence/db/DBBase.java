package r01f.persistence.db;


import javax.persistence.EntityManager;

import com.google.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


/**
 * Base type for every persistence layer type
 */
@Accessors(prefix="_")
public abstract class DBBase
	       implements HasEntityManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The entity manager obtained from the {@link EntityManager} {@link Provider}
	 */
	@Getter protected final EntityManager _entityManager;
	/**
	 * Properties
	 */
	@Getter protected final XMLPropertiesForAppComponent _persistenceProperties;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBase(final EntityManager entityManager,
				  final XMLPropertiesForAppComponent persistenceProps) {
		_entityManager = entityManager;
		_persistenceProperties = persistenceProps;
	}
}
