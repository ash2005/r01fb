package r01f.persistence.db;

import com.google.common.base.Function;

import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.IsBuilder;
import r01f.usercontext.UserContext;


/**
 * Transformer functions between {@link DBEntity} and {@link PersistableModelObject}
 */
public class DBEntityToModelObjectTransformerBuilder
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param modelObjType
	 * @return
	 */
	public static <DB extends DBEntity,M> Function<DB,M> createFor(final UserContext userContext,
						  				   						   final Class<M> modelObjType) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB dbEntity) {
							// transform the dbentity to a model object
							M outModelObj = dbEntity.<M>toModelObject(userContext);
							
							// ensure that the model object has the tacking info and entity version
							if (outModelObj != null && outModelObj instanceof PersistableModelObject) {
								DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																															 (PersistableModelObject<?>)outModelObj);
							}
							return outModelObj;
						}
				   };
	}
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param transformer another transformer
	 * @return
	 */
	public static <DB extends DBEntity,M> Function<DB,M> createFor(final UserContext userContext,
						  				   						   final Function<DB,M> transformer) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB dbEntity) {
							// Transform to model object
							M outModelObj = transformer.apply(dbEntity);

							// ensure that the model object has the tacking info and entity version
							if (outModelObj != null && outModelObj instanceof PersistableModelObject) {
								DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																															 (PersistableModelObject<?>)outModelObj);
							}
							return outModelObj;
						}
				   };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static void copyDBEntiyTrackingInfoAndEntityVersionToModelObject(final DBEntity dbEntity,
																		     final PersistableModelObject<?> modelObject) {
		// do not forget!
		ModelObjectTracking trackingInfo = new ModelObjectTracking();
		trackingInfo.setCreateDate(dbEntity.getCreateTimeStamp());
		trackingInfo.setLastUpdateDate(dbEntity.getLastUpdateTimeStamp());
		trackingInfo.setCreatorUserCode(dbEntity.getCreatorUserCode());
		trackingInfo.setLastUpdatorUserCode(dbEntity.getLastUpdatorUserCode());
		
		modelObject.setTrackingInfo(trackingInfo);
		
		modelObject.setEntityVersion(dbEntity.getEntityVersion());
	}
}
