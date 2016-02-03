package r01f.services.client.servicesproxy.rest;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.ModelObject;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForModelObjectPersistence;
import r01f.usercontext.UserContext;

@Accessors(prefix="_")
public abstract class RESTServicesForModelObjectProxyBase<O extends OID,M extends ModelObject> 
              extends RESTServicesProxyBase {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object type
	 */
	@Getter protected final Class<M> _modelObjectType;
	/**
	 * MetaData about the model object
	 */
	@Getter protected final ModelObjectTypeMetaData _modelObjectMetaData;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O>> 
		   RESTServicesForModelObjectProxyBase(final Marshaller marshaller,
											   final Class<M> modelObjectType,
											   final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  servicesRESTResourceUrlPathBuilder);
		_modelObjectType = modelObjectType;
		_modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjectType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link UserContext} as XML
	 * @param userContext
	 * @return
	 */
	protected String _userContextXml(final UserContext userContext) {
		return _marshaller.xmlFromBean(userContext);
	}
}
