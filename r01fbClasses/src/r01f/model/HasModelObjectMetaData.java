package r01f.model;

import r01f.model.metadata.ModelObjectTypeMetaData;

import com.google.common.annotations.GwtIncompatible;

/**
 * Interface for {@link ModelObject}s described by {@link ModelObjectTypeMetaData}
 */
public interface HasModelObjectMetaData {
	/**
	 * @return the {@link ModelObjectTypeMetaData} associated with this model object
	 */
	@GwtIncompatible("GWT does NOT suppports ModelObjectMetaData Building")
	public ModelObjectTypeMetaData getModelObjectMetaData();
}
