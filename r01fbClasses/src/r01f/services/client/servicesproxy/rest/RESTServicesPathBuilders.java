package r01f.services.client.servicesproxy.rest;

import java.util.Date;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.types.Path;
import r01f.types.Range;
import r01f.types.weburl.SerializedURL;
import r01f.util.types.Dates;

import com.google.common.annotations.GwtIncompatible;

/**
 * Interfaces used at {@link RESTServicesProxyBase} and {@link RESTServicesForModelObjectProxyBase} and {@link RESTVersionableCRUDServicesProxyBase}
 * to build the REST endpoint url
 */
public class RESTServicesPathBuilders {
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFACES
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface ServicesRESTResourcePathBuilder {
		public SerializedURL getHost();
		public Path getPersistenceEndPointBasePath();
		public Path getSearchIndexEndPointBasePath();
		public Path pathOfIndex();
		public Path pathOfResource();
	}
	public static interface ServicesRESTResourcePathBuilderForModelObject<O extends OID>
					extends ServicesRESTResourcePathBuilder {
		public Path pathOfEntity(final O oid);
		public Path pathOfAllEntities();
		public Path pathOfEntityList();		
		@GwtIncompatible("Range NOT usable in GWT")
		public Path pathOfEntityListByCreateDate(final Range<Date> dateRange);
		@GwtIncompatible("Range NOT usable in GWT")
		public Path pathOfEntityListByLastUpdateDate(final Range<Date> dateRange);
		public Path pathOfEntityListByCreator(final UserCode creatorUserCode);
		public Path pathOfEntityListByLastUpdator(final UserCode lastUpdatorUserCode);		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class ServicesRESTResourcePathBuilderForModelObjectBase<O extends OID>
			 		  implements ServicesRESTResourcePathBuilderForModelObject<O> {
		private final SerializedURL _host;
		private final Path _persistenceEndPointBasePath;
		private final Path _searchIndexEndPointBasePath;
		private final Path _indexPath;
		private final Path _resourcePath;
		
		public ServicesRESTResourcePathBuilderForModelObjectBase(final SerializedURL host,
																 final Path persistenceEndPointBasePath,		// the WAR where the persistence REST services are located
																 final Path searchIndexEndPointBasePath,		// the WAR where the search REST services are located
																 final Path indexPath,
																 final Path resourcePath) {
			_host = host;
			_persistenceEndPointBasePath = persistenceEndPointBasePath;	
			_searchIndexEndPointBasePath = searchIndexEndPointBasePath;
			_indexPath = indexPath;
			_resourcePath = resourcePath;
		}
		public ServicesRESTResourcePathBuilderForModelObjectBase(final SerializedURL host,
																 final Path persistenceEndPointBasePath,	// the WAR where the persistence REST services are located	
																 final Path resourcePath) {
			this(host,
				 persistenceEndPointBasePath,
				 null,	// no index
				 null,	// no index
				 resourcePath);
		}	
		@Override
		public SerializedURL getHost() {
			return _host;
		}
		@Override
		public Path getPersistenceEndPointBasePath() {
			return Path.of(_persistenceEndPointBasePath);
		}
		@Override
		public Path getSearchIndexEndPointBasePath() {
			return Path.of(_searchIndexEndPointBasePath);
		}
		@Override
		public Path pathOfIndex() {
			return Path.of(_indexPath);
		}
		@Override
		public Path pathOfResource() {
			return Path.of(_resourcePath);
		}
		@Override
		public Path pathOfEntity(final O oid) {
			return this.pathOfResource()
					   .add(oid);
		}
		@Override
		public Path pathOfAllEntities() {
			return this.pathOfResource();
		}
		@Override
		public Path pathOfEntityList() {
			return this.pathOfAllEntities()
					   .add("list");
		}
		@Override @GwtIncompatible("Range NOT usable in GWT")
		public Path pathOfEntityListByCreateDate(final Range<Date> dateRange) {
			return this.pathOfEntityList()
					   .add("byCreateDate")
					   .add(dateRange.asString());
		}
		@Override @GwtIncompatible("Range NOT usable in GWT")
		public Path pathOfEntityListByLastUpdateDate(final Range<Date> dateRange) {
			return this.pathOfEntityList()
					   .add("byLastUpdateDate")
					   .add(dateRange.asString());
		}
		@Override
		public Path pathOfEntityListByCreator(final UserCode creatorUserCode) {
			return this.pathOfEntityList()
					   .add("byCreator")
					   .add(creatorUserCode.asString());
		}
		@Override
		public Path pathOfEntityListByLastUpdator(final UserCode lastUpdatorUserCode) {
			return this.pathOfEntityList()
					   .add("byLastUpdator")
					   .add(lastUpdatorUserCode.asString());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  VERSIONABLE MODEL OBJECT
/////////////////////////////////////////////////////////////////////////////////////////
	public static abstract class ServicesRESTResourcePathBuilderForVersionableModelObjectBase<O extends OIDForVersionableModelObject>
						 extends ServicesRESTResourcePathBuilderForModelObjectBase<O> {
		
		public ServicesRESTResourcePathBuilderForVersionableModelObjectBase(final SerializedURL host,
																		    final Path persistenceEndPointBasePath,final Path searchIndexEndPointBasePath,
																			final Path indexPath,final Path resourcePath) {
			super(host,
				  persistenceEndPointBasePath,
				  searchIndexEndPointBasePath,
				  indexPath,resourcePath);
		}
		public ServicesRESTResourcePathBuilderForVersionableModelObjectBase(final SerializedURL host,
																			final Path persistenceEndPointBasePath,
																 			final Path resourcePath) {
			this(host,
				 persistenceEndPointBasePath,
				 null,	// no index
				 null,	// no index
				 resourcePath);
		}
		@Override
		public Path pathOfEntity(final O oid) {
			return this.pathOfAllVersions(oid.getOid())			// beware!!
					   .add(oid.getVersion());
		}
		public Path pathOfVersionIndependent(final VersionIndependentOID oid) {
			return this.pathOfResource()
					   .add(oid);	
		}
		public Path pathOfAllVersions(final VersionIndependentOID oid) {
			return this.pathOfVersionIndependent(oid)
					   .add("versions");
		}
		public Path pathOfWorkVersion(final VersionIndependentOID oid) {
			return this.pathOfAllVersions(oid)
					   .add("workVersion");
		}
		public Path pathOfActiveVersion(final VersionIndependentOID oid) {
			return this.pathOfAllVersions(oid)
					   .add("activeVersion");
		}
		public Path pathOfActiveVersionAt(final VersionIndependentOID oid,final Date date) {
			return this.pathOfAllVersions(oid)
					   .add("activeAt")
					   .add(Dates.asEpochTimeStamp(date));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface RESTServicesPathBuilderForIndexManagement 
				    extends ServicesRESTResourcePathBuilder {
		// just a marker interface
	}
}
