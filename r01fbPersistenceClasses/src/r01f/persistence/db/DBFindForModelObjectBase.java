package r01f.persistence.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.google.common.collect.Lists;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.guids.OIDs;
import r01f.marshalling.Marshaller;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Facetables;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.FindOIDsResultBuilder;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.types.Range;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Accessors(prefix="_")
public abstract class DBFindForModelObjectBase<O extends OID,M extends PersistableModelObject<O>,
							     			   PK extends DBPrimaryKeyForModelObject,DB extends DBEntity & DBEntityForModelObject<PK>>
			  extends DBBaseForModelObject<O,M,
			  				 			   PK,DB> 
	       implements DBFindForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBFindForModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
									final EntityManager entityManager,
									final Marshaller marshaller,
									final XMLPropertiesForAppComponent persistenceProps) {
		super(modelObjectType,dbEntityType,
			  entityManager,
			  marshaller,
			  persistenceProps);
	}
	
	public DBFindForModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
									final TransformsDBEntityIntoModelObject<DB,M> dbEntityIntoModelObjectTransformer,
									final EntityManager entityManager,
									final Marshaller marshaller,
									final XMLPropertiesForAppComponent persistenceProps) {
		super(modelObjectType,dbEntityType,
			  dbEntityIntoModelObjectTransformer,
			  entityManager,
			  marshaller,
			  persistenceProps);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsResult<O> findAll(final UserContext userContext) {
		CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
		
		// use projections to return ONLY the oid (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<DB> root = query.from(_DBEntityType);
		if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
			query.multiselect(root.get("_oid"),
							  root.get("_version"));
		} else {
			query.multiselect(root.get("_oid"));
		}
		List<Tuple> tupleResult = _entityManager.createQuery(query)
														.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();
		FindOIDsResult<O> outOids = _buildResultsFromOids(userContext,
														  tupleResult);
		return outOids;
	}
	@Override
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
											  final Range<Date> createDate) {
		CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
		
		// use projections to return ONLY the oid (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<DB> root = query.from(_DBEntityType);
		if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
			query.multiselect(root.get("_oid"),
							  root.get("_version"));
		} else {
			query.multiselect(root.get("_oid"));
		}
		Predicate where = _buildDateRangePredicate(builder,root,"_createDate",
												   createDate);
		if (where != null) query.where(where);
		List<Tuple> tupleResult = _entityManager.createQuery(query)
														.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();
		FindOIDsResult<O> outOids = _buildResultsFromOids(userContext,
														  tupleResult);
		return outOids;
	}
	@Override
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
												  final Range<Date> lastUpdateDate) {
		CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
		
		// use projections to return ONLY the oid (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<DB> root = query.from(_DBEntityType);
		if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
			query.multiselect(root.get("_oid"),
							  root.get("_version"));
		} else {
			query.multiselect(root.get("_oid"));
		}
		Predicate where = _buildDateRangePredicate(builder,root,"_lastUpdateDate",
												   lastUpdateDate);
		if (where != null) query.where(where);
		List<Tuple> tupleResult = _entityManager.createQuery(query)
														.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();
		FindOIDsResult<O> outOids = _buildResultsFromOids(userContext,
														  tupleResult);
		return outOids;
	}
	@Override
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
										   final UserCode creatorUserCode) {
		CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
		
		// use projections to return ONLY the oid (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<DB> root = query.from(_DBEntityType);
		if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
			query.multiselect(root.get("_oid"),
							  root.get("_version"));
		} else {
			query.multiselect(root.get("_oid"));
		}
		Predicate where = _buildUserPredicate(builder,root,"_creator",
											  creatorUserCode);
		if (where != null) query.where(where);
		List<Tuple> tupleResult = _entityManager.createQuery(query)
														.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();
		FindOIDsResult<O> outOids = _buildResultsFromOids(userContext,
														  tupleResult);
		return outOids;
	}
	@Override
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
											   final UserCode lastUpdatorUserCode) {
		CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
		
		// use projections to return ONLY the oid (see http://stackoverflow.com/questions/12618489/jpa-criteria-api-select-only-specific-columns)
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<DB> root = query.from(_DBEntityType);
		if (Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class)) {
			query.multiselect(root.get("_oid"),
							  root.get("_version"));
		} else {
			query.multiselect(root.get("_oid"));
		}
		Predicate where = _buildUserPredicate(builder,root,"_lastUpdator",
											  lastUpdatorUserCode);
		if (where != null) query.where(where);
		List<Tuple> tupleResult = _entityManager.createQuery(query)
														.setHint(QueryHints.READ_ONLY,HintValues.TRUE)
											    .getResultList();
		FindOIDsResult<O> outOids = _buildResultsFromOids(userContext,
														  tupleResult);
		return outOids;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected FindOIDsResult<O> _buildResultsFromOids(final UserContext userContext,
													  final Collection<Tuple> dbTuples) {
		Class<O> oidType = OIDs.oidTypeFor(_modelObjectType);
		
		FindOIDsResult<O> outOids = null;
		if (CollectionUtils.hasData(dbTuples)) {
			boolean isVersionableOid = Facetables.hasFacet(_modelObjectType,HasVersionableFacet.class);
			Collection<O> oids = Lists.newArrayListWithExpectedSize(dbTuples.size());
			for (Tuple tuple : dbTuples) {
				O oid = null;
				if (isVersionableOid) {
					String versionIndependentOidAsString = (String)tuple.get(0);
					String versionOid = (String)tuple.get(1);
					oid = OIDs.createVersionableOIDFromString(oidType,
							 					 		      versionIndependentOidAsString,versionOid);
				} else {
					String oidAsString = (String)tuple.get(0);
					oid = OIDs.createOIDFromString(oidType,
												   oidAsString);
				}
				oids.add(oid);
			}
			outOids = FindOIDsResultBuilder.using(userContext)
										   .on(_modelObjectType)
										   .foundEntitiesWithOids(oids);
		} else {
			outOids = FindOIDsResultBuilder.using(userContext)
										   .on(_modelObjectType)
										   .noEntityFound();
		}
		return outOids;
	}
	protected Predicate _buildDateRangePredicate(final CriteriaBuilder builder,
												 final Root<DB> root,final String dbColName,
												 final Range<Date> dateRange) {
		Predicate outPredicate = null;
		if (dateRange != null) {
			if (dateRange.hasLowerBound() && dateRange.hasUpperBound()) {
				outPredicate = builder.between(root.<Date>get(dbColName),
											   dateRange.getLowerBound(),dateRange.getUpperBound());
			} else if (dateRange.hasLowerBound()) {
				outPredicate = builder.greaterThanOrEqualTo(root.<Date>get("_createDate"),
															dateRange.getLowerBound());
			} else if (dateRange.hasUpperBound()) {
				outPredicate = builder.lessThanOrEqualTo(root.<Date>get("_createDate"),
														 dateRange.getLowerBound());
			}
		}
		return outPredicate;
	}
	protected Predicate _buildUserPredicate(final CriteriaBuilder builder,
											final Root<DB> root,final String dbColName,
											final UserCode userCode) {
		Predicate outPredicate = null;
		if (userCode != null) {
			outPredicate = builder.equal(root.<String>get(dbColName),
										 userCode.asString());
		}
		return outPredicate;
	}
}
