package r01f.persistence.db;

import com.google.common.base.Function;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.patterns.IsBuilder;
import r01f.usercontext.UserContext;


/**
 * Transformer functions between {@link DBEntity} and {@link PersistableModelObject}
 */
@Slf4j
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
	public static <DB extends DBEntity,
				   M extends PersistableModelObject<? extends OID>> Function<DB,M> createFor(final UserContext userContext,
						  				   						 							 final TransformsDBEntityIntoModelObject<DB,M> dbEntityToModelObjectTransformer) {
		return DBEntityToModelObjectTransformerBuilder.createFor(userContext,
																 new Function<DB,M>() {
																		@Override
																		public M apply(final DB dbEntity) {
																			try {
																				// transform the dbentity to a model object
																				return dbEntityToModelObjectTransformer.dbEntityToModelObject(userContext,
																																			  dbEntity);
																			} catch(Exception ex) {																		
																				log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex.getMessage(),ex);																		
																		    } catch(Throwable ex) {
																		    	log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex.getMessage(),ex);																		
																		    }																	
																		    return null;//Return null if any exception happens, so must be applied a no null filtering!
																		}
																 });
	}
	/**
	 * Creates a new transformer from a {@link DBEntity} to a {@link PersistableModelObject}
	 * @param userContext
	 * @param transformer another transformer
	 * @return
	 */
	public static <DB extends DBEntity,
				   M extends PersistableModelObject<? extends OID>> Function<DB,M> createFor(final UserContext userContext,
						  				   						 							 final Function<DB,M> transformer) {
		return new Function<DB,M>() {
						@Override
						public M apply(final DB dbEntity) {
							try {
								// Transform to model object	
								M outModelObj = transformer.apply(dbEntity);	
								// ensure that the model object has the tacking info and entity version
								if (outModelObj != null) {
									DBEntityToModelObjectTransformerBuilder.copyDBEntiyTrackingInfoAndEntityVersionToModelObject(dbEntity,
																																 outModelObj);
								}
								return outModelObj;							
							}catch(Exception ex){																			
								log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex.getMessage(),ex);																																	
						    }catch(Throwable ex){
						    	log.error("DBEntityToModelObjectTransformerBuilder error :{}",ex.getMessage(),ex);																		
						    }																	
						    return null;//Return null if any exception happens, so must be applied a not null filtering!
							
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
		
		if (dbEntity.getCreateTimeStamp() != null) {
			trackingInfo.setCreateDate(dbEntity.getCreateTimeStamp());
		} else if (modelObject.getTrackingInfo() != null 
				&& modelObject.getTrackingInfo().getCreateDate() != null) {
			trackingInfo.setCreateDate(modelObject.getTrackingInfo().getCreateDate());
		}
		
		if (dbEntity.getLastUpdateTimeStamp() != null) {
			trackingInfo.setLastUpdateDate(dbEntity.getLastUpdateTimeStamp());
		} else if (modelObject.getTrackingInfo() != null 
				&& modelObject.getTrackingInfo().getLastUpdateDate() != null) {
			trackingInfo.setLastUpdateDate(modelObject.getTrackingInfo().getLastUpdateDate());
		}
		
		if (dbEntity.getCreatorUserCode() != null) {
			trackingInfo.setCreatorUserCode(dbEntity.getCreatorUserCode());
		} else if (modelObject.getTrackingInfo() != null 
				&& modelObject.getTrackingInfo().getCreatorUserCode() != null) {
			trackingInfo.setCreatorUserCode(modelObject.getTrackingInfo().getCreatorUserCode());
		}
		
		if (dbEntity.getLastUpdatorUserCode() != null) {
			trackingInfo.setLastUpdatorUserCode(dbEntity.getLastUpdatorUserCode());
		} else if (modelObject.getTrackingInfo() != null 
				&& modelObject.getTrackingInfo().getLastUpdatorUserCode() != null) {
			trackingInfo.setLastUpdatorUserCode(modelObject.getTrackingInfo().getLastUpdatorUserCode());
		}
		
		modelObject.setTrackingInfo(trackingInfo);
		
		modelObject.setEntityVersion(dbEntity.getEntityVersion());
	}
}
