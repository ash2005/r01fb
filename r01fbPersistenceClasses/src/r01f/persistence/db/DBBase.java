package r01f.persistence.db;


import javax.persistence.EntityManager;

import com.google.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.marshalling.HasModelObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.xmlproperties.XMLPropertiesForAppComponent;


/**
 * Base type for every persistence layer type
 */
@Accessors(prefix="_")
public abstract class DBBase
	       implements HasEntityManager,
	       			  HasModelObjectsMarshaller {
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
	/**
	 * Marshaller
	 */
	@Getter protected final Marshaller _modelObjectsMarshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBase(final EntityManager entityManager,
				  final Marshaller marshaller,
				  final XMLPropertiesForAppComponent persistenceProps) {
		_entityManager = entityManager;
		_modelObjectsMarshaller = marshaller;
		_persistenceProperties = persistenceProps;
	}
}
