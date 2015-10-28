package r01f.rest.resources.delegates;

import lombok.experimental.Accessors;
import r01f.model.HasModelObjectMetaData;
import r01f.model.ModelObject;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;

/**
 * Base type for REST services 
 */
@Accessors(prefix="_")
public abstract class RESTDelegateForModelObjectBase<M extends ModelObject & HasModelObjectMetaData> 
	       implements RESTDelegate { 
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<M> _modelObjectType;
	protected final ModelObjectTypeMetaData _modelObjectMetaData;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTDelegateForModelObjectBase(final Class<M> modelObjectType) {
		_modelObjectType = modelObjectType;
		_modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjectType);
	}
}
