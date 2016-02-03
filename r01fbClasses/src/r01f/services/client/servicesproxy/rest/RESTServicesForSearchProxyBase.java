package r01f.services.client.servicesproxy.rest;

import java.util.Collection;

import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForModelObjectPersistence;
import r01f.services.interfaces.SearchServices;
import r01f.types.UrlPath;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;

public abstract class RESTServicesForSearchProxyBase<F extends SearchFilter,I extends SearchResultItem> 
              extends RESTServicesProxyBase
           implements SearchServices<F,I> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	private final DelegateForRawRESTSearch<F,I> _rawSearchDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<? extends OID>>
		   RESTServicesForSearchProxyBase(final Marshaller marshaller,
									   	  final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  servicesRESTResourceUrlPathBuilder);
		_rawSearchDelegate = new DelegateForRawRESTSearch<F,I>(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int countRecords(final UserContext userContext,
							final F filter) {
		throw new UnsupportedOperationException("NOT yet implemented!!");	// TODO implement REST countRecords proxy
	}
	@Override
	public <U extends OID> Collection<U> filterRecordsOids(final UserContext userContext,
													       final F filter) {
		throw new UnsupportedOperationException("NOT yet implemented!!");	// TODO implement REST filterRecordsOids proxy
	}
	@Override
	public SearchResults<F,I> filterRecords(final UserContext userContext,
										    final F filter, 
										    final int firstRowNum,final int numberOfRows) {
		Url restResourceUrl = this.composeURIFor(UrlPath.of("index"));
		return _rawSearchDelegate.doSEARCH(restResourceUrl,
										   userContext,	
									       filter,
										   firstRowNum,numberOfRows);
	}
}
