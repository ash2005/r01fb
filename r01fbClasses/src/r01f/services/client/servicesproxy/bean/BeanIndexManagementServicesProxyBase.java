package r01f.services.client.servicesproxy.bean;


import lombok.RequiredArgsConstructor;
import r01f.model.jobs.EnqueuedJob;
import r01f.services.interfaces.IndexManagementServices;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
@RequiredArgsConstructor
public abstract class BeanIndexManagementServicesProxyBase
           implements IndexManagementServices,
  			   		  ProxyForBeanImplementedService {		// it's a bean implementation of the R01MServiceInterface

/////////////////////////////////////////////////////////////////////////////////////////
// 	DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final IndexManagementServices _indexManagementServices;

/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX MANAGEMENT
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob openIndex(final UserContext userContext) {
		return _indexManagementServices.openIndex(userContext);
	}
	@Override
	public EnqueuedJob closeIndex(final UserContext userContext) {
		return _indexManagementServices.closeIndex(userContext);
	}
	@Override
	public EnqueuedJob optimizeIndex(final UserContext userContext) {
		return _indexManagementServices.optimizeIndex(userContext);
	}
	@Override
	public EnqueuedJob truncateIndex(final UserContext userContext) {
		return _indexManagementServices.truncateIndex(userContext);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void start() {
		this.openIndex(null);
	}
	@Override
	public void stop() {
		this.closeIndex(null);
	}
}
