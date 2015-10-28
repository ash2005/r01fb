package r01f.persistence.db;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.usercontext.UserContext;

/**
 * Transform a {@link DBEntity} to a {@link PersistableModelObject} and vice-versa
 * @see DBBaseForModelObject
 */
public interface DBEntityTransformsToAndFromModelObject {
	/**
	 * Builds a {@link PersistableModelObject} from this {@link DBEntity} data
	 * @param userContext
	 * @return a model object
	 */
	public <M extends PersistableModelObject<? extends OID>> M toModelObject(final UserContext userContext);
	
	/**
	 * Fills up the {@link DBEntity} object's data from the model object
	 * @param userContext
	 * @param modelObj the model object
	 */
	public abstract <M extends PersistableModelObject<? extends OID>> void fromModelObject(final UserContext userContext,
										 												   final M modelObj);
}
