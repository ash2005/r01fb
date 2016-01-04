package r01f.model;

import r01f.guids.OID;


/**
 * Interface for types that store the model object's type
 * @param <M>
 */
public interface HasModelObjectTypeInfo<M extends PersistableModelObject<? extends OID>> {
	/**
	 * @return the model object's type 
	 */
	public Class<M> getModelObjectType();
	/**
	 * Sets the model object's type
	 * @param type
	 */
	public void setModelObjectType(final Class<M> type);
}
