package r01f.test.persistence;

import java.util.Iterator;

import r01f.locale.Language;
import r01f.model.facets.HasName;
import r01f.model.facets.LangDependentNamed;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;
import r01f.model.facets.LangInDependentNamed;
import r01f.model.facets.LangInDependentNamed.HasLangInDependentNamedFacet;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;

public class TestSearchUtil {
	/**
	 * Prints info about search results
	 * @param results
	 */
	public static <F extends SearchFilterForModelObject,
				   I extends SearchResultItemForModelObject<?,?>> void debugSearchResults(final SearchResults<F,I> results) {
		if (results.hasData()) {
			System.out.println(">>>>Found " +  results.getTotalItemsCount() + ": results");
			for (I item : results.getPageItems()) {
				StringBuilder sb = new StringBuilder();
				sb.append(item.getOid() + " (" + item.getClass() + ") > ");
				
				String itemSum = null; 
				
				Summary sum = item.asSummarizable()
								  .getSummary();
				// There's a summary at the search result item
				if (sum != null) {
					if (sum.isLangDependent()) {
						LangDependentSummary langDepSum = sum.asLangDependent();
						for (Iterator<Language> langIt = langDepSum.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
							Language lang = langIt.next();
							sb.append("[" + lang + "]: " + langDepSum.asString(lang));
							if (langIt.hasNext()) sb.append(", ");
						}
					} else {
						LangIndependentSummary langIndepSum = sum.asLangIndependent();
						sb.append(langIndepSum.asString());
					}
					itemSum = sb.toString();
					System.out.println("\t-" + itemSum);					
				}
				// The item's summary is NOT available... try to print something about the model object's name
				else if (item.getModelObject() != null && item.getModelObject().hasFacet(HasName.class)) {
					if (item.getModelObject().hasFacet(HasLangDependentNamedFacet.class)) {
						LangDependentNamed langNames = item.getModelObject()
														   .asFacet(HasLangDependentNamedFacet.class)
														   .asLangDependentNamed();
						for (Iterator<Language> langIt = langNames.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
							Language lang = langIt.next();
							sb.append("[" + lang + "]: " + langNames.getNameIn(lang));
							if (langIt.hasNext()) sb.append(", ");
						}
					} else {
						LangInDependentNamed name = item.getModelObject()
														.asFacet(HasLangInDependentNamedFacet.class)
														.asLangInDependentNamed();
						sb.append(name.getName());
					}
					itemSum = sb.toString();
					System.out.println("\t-" + itemSum);
				} 
				// There's nowhere to get a summary
				else {
					System.out.println("\t-->NO summary");
				}
			}
		} else {
			System.out.println(">> NO results");
		}
	}
}
