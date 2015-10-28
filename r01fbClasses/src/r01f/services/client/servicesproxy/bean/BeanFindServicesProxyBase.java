package r01f.services.client.servicesproxy.bean;


import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindOIDsResult;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.types.Range;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class BeanFindServicesProxyBase<O extends OID,M extends PersistableModelObject<O>>
    	   implements FindServicesForModelObject<O,M>,
    	   			  ProxyForBeanImplementedService {

/////////////////////////////////////////////////////////////////////////////////////////
// 	DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final FindServicesForModelObject<O,M> _findServices;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILITY METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends FindServicesForModelObject<O,M>> T getFindServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_findServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsResult<O> findAll(final UserContext userContext) {
		return _findServices.findAll(userContext);
	}
	@Override
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
											  final Range<Date> createDate) {
		return _findServices.findByCreateDate(userContext,
										      createDate);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
												  final Range<Date> lastUpdateDate) {
		return _findServices.findByLastUpdateDate(userContext,
												  lastUpdateDate);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
										   final UserCode creatorUserCode) {
		return _findServices.findByCreator(userContext,
										   creatorUserCode);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
											   final UserCode lastUpdtorUserCode) {
		return _findServices.findByLastUpdator(userContext,
											   lastUpdtorUserCode);
	}
	
}
