package r01f.services.client.servicesproxy.rest;

import lombok.Getter;
import r01f.marshalling.Marshaller;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.index.IndexManagementCommand;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilder;
import r01f.services.interfaces.IndexManagementServices;
import r01f.types.Paths;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.usercontext.UserContext;

public abstract class RESTServicesForIndexManagementProxyBase
     		  extends RESTServicesProxyBase
     	   implements IndexManagementServices {
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////	
    @Getter private DelegateForRawRESTIndexManagement _rawIndexManagementDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public RESTServicesForIndexManagementProxyBase(final Marshaller marshaller,
												final RESTServiceResourceUrlPathBuilder servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  servicesRESTResourceUrlPathBuilder);
		_rawIndexManagementDelegate = new DelegateForRawRESTIndexManagement(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PATH BUILDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Composes the complete REST endpoint URI for a path
	 * @param path
	 * @return
	 */
	protected Url composeSearchIndexURIFor(final UrlPath path) {
		RESTServiceResourceUrlPathBuilder pathBuilder = this.getServicesRESTResourceUrlPathBuilder();
		return Url.from(pathBuilder.getHost(),
					    Paths.forUrlPaths().join(pathBuilder.getEndPointBasePath(),
					    						path));
	}
	/**
	 * @return the index Path
	 */
	protected abstract UrlPath getIndexPath();
/////////////////////////////////////////////////////////////////////////////////////////
//  IndexManagementServices
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob openIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(Paths.forUrlPaths().join(this.getIndexPath(),
																														   "status")),
															 		userContext,
															 		IndexManagementCommand.toOpenIndex());
	}
	@Override
	public EnqueuedJob closeIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(Paths.forUrlPaths().join(this.getIndexPath(),
																														   "status")),
															 		userContext,
															 		IndexManagementCommand.toCloseIndex());
	}
	@Override
	public EnqueuedJob optimizeIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(Paths.forUrlPaths().join(this.getIndexPath(),
																														   "status")),
															 		userContext,
															 		IndexManagementCommand.toOptimizeIndex());
	}
	@Override
	public EnqueuedJob truncateIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(Paths.forUrlPaths().join(this.getIndexPath(),
																														   "status")),
															 		userContext,
															 		IndexManagementCommand.toTruncateIndex());
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
