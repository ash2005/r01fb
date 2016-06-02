package r01f.persistence.db;

import r01f.guids.OID;
import r01f.model.ModelObject;
import r01f.model.PersistableModelObject;
import r01f.usercontext.UserContext;

/**
 * Interface for types that transforms a {@link DBEntity} into a {@link ModelObject}
 * (used when loading a {@link ModelObject} from a {@link DBEntity})
 * @param <DB>
 * @param <M>
 */
public interface TransformsDBEntityIntoModelObject<DB extends DBEntity,
												   M extends PersistableModelObject<? extends OID>> {
	/**
	 * Builds a {@link ModelObject} from this {@link DBEntity} data
	 * @param userContext
	 * @param dbEntity
	 * @return a model object
	 */
	public abstract M dbEntityToModelObject(final UserContext userContext,
										    final DB dbEntity);
}
