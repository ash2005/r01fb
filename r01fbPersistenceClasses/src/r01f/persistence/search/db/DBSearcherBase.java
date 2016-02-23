package r01f.persistence.search.db;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.guids.OIDs;
import r01f.locale.LanguageTexts;
import r01f.model.IndexableModelObject;
import r01f.model.ModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.model.facets.HasNumericID;
import r01f.model.facets.HasOID;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;
import r01f.model.facets.LangInDependentNamed.HasLangInDependentNamedFacet;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.search.Searcher;
import r01f.types.Factory;
import r01f.types.summary.Summary;
import r01f.types.summary.SummaryLanguageTextsBacked;
import r01f.types.summary.SummaryStringBacked;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for types that implements db searching ({@link R01EDBSearcher} interface)
 * @param <F>
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class DBSearcherBase<F extends SearchFilterForModelObject,
									 I extends SearchResultItemForModelObject<? extends OID,? extends IndexableModelObject>> 
           implements Searcher<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The entity manager MUST be provided by the higher level layer because there is where the 
	 * transaction begins and that transaction could span more than one persistence types 
	 * (ie the CRUD persistence and the relations persistence)
	 */
	@Getter(AccessLevel.PROTECTED) private final EntityManager _entityManager;	
	/**
	 * The name of the entity
	 */
	@Getter(AccessLevel.PROTECTED) private final Class<? extends DBEntity> _entityType;
	/**
	 * Factory of {@link SearchResultItemForModelObject} instances
	 */
	@Getter(AccessLevel.PROTECTED) private final Factory<I> _searchResultItemsFactory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH   
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public int countRecords(final UserContext userContext,
							final F filter) {
		// [1]: Build the query
		Query q = DBSearchQuery.of(_entityManager)
							   .forEntity(_entityType)
							   .withPredicates(filter.getBooleanQuery())
					   .getCountQuery();
		// [2]: run the query
		List<Integer> results = q.getResultList();
		return !CollectionUtils.isNullOrEmpty(results) ? results.get(0) 
													   : 0;
	}
	@Override 
	public SearchResults<F,I> filterRecords(final UserContext userContext,
											final F filter,
									   		final int firstResultItemOrder,final int numberOfResults) {
		// [0]-Count the total items 
		int totalItemsCount = this.countRecords(userContext,
												filter);
		
		// [1]-build the query
		Query q = DBSearchQuery.of(_entityManager)
								   .forEntity(_entityType)
								   .withPredicates(filter.getBooleanQuery())
							   .getResultsQuery();
		q.setFirstResult(firstResultItemOrder);
		q.setMaxResults(numberOfResults);
		Collection<? extends DBEntityForModelObject<?>> results = q.getResultList();
		
		// [3]-run the query and transform results
		Collection<I> items = CollectionUtils.hasData(results) ? Collections2.transform(results,
																		 				new Function<DBEntityForModelObject<?>,I>() {
																								@Override
																								public I apply(final DBEntityForModelObject<?> dbEntity) {
																									return _createSearchResultItemFor(userContext,
																																	  dbEntity);
																								}
																						})
															   : null;
		return new SearchResults<F,I>(filter,
									  totalItemsCount,firstResultItemOrder,
									  items);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns ALL record oids that satisfy a given filter
	 * @param userContext
	 * @param filter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <O extends OID> Collection<O> filterRecordsOids(final UserContext userContext,
														   final F filter) {
		// [0]: guess the model object type
		Class<? extends ModelObject> modelObjType = CollectionUtils.pickOneAndOnlyElement(filter.getFilteredModelObjectTypes(),
																						  "This type is only suitable for filters with a single model object type");
		final ModelObjectTypeMetaData modelObjMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjType);
		
		// [1]: Build the query
		Query q = DBSearchQuery.of(_entityManager)
								   .forEntity(_entityType)
								   .withPredicates(filter.getBooleanQuery())
							   .getOidsQuery();
		
		// [3]: Run the query and transform results 
		Collection<String> oidsAsDB = q.getResultList();
		Collection<? extends OID> oids = CollectionUtils.hasData(oidsAsDB) 
												? Collections2.transform(oidsAsDB,
																		 new Function<String,OID>() {
																					@Override
																					public OID apply(final String pk) {
																						Class<? extends OID> oidType = (Class<? extends OID>)modelObjMetaData.getOIDFieldMetaData()
																															 		   						 		.getDataType();
																						return OIDs.createOIDFromString(oidType,pk);
																					}
																		 })
												: null;
		return (Collection<O>)oids;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	RESULT ITEMS 
/////////////////////////////////////////////////////////////////////////////////////////
	private I _createSearchResultItemFor(final UserContext userContext,
										 final DBEntityForModelObject<?> dbEntity) {
		// [0] - Get the model object from the dbEntity
		IndexableModelObject modelObj = dbEntity.toModelObject(userContext);
		
		// [1] - Use the search result item factory to create an item
		I item = _searchResultItemsFactory.create();	
		
		// [2] - Create the item 
		// ... common fields
		_setResultItemCommonFields(item,
							 	   modelObj);
		
		// [3] - Set the model object
		((SearchResultItemForModelObject<?,?>)item).unsafeSetModelObject(modelObj);
		
		// [4] - Return
		return item;
		
	}
	@SuppressWarnings("unchecked")
	protected void _setResultItemCommonFields(final I item,
									    	  final IndexableModelObject modelObj) {
		ModelObjectTypeMetaData modelObjectMetadata = ModelObjectTypeMetaDataBuilder.createFor(modelObj.getClass());
		
		// Model object type
		item.unsafeSetModelObjectType((Class<? extends IndexableModelObject>)modelObj.getClass());
		item.setModelObjectTypeCode(modelObjectMetadata.getTypeCode());	
		
		// OID
		if (modelObjectMetadata.hasFacet(HasOID.class)) {
			HasOID<? extends OID> hasOidModelObj = (HasOID<? extends OID>)modelObj;
			OID oid = hasOidModelObj.getOid();
			item.unsafeSetOid(oid);
		}	
		// numeric id
		if (modelObjectMetadata.hasFacet(HasNumericID.class)) {
			item.setNumericId(modelObj.getNumericId());
		}
		// EntityVersion 
		if (modelObjectMetadata.hasFacet(HasEntityVersion.class)) {
			item.setEntityVersion(modelObj.getEntityVersion());
		}
		// Summary
		if (modelObjectMetadata.hasFacet(HasLangDependentNamedFacet.class)) {
			HasLangDependentNamedFacet modelObjHasNames = (HasLangDependentNamedFacet)modelObj;
			LanguageTexts names = modelObjHasNames.getNamesByLanguage();
			Summary summary = SummaryLanguageTextsBacked.of(names);
			item.asSummarizable()
				.setSummary(summary);
		} else {
			HasLangInDependentNamedFacet modelObjHasName = (HasLangInDependentNamedFacet)modelObj;
			String name = modelObjHasName.getName();
			Summary summary = SummaryStringBacked.of(name);
			item.asSummarizable()
				.setSummary(summary);
		}
	}
}
