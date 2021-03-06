package r01f.services.persistence;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.persistence.search.HasSearcher;
import r01f.persistence.search.Searcher;
import r01f.services.interfaces.SearchServices;
import r01f.usercontext.UserContext;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Core service base for search services
 */
@Accessors(prefix="_")
public abstract class CoreSearchServicesForModelObjectBase<F extends SearchFilter,I extends SearchResultItem> 
     		  extends CoreServiceBase					  
     	   implements SearchServices<F,I>,
  			 		  HasSearcher<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//	 
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search component 
	 */
	@Inject
	@Getter protected Searcher<F,I> _searcher;
	/**
	 * Search properties
	 * (this properties are set at BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase type)
	 */
	@Inject @XMLPropertiesComponent("searchpersistence")
	@Getter protected XMLPropertiesForAppComponent _searchProperties;
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int countRecords(final UserContext userContext,
							final F filter) {
		return  this.createDelegateAs(SearchServices.class)
						.countRecords(userContext,
									  filter);
	}
	@Override
	public <O extends OID> Collection<O> filterRecordsOids(final UserContext userContext,
													   	   final F filter) {
		return  this.createDelegateAs(SearchServices.class)
						.filterRecordsOids(userContext,
										   filter);
	}
	@Override
	public SearchResults<F,I> filterRecords(final UserContext userContext,
	                    		 			final F filter,
	                    		 			final int firstRowNum,final int numberOfRows) {
		return  this.createDelegateAs(SearchServices.class)
						.filterRecords(userContext, 
									   filter,
									   firstRowNum,numberOfRows);
	}

}
