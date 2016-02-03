package r01f.services.client.servicesproxy.rest;

import java.util.Date;

import com.google.common.annotations.GwtIncompatible;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.types.Paths;
import r01f.types.Range;
import r01f.types.UrlPath;
import r01f.types.url.Url;
import r01f.util.types.Dates;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Interfaces used at {@link RESTServicesProxyBase} and {@link RESTServicesForModelObjectProxyBase} and {@link RESTServicesForVersionableCRUDServicesProxyBase}
 * to build the REST endpoint url
 */
@GwtIncompatible("Not used from GWT")
@Slf4j
public class RESTServiceResourceUrlPathBuilders {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Encapsulates the service endpoint url (the host & base path) 
	 * which usually is loaded from the properties file
	 * at a standard section as:
	 * <pre class='brush:xml'>
	 * 	<restEndPoints>
	 *		<host>http://localhost:8080/</host>				
	 *		<persistenceEndPointBasePath>xxRESTServicesWar</persistenceEndPointBasePath>	 
	 *	</restEndPoints>
	 * </pre>
	 */
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public static class RESTServiceEndPointUrl {
		@Getter private final Url _host;
		@Getter private final UrlPath _endPointBasePath;
		
		public RESTServiceEndPointUrl(final XMLPropertiesForAppComponent clientProps,
									  final String selector) {
			this(clientProps.propertyAt("client/restEndPoints/host")
					   .asUrl(),
				 clientProps.propertyAt(Strings.customized("client/restEndPoints/{}EndPointBasePath",selector))
					  .asUrlPath());
			log.warn("REST service endpoint for {}: host={}, basePath={}",
					 selector,_host,_endPointBasePath);
			// warn if the properties could NOT be retrieved
			if (!clientProps.propertyAt("client/restEndPoints/host").exist()
						||
				!clientProps.propertyAt(Strings.customized("client/restEndPoints/{}EndPointBasePath",selector)).exist()) {
				// the config is NOT valid!
				throw new IllegalStateException(Throwables.message("The provided client properties file DOES NOT contains the REST endpoint section; " +
																   "ensure that it contains the xPaths client/restEndPoints/host and client/restEndPoints/{}EndPointBasePath",
																   selector));
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GENERAL
/////////////////////////////////////////////////////////////////////////////////////////
	@GwtIncompatible("Not used from GWT")
	public static interface RESTServiceResourceUrlPathBuilder {
		public Url getHost();
		public UrlPath getEndPointBasePath();
		public UrlPath pathOfResource();
	}
	@GwtIncompatible("Not used from GWT")
	@RequiredArgsConstructor
	public static abstract class RESTServiceResourceUrlPathBuilderBase 
					  implements RESTServiceResourceUrlPathBuilder {
		private final Url _host;					// localhost
		private final UrlPath _endPointBasePath;	// xxFooWar
		private final UrlPath _resourcePath;		// bar/baz
		
		public RESTServiceResourceUrlPathBuilderBase(final RESTServiceEndPointUrl endPointUrl,
													 final UrlPath resourcePath) {
			this(endPointUrl.getHost(),
				 endPointUrl.getEndPointBasePath(),
				 resourcePath);
			if (resourcePath == null) throw new IllegalArgumentException("The REST resource url path is mandatory");
		}
		
		@Override
		public Url getHost() {
			return _host;
		}
		@Override
		public UrlPath getEndPointBasePath() {
			return _endPointBasePath;
		}
		@Override
		public UrlPath pathOfResource() {
			return UrlPath.of(_resourcePath);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT
/////////////////////////////////////////////////////////////////////////////////////////
	@GwtIncompatible("Not used from GWT")
	public static interface RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O extends OID>
					extends RESTServiceResourceUrlPathBuilder {
		public UrlPath pathOfEntity(final O oid);
		public UrlPath pathOfAllEntities();
		public UrlPath pathOfEntityList();	
		public UrlPath pathOfEntityListByCreateDate(final Range<Date> dateRange);
		public UrlPath pathOfEntityListByLastUpdateDate(final Range<Date> dateRange);
		public UrlPath pathOfEntityListByCreator(final UserCode creatorUserCode);
		public UrlPath pathOfEntityListByLastUpdator(final UserCode lastUpdatorUserCode);		
	}
	@GwtIncompatible("Not used from GWT")
	public static abstract class RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase<O extends OID>
						 extends RESTServiceResourceUrlPathBuilderBase
			 		  implements RESTServiceResourceUrlPathBuilderForModelObjectPersistence<O> {		
		public RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase(final RESTServiceEndPointUrl endPointUrl,
																			  final UrlPath resourcePath) {
			super(endPointUrl,
				  resourcePath);
		}
		public RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase(final Url host,
																			  final UrlPath endPointBasePath,
																			  final UrlPath resourcePath) {
			super(host,
				  endPointBasePath,
				  resourcePath);
		}		
		@Override
		public UrlPath pathOfEntity(final O oid) {
			return Paths.forUrlPaths().join(this.pathOfResource(),
					   						oid);
		}
		@Override
		public UrlPath pathOfAllEntities() {
			return this.pathOfResource();
		}
		@Override
		public UrlPath pathOfEntityList() {
			return Paths.forUrlPaths().join(this.pathOfAllEntities(),
					   						"list");
		}
		@Override @GwtIncompatible("Range NOT usable in GWT")
		public UrlPath pathOfEntityListByCreateDate(final Range<Date> dateRange) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byCreateDate",
					   						dateRange.asString());
		}
		@Override @GwtIncompatible("Range NOT usable in GWT")
		public UrlPath pathOfEntityListByLastUpdateDate(final Range<Date> dateRange) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byLastUpdateDate",
					   						dateRange.asString());
		}
		@Override
		public UrlPath pathOfEntityListByCreator(final UserCode creatorUserCode) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   					    "byCreator",
					   					    creatorUserCode.asString());
		}
		@Override
		public UrlPath pathOfEntityListByLastUpdator(final UserCode lastUpdatorUserCode) {
			return Paths.forUrlPaths().join(this.pathOfEntityList(),
					   						"byLastUpdator",
					   						lastUpdatorUserCode.asString());
		}
	}
	@GwtIncompatible("Not used from GWT")
	public static abstract class RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase<O extends OIDForVersionableModelObject>
						 extends RESTServiceResourceUrlPathBuilderForModelObjectPersistenceBase<O> {
		
		public RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase(final Url host,
																						 final UrlPath endPointBasePath,
																		       			 final UrlPath resourcePath) {
			super(host,
				  endPointBasePath,
				  resourcePath);
		}
		@Override
		public UrlPath pathOfEntity(final O oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid.getOid()),			// beware!!
					   						oid.getVersion());
		}
		public UrlPath pathOfVersionIndependent(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfResource(),
					   						oid);	
		}
		public UrlPath pathOfAllVersions(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfVersionIndependent(oid),
					   						"versions");
		}
		public UrlPath pathOfWorkVersion(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"workVersion");
		}
		public UrlPath pathOfActiveVersion(final VersionIndependentOID oid) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"activeVersion");
		}
		public UrlPath pathOfActiveVersionAt(final VersionIndependentOID oid,final Date date) {
			return Paths.forUrlPaths().join(this.pathOfAllVersions(oid),
					   						"activeAt",
					   						Dates.asEpochTimeStamp(date));
		}
	}
}