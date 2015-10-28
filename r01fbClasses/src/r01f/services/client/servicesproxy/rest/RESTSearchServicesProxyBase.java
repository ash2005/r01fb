package r01f.services.client.servicesproxy.rest;

import java.util.Collection;

import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.IndexableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilderForModelObject;
import r01f.services.interfaces.SearchServices;
import r01f.types.Path;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

public abstract class RESTSearchServicesProxyBase<O extends OID,M extends IndexableModelObject<O>,
												  F extends SearchFilter,I extends SearchResultItem> 
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements SearchServices<F,I> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	private final DelegateForRawRESTSearch<F,I> _rawSearchDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends ServicesRESTResourcePathBuilderForModelObject<O>>
		   RESTSearchServicesProxyBase(final Marshaller marshaller,
									   final Class<M> modelObjectType,
									   final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_rawSearchDelegate = new DelegateForRawRESTSearch<F,I>(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int countRecords(final UserContext userContext,
							final F filter) {
		return 0; 		// TODO terminar!!!
	}
	@Override
	public <U extends OID> Collection<U> filterRecordsOids(final UserContext userContext,
													       final F filter) {
		return null;	// TODO terminar!!!
	}
	@Override
	public SearchResults<F,I> filterRecords(final UserContext userContext,
										    final F filter, 
										    final int firstRowNum,final int numberOfRows) {
		SerializedURL restResourceUrl = this.composeSearchURIFor(Path.of("index")
																	 .add(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
																			  .pathOfResource()));
		return _rawSearchDelegate.doSEARCH(restResourceUrl,
										   userContext,	
									       filter,
										   firstRowNum,numberOfRows);
	}
}
