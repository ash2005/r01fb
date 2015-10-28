package r01f.persistence.db;

import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.IsBuilder;
import r01f.usercontext.UserContext;

import com.google.common.base.Function;


/**
 * Transformer functions between {@link DBEntity} and {@link PersistableModelObject}
 */
public class DBEntityToModelObjectTransformerBuilder
  implements IsBuilder {
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param dbEntityType
	 * @param modelObjType
	 * @return
	 */
	public static <DB extends DBEntity,M extends PersistableModelObject<? extends OID>> 
				  Function<DB,M> createFor(final UserContext userContext,
						  				   final Class<DB> dbEntityType,final Class<M> modelObjType) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB db) {
							if (db instanceof DBEntityTransformsToAndFromModelObject) {
								return ((DBEntityTransformsToAndFromModelObject)db).<M>toModelObject(userContext);
							}
							throw new IllegalArgumentException(Throwables.message("{} is not a {} instance",
																				   db.getClass(),DBEntityTransformsToAndFromModelObject.class));
						}
				   };
	}
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param modelObjType
	 * @return
	 */
	public static <DB extends DBEntity,M extends PersistableModelObject<? extends OID>> 
				  Function<DB,M> createFor(final UserContext userContext,
						  				   final Class<M> modelObjType) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB dbEntity) {
							if (dbEntity instanceof DBEntityTransformsToAndFromModelObject) {
								// transform the dbentity to a model object
								M outModelObj = ((DBEntityTransformsToAndFromModelObject)dbEntity).<M>toModelObject(userContext);
								
								// ensure that the model object has the tacking info and entity version
								DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																															 outModelObj);
								return outModelObj;
							}
							throw new IllegalArgumentException(Throwables.message("{} is not a {} instance",
																				   dbEntity.getClass(),DBEntityTransformsToAndFromModelObject.class));
						}
				   };
	}
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param transformer another transformer
	 * @return
	 */
	public static <DB extends DBEntity,M extends PersistableModelObject<? extends OID>> 
				  Function<DB,M> createFor(final UserContext userContext,
						  				   final Function<DB,M> transformer) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB dbEntity) {
							// Transform to model object
							M outModelObj = transformer.apply(dbEntity);

							// ensure that the model object has the tacking info and entity version 
							DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																														 outModelObj);
							return outModelObj;
						}
				   };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static <M extends PersistableModelObject<? extends OID>> void copyDBEntiyTrackingInfoAndEntityVersionToModelObject(final DBEntity dbEntity,
							  										   														   final M modelObject) {
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
