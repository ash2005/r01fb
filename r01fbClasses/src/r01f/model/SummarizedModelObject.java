package r01f.model;

import java.io.Serializable;

import r01f.guids.OID;

/**
 * A model object summary used when returning persistence find results
 * @param <M>
 */
public interface SummarizedModelObject<M extends PersistableModelObject<? extends OID>>
		 extends Serializable {
	/**
	 * Return the model object type
	 * @return
	 */
	public Class<M> getModelObjectType();
}
