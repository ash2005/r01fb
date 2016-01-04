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
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilder;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilderForModelObject;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.types.Path;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

public abstract class RESTIndexServicesProxyBase<O extends OID,M extends IndexableModelObject<O>> 
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements IndexServicesForModelObject<O,M> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final DelegateForRawRESTIndex _rawRESTIndexDelegate;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends ServicesRESTResourcePathBuilderForModelObject<O>>
		   RESTIndexServicesProxyBase(final Marshaller marshaller,
									  final Class<M> modelObjectType,
									  final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
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
	private SerializedURL _indexSomeResourceUrl(final O oid) {
		return this.composeIndexURIFor(Path.of("index")
										   .add(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
												   	.pathOfEntity(oid)));
	}
	protected SerializedURL _indexAllResourcesUrl() {
		return this.composeIndexURIFor(Path.of("index")
									  	   .add(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
									  			    .pathOfAllEntities()));
	}
	/**
	 * Composes the complete REST endpoint URI for a path
	 * @param path
	 * @return
	 */
	protected SerializedURL composeIndexURIFor(final Path path) {
		ServicesRESTResourcePathBuilder pathBuilder = this.getServicesRESTResourceUrlPathBuilder();
		Path uri = Path.of(pathBuilder.getHost())
					   .add(pathBuilder.getSearchIndexEndPointBasePath())
					   .add(path);
		return SerializedURL.create(uri.asString());
	}
}
