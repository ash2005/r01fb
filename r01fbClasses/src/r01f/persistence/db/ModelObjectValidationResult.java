package r01f.persistence.db;

import r01f.model.ModelObject;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultNOK;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultOK;

public interface ModelObjectValidationResult<M extends ModelObject> {
	/**
	 * @return the validated model object
	 */
	public M getModelObject();
	/**
	 * @return true if the model object is valid
	 */
	public boolean isValid();
	/**
	 * @return true if the model object is NOT valid
	 */
	public boolean isNOTValid();
	/**
	 * @return a {@link ModelObjectValidationResultOK} if the model object is valid or a {@link ClassCastException} if the model object is NOT valid
	 */
	public ModelObjectValidationResultOK<M> asOKValidationResult();
	/**
	 * @return a {@link ModelObjectValidationResultNOK} if the model object is NOT valid or a {@link ClassCastException} if the model object is valid
	 */
	public ModelObjectValidationResultNOK<M> asNOKValidationResult();
}