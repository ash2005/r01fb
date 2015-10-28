package r01f.services.delegates.persistence;


import java.util.Date;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.FindOIDsResultBuilder;
import r01f.persistence.db.DBFindForModelObject;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;
import r01f.usercontext.UserContext;

import com.google.common.eventbus.EventBus;

/**
 * Service layer delegated type for CRUD find operations
 */
public abstract class FindServicesForModelObjectDelegateBase<O extends OID,M extends PersistableModelObject<O>>
			  extends PersistenceServicesForModelObjectDelegateBase<O,M>
		   implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR  
/////////////////////////////////////////////////////////////////////////////////////////
	public FindServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
												  final DBFindForModelObject<O,M> findServices) {
		this(modelObjectType,
			 findServices,
			 null);
	}
	public FindServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
												  final DBFindForModelObject<O,M> findServices,
												  final EventBus eventBus) {
		super(modelObjectType,
			  findServices,
			  eventBus);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findAll(final UserContext userContext) {
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
												.findAll(userContext);
		return outResults;
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
										  	  final Range<Date> createDate) {
		// [0] - check the date
		if (createDate == null) {
			return FindOIDsResultBuilder.using(userContext)
										.on(_modelObjectType)
										.errorFindingOids()
											.causedByClientBadRequest("The date range MUST NOT be null in order to find entities by create date");
		}
		// [1] - do the find
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
												.findByCreateDate(userContext,
																  createDate);
		return outResults;
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
											  	  final Range<Date> lastUpdateDate) {
		// [0] - check the date
		if (lastUpdateDate == null) {
			return FindOIDsResultBuilder.using(userContext)
										.on(_modelObjectType)
										.errorFindingOids()
											.causedByClientBadRequest("The date range MUST NOT be null in order to find entities by last update date");
		}
		// [1] - do the find
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
												.findByLastUpdateDate(userContext,
																   	   	   lastUpdateDate);
		return outResults;
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
									   	   final UserCode creatorUserCode) {
		// [0] - check the date
		if (creatorUserCode == null) {
			return FindOIDsResultBuilder.using(userContext)
										.on(_modelObjectType)
										.errorFindingOids()
												.causedByClientBadRequest("The user code MUST NOT be null in order to find entities creator");
		}
		// [1] - do the find
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
												.findByCreator(userContext,
															   creatorUserCode);
		return outResults;
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
										       final UserCode lastUpdatorUserCode) {
		// [0] - check the date
		if (lastUpdatorUserCode == null) {
			return FindOIDsResultBuilder.using(userContext)
										.on(_modelObjectType)
										.errorFindingOids()
											.causedByClientBadRequest("The user code MUST NOT be null in order to find entities by last updator user code");
		}
		// [1] - do the find
		FindOIDsResult<O> outResults = this.getServiceImplAs(FindServicesForModelObject.class)
												.findByLastUpdator(userContext,
																   lastUpdatorUserCode);
		return outResults;
	}
}
