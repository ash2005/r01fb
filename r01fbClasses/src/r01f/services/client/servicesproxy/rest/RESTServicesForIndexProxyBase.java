package r01f.services.client.servicesproxy.rest;

import java.util.Collection;

import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.IndexableModelObject;
import r01f.model.facets.Facetable;
import r01f.model.facets.Facetables;
import r01f.model.facets.HasOID;
import r01f.model.jobs.EnqueuedJob;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForModelObjectPersistence;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.types.Path;
import r01f.types.Paths;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;

public abstract class RESTServicesForIndexProxyBase<O extends OID,M extends IndexableModelObject<O>> 
              extends RESTServicesProxyBase
           implements IndexServicesForModelObject<O,M> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final DelegateForRawRESTIndex _rawRESTIndexDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O>>
		   RESTServicesForIndexProxyBase(final Marshaller marshaller,
									  	 final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  servicesRESTResourceUrlPathBuilder);
		_rawRESTIndexDelegate = new DelegateForRawRESTIndex(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override @SuppressWarnings({ "cast" })
	public EnqueuedJob index(final UserContext userContext,
							 final M modelObject) {
		if (!Facetables.hasFacet(modelObject,HasOID.class)) throw new IllegalArgumentException(Throwables.message("The {} model object does NOT implements {}",
																												  modelObject.getClass(),HasOID.class));
		HasOID<O> hasOid = Facetables.asFacet((Facetable)modelObject,HasOID.class);
		return _rawRESTIndexDelegate.index(_indexSomeResourceUrl(hasOid.getOid()),
										   userContext,
										   modelObject);
	}
	@Override @SuppressWarnings({ "cast" })
	public EnqueuedJob updateIndex(final UserContext userContext,
							 	   final M modelObject) {
		if (!Facetables.hasFacet(modelObject,HasOID.class)) throw new IllegalArgumentException(Throwables.message("The {} model object does NOT implements {}",
																												  modelObject.getClass(),HasOID.class));
		HasOID<O> hasOid = Facetables.asFacet((Facetable)modelObject,HasOID.class);
		return _rawRESTIndexDelegate.updateIndex(_indexSomeResourceUrl(hasOid.getOid()),
										   		 userContext,
										   		 modelObject);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UNINDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob removeFromIndex(final UserContext userContext,
							   		   final O oid) {
		return _rawRESTIndexDelegate.removeFromIndex(_indexSomeResourceUrl(oid),
												   	 userContext,
												   	 null);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext,
								  		  final Collection<O> all) {
		return _rawRESTIndexDelegate.removeFromIndex(_indexAllResourcesUrl(),
										   			 userContext,
										   			 all);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext) {
		return _rawRESTIndexDelegate.removeFromIndex(_indexAllResourcesUrl(),
										   	  		 userContext,
										   	  		 null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob reIndex(final UserContext userContext,
						 	   final O oid) {
		return _rawRESTIndexDelegate.index(_indexSomeResourceUrl(oid),
										   userContext,
								   	   	   null);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext,
								  final Collection<O> all) {
		return _rawRESTIndexDelegate.index(_indexAllResourcesUrl(),
										   userContext,
								   	   	   all);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext) {
		return _rawRESTIndexDelegate.index(_indexAllResourcesUrl(),
										   userContext,
										   null);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private Url _composeIndexURIFor(final Path path) {
		return this.composeURIFor(Paths.forUrlPaths().join("index",
					     						  			path));
	}
	private Url _indexSomeResourceUrl(final O oid) {
		return _composeIndexURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
										   			.pathOfEntity(oid));
	}
	protected Url _indexAllResourcesUrl() {
		return _composeIndexURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForModelObjectPersistence.class)
									  	   			.pathOfAllEntities());
	}
}