package r01f.persistence.search.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.locale.Language;
import r01f.model.search.query.BooleanQueryClause;
import r01f.model.search.query.BooleanQueryClause.QualifiedQueryClause;
import r01f.model.search.query.BooleanQueryClause.QueryClauseOccur;
import r01f.model.search.query.ContainedInQueryClause;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.model.search.query.EqualsQueryClause;
import r01f.model.search.query.QueryClause;
import r01f.model.search.query.RangeQueryClause;
import r01f.persistence.db.DBEntity;
import r01f.persistence.search.QueryBase;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;



/**
 * Utility to run search queries using JPA:
 * <pre class='brush:java'>
 * 		// Put eclipselink.jar and the mysql connector driver in the classpath
 * 		// ... also copy the persistence.xml in the META-INF dir
 *	    Properties props = new Properties();
 *	    props.put("javax.persistence.jdbc.user","r01e");
 *	    props.put("javax.persistence.jdbc.password","r01e");
 *	    props.put("javax.persistence.jdbc.driver","com.mysql.jdbc.Driver");		// IMPORTANT!! When in Tomcat & MySQL, set jconector MySQL's driver at $CATALINA_HOME/lib  
 *	    props.put("javax.persistence.jdbc.url","jdbc:mysql://localhost:3306/r01e");
 *	    props.put("eclipselink.target-database","org.eclipse.persistence.platform.database.MySQLPlatform");		// org.eclipse.persistence.platform.database.oracle.OraclePlatform
 *	    
 *	    
 *	    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistenceUnit.xxx",props);
 *	    EntityManager em = emf.createEntityManager();
 *	    DBSearchQuery qry = DBSearchQuery.of(em)
 *	    								 .forEntity("MyDBEntity")		// the name of the JPA entity class
 *	    								 .withPredicates(BooleanQueryClause.create()
 *	    										 				.field("oid").must().beWithin(MyEntityOID.forId("xxxlc814593391b1d721a3067bdde926665c5e952"),
 *	    										 											  MyEntityOID.forId("xxxlc9145932d760a241b21e51fc9b298602689bd"))
 *	    										 				.field("createDate").must().beInsideDateRange(Range.atMost(new Date()))
 *	    										 				.build());
 *	    Query jpaQry = qry.getCountQuery();
 *	    System.out.println(">>>" + jpaQry.getSingleResult());
 * </pre>
 */
@Slf4j
public class DBSearchQuery 
	 extends QueryBase<DBSearchQuery> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final EntityManager _entityManager;	
	
	private Class<? extends DBEntity> _dbEntityType;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	private DBSearchQuery(final EntityManager entityManager) {
		this(entityManager,
			 null);				// no user interface language specified
	}
	private DBSearchQuery(final EntityManager entityManager,
						  final Language uiLanguage) {
		super(new BooleanQueryClause(new LinkedHashSet<QualifiedQueryClause<? extends QueryClause>>()),
			  uiLanguage);
		_entityManager = entityManager;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT API
/////////////////////////////////////////////////////////////////////////////////////////
	public static DBSearchQueryEntityStep of(final EntityManager entityManager) {
		return new DBSearchQueryEntityStep(entityManager);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class DBSearchQueryEntityStep {
		private final EntityManager _entityManager;	
		
		public DBSearchQueryPredicatesStep forEntity(final Class<? extends DBEntity> dbEntityType) {
			return new DBSearchQueryPredicatesStep(_entityManager,
												   dbEntityType);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class DBSearchQueryPredicatesStep {
		private final EntityManager _entityManager;
		private final Class<? extends DBEntity> _dbEntityType;
		
		public DBSearchQuery withPredicates(final BooleanQueryClause pred) {
			DBSearchQuery outQuery = new DBSearchQuery(_entityManager);
			outQuery._dbEntityType = _dbEntityType;
			outQuery.withPredicates(pred);
			return outQuery;
		}
		public DBSearchQuery noPredicates() {
			DBSearchQuery outQuery = new DBSearchQuery(_entityManager);
			outQuery._dbEntityType = _dbEntityType;
			return outQuery;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the count search query
	 * @return
	 */
	public Query getCountQuery() {
		// Compose JPQL
		String jpql = _composeJPQL("COUNT(e)",
								   _dbEntityType,
								   _jpqlQueryFrom(this.getContainerBoolQry()));
		// Create the query
		Query outQry = _entityManager.createQuery(jpql);
		_setJPAQueryParams(outQry,this.getContainerBoolQry()); 
		return outQry;
	}
	/**
	 * Returns the results retrieving query
	 * @return
	 */
	public Query getResultsQuery() {
		// Compose JPQL
		String jpql = _composeJPQL("e",
								   _dbEntityType,
								   _jpqlQueryFrom(this.getContainerBoolQry()));
		// Create the query
		Query outQry = _entityManager.createQuery(jpql);
		_setJPAQueryParams(outQry,this.getContainerBoolQry()); 
		return outQry;
	}
	/**
	 * Returns the results retrieving query
	 * @return
	 */
	public Query getOidsQuery() {
		// Compose JPQL
		String jpql = _composeJPQL("e._oid",
								   _dbEntityType,
								   _jpqlQueryFrom(this.getContainerBoolQry()));
		// Create the query
		Query outQry = _entityManager.createQuery(jpql);
		_setJPAQueryParams(outQry,this.getContainerBoolQry()); 
		return outQry;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _composeJPQL(final String colSpec,
									   final Class<? extends DBEntity> dbEntityType,
									   final String jpqlWhere) {
		String jpql = null;
		String entityName = ReflectionUtils.classNameFromClassNameIncludingPackage(dbEntityType.getName());
		if (jpqlWhere != null) {
			jpql = "select " + colSpec + " from " + entityName + " e where " + jpqlWhere;
		} else {
			jpql = "select " + colSpec + " from " + entityName;
		}
		log.debug("DB Search query: {}",jpql);
		return jpql;
	}
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  QUERY
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _setJPAQueryParams(final Query qry,
									  final BooleanQueryClause qryClause) {
		Set<QualifiedQueryClause<? extends QueryClause>> clauses = qryClause.getClauses();
		for (Iterator<QualifiedQueryClause<? extends QueryClause>> clauseIt = clauses.iterator(); clauseIt.hasNext(); ) {
			QueryClause clause = clauseIt.next().getClause();
			
			String dbFieldId = clause.getFieldId().asString();
			
			if (clause instanceof BooleanQueryClause) {
				BooleanQueryClause boolQry = (BooleanQueryClause)clause;
				_setJPAQueryParams(qry,
						  		   boolQry);		// recurse!
			} 
			else if (clause instanceof EqualsQueryClause<?>) {
				EqualsQueryClause<?> eqQry = (EqualsQueryClause<?>)clause;
				qry.setParameter(dbFieldId,
								 eqQry.getValue().toString());
			} 
			else if (clause instanceof ContainsTextQueryClause) {
				ContainsTextQueryClause containsTxtClause = (ContainsTextQueryClause)clause;
				qry.setParameter(dbFieldId,
								 containsTxtClause.getText());
			} 
			else if (clause instanceof RangeQueryClause<?>) {
				RangeQueryClause<?> rangeQry = (RangeQueryClause<?>)clause;
				if (rangeQry.getRange().hasLowerBound() && rangeQry.getRange().hasUpperBound()) {
					qry.setParameter(dbFieldId + "Start",rangeQry.getRange().lowerEndpoint());
					qry.setParameter(dbFieldId + "End",rangeQry.getRange().lowerEndpoint());
				} else if (rangeQry.getRange().hasLowerBound()) {
					qry.setParameter(dbFieldId,rangeQry.getRange().lowerEndpoint());
				} else if (rangeQry.getRange().hasUpperBound()) {
					qry.setParameter(dbFieldId,rangeQry.getRange().upperEndpoint());
				}
			} 
			else if (clause instanceof ContainedInQueryClause<?>) {
				ContainedInQueryClause<?> containedInQry = (ContainedInQueryClause<?>)clause;
				Collection<?> spectrum = Lists.newArrayList(containedInQry.getSpectrum());
				qry.setParameter(dbFieldId,spectrum);
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  JPQL
/////////////////////////////////////////////////////////////////////////////////////////
	protected String _jpqlQueryFrom(final BooleanQueryClause qryClause) {
		if (qryClause == null || CollectionUtils.isNullOrEmpty(qryClause.getClauses())) {
			log.warn("A filter with NO filter parameters was received... al records will be returned");
			return null;
		}
		
		String dbFieldId = qryClause.getFieldId().asString();
		
		StringBuilder outJPQL = new StringBuilder();
		outJPQL.append("(");
		
		Set<QualifiedQueryClause<? extends QueryClause>> clauses = qryClause.getClauses();
		
		QueryClauseOccur prevClauseOccur = null;
		for (Iterator<QualifiedQueryClause<? extends QueryClause>> clauseIt = clauses.iterator(); clauseIt.hasNext(); ) {
			QualifiedQueryClause<? extends QueryClause> clause = clauseIt.next();
			
			String jpqlQuery = _jpqlQueryFrom(clause.getClause());
			String jpqlJoin = _jpqlJoinFor(clause.getOccur());
			
			if (jpqlQuery != null) {
				if (prevClauseOccur != null) outJPQL.append(jpqlJoin);
				if (clauseIt.hasNext()) prevClauseOccur = clause.getOccur();
				
				outJPQL.append("(");
				outJPQL.append(jpqlQuery);	// The clause
				outJPQL.append(")");
			} else {
				log.error("A null lucene query was returned for field {}",
						  dbFieldId);
			}
		}		
		outJPQL.append(")");
		return outJPQL.toString();
	}
	private <Q extends QueryClause> String _jpqlQueryFrom(final Q clause) {
		if (clause == null) return null;
		String outJPQL = null;
		if (clause instanceof BooleanQueryClause) {
			outJPQL = _jpqlQueryFrom((BooleanQueryClause)clause);
		} 
		else if (clause instanceof EqualsQueryClause<?>) {
			outJPQL = _jpqlQueryFrom((EqualsQueryClause<?>)clause);
		} 
		else if (clause instanceof ContainsTextQueryClause) {
			outJPQL = _jpqlQueryFrom((ContainsTextQueryClause)clause);
		} 
		else if (clause instanceof RangeQueryClause<?>) {
			outJPQL = _jpqlQueryFrom((RangeQueryClause<?>)clause);
		} 
		else if (clause instanceof ContainedInQueryClause<?>) {
			outJPQL = _jpqlQueryFrom((ContainedInQueryClause<?>)clause);
		}
		return outJPQL;
	}
	private static String _jpqlQueryFrom(final EqualsQueryClause<?> eqQry) {
		if (eqQry == null || eqQry.getValue() == null) return null;
		
		String dbFieldId = eqQry.getFieldId().asString();
		
		String outJPQL = Strings.of("e._{} = :{}")
							    .customizeWith(dbFieldId,dbFieldId)
							    .asString();
		return outJPQL;
	}
	private static String _jpqlQueryFrom(final ContainsTextQueryClause containsTextQry) {
		String dbFieldId = containsTextQry.getFieldId().asString();
		
		String template = null;
		if (containsTextQry.isBegining()) {
			template = "e._{} LIKE '%:{}'";
		} else if (containsTextQry.isEnding()) {
			template = "e._{} LIKE ':{}%'";			
		} else if (containsTextQry.isContaining()) {
			template = "e._{} LIKE '%:{}%'";
		} else if (containsTextQry.isFullText()) {
			template = "SQL(  'MATCH({}) " + 
			  			    "AGAINST(? IN BOOLEAN MODE)',':{}')";
		}
		return Strings.of(template)
			          .customizeWith(dbFieldId,dbFieldId)
					  .asString();
	}
	private static String _jpqlQueryFrom(final RangeQueryClause<?> rangeQry) {
		String dbFieldId = rangeQry.getFieldId().asString();
		
		String outJPQL = null;
		// TODO mind the bound types... now only CLOSED (inclusive) bounds are being having into account 
		if (rangeQry.getRange().hasLowerBound() && rangeQry.getRange().hasUpperBound()) {
			outJPQL = Strings.of("e._{} BETWEEN :{}Start AND :{}End")		// SQL between is INCLUSIVE (>= lower and <= lower)
							 .customizeWith(dbFieldId,dbFieldId,dbFieldId)
							 .asString();
		} else if (rangeQry.getRange().hasLowerBound()) {
			outJPQL = Strings.of("e._{} >= :{}")
							 .customizeWith(dbFieldId,dbFieldId)
							 .asString();
		} else if (rangeQry.getRange().hasUpperBound()) {
			outJPQL = Strings.of("e._{} <= :{}")
							 .customizeWith(dbFieldId,dbFieldId)
							 .asString();
		}
		return outJPQL;
	}
	private static String _jpqlQueryFrom(final ContainedInQueryClause<?> containedInQry) {
		String dbFieldId = containedInQry.getFieldId().asString();
		String outJPQL = Strings.of("e._{} IN :{}")
							    .customizeWith(dbFieldId,dbFieldId)
							    .asString();
		return outJPQL;
	}
	private static String _jpqlJoinFor(final QueryClauseOccur occur) {
		String outJPQL = null;
		switch(occur) {
		case MUST:
			outJPQL = " AND ";
			break;
		case MUST_NOT:
			outJPQL = " AND NOT ";
			break;
		case SHOULD:
			outJPQL = " OR ";
			break;
		default:
			throw new IllegalArgumentException();
		}
		return outJPQL;
	}
}
