package r01f.services.client.servicesproxy.rest;

import lombok.Getter;
import r01f.marshalling.Marshaller;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.index.IndexManagementCommand;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilder;
import r01f.services.interfaces.IndexManagementServices;
import r01f.types.Path;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

public abstract class RESTIndexManagementServicesProxyBase
     		  extends RESTServicesProxyBase
     	   implements IndexManagementServices {
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////	
    @Getter private DelegateForRawRESTIndexManagement _rawIndexManagementDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public RESTIndexManagementServicesProxyBase(final Marshaller marshaller,
												final ServicesRESTResourcePathBuilder servicesRESTResourceUrlPathBuilder) {
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
	protected SerializedURL composeSearchIndexURIFor(final Path path) {
		ServicesRESTResourcePathBuilder pathBuilder = this.getServicesRESTResourceUrlPathBuilder();
		Path uri = Path.of(pathBuilder.getHost())
					   .add(pathBuilder.getSearchIndexEndPointBasePath())
					   .add(path);
		return SerializedURL.create(uri.asString());
	}
	/**
	 * @return the index Path
	 */
	protected abstract Path getIndexPath();
/////////////////////////////////////////////////////////////////////////////////////////
//  IndexManagementServices
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob openIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(this.getIndexPath().add("status")),
															 		userContext,
															 		IndexManagementCommand.toOpenIndex());
	}
	@Override
	public EnqueuedJob closeIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(this.getIndexPath().add("status")),
															 		userContext,
															 		IndexManagementCommand.toCloseIndex());
	}
	@Override
	public EnqueuedJob optimizeIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(this.getIndexPath().add("status")),
															 		userContext,
															 		IndexManagementCommand.toOptimizeIndex());
	}
	@Override
	public EnqueuedJob truncateIndex(UserContext userContext) {
		return _rawIndexManagementDelegate.doIndexManagementCommand(this.composeSearchIndexURIFor(this.getIndexPath().add("status")),
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
