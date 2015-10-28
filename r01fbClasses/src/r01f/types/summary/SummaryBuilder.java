package r01f.types.summary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.locale.LanguageTexts;
import r01f.model.facets.FullTextSummarizable;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;
import r01f.model.facets.LangInDependentNamed.HasLangInDependentNamedFacet;
import r01f.model.facets.LangNamed.HasLangNamedFacet;
import r01f.model.facets.Summarizable;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.patterns.IsBuilder;
import r01f.types.summary.SummaryBases.InmutableLangIndependentSummary;

/**
 * {@link Summary} builder for {@link Summarizable} and {@link FullTextSummarizable} objects
 * Usage
 * <pre class='brush:java'>
 *	// Language dependent summary backed by a LanguageTexts object
 *	LangDependentSummary summary1 = SummaryBuilder.languageDependent()
 *												  .create(LanguageTextsBuilder.createMapBacked()
 *														 					  .returningWhenLangTextMissing("--not configured--")
 *														 					  .addForLang(Language.SPANISH,"Spanish summary")
 *														 					  .addForLang(Language.ENGLISH,"English summary")
 *														 					  .build());
 *	// Language dependent full text summary backed by a HasLangDependentNamedFacet
 *	HasLangDependentNamedFacet hasLangDependentNamed = ...;
 *	LangDependentSummary summary2 = SummaryBuilder.languageDependent()
 *												  .createFullText(hasLangDependentNamed);
 *	// Language independent summary
 *	LangIndependentSummary summary3 = SummaryBuilder.languageInDependent()
 *													.create("A summary");
 *	HasLangInDependentNamedFacet hasLangInDependentNamed = ...;
 *	LangIndependentSummary summary4 = SummaryBuilder.languageInDependent()
 *													.createFullText(hasLangInDependentNamed);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class SummaryBuilder
           implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static SummaryBuilderForLangDependent languageDependent() {
		return new SummaryBuilderForLangDependent();
	}
	public static SummaryBuilderForLangInDependent languageInDependent() {
		return new SummaryBuilderForLangInDependent();
	}
	public static SummaryBuilderForLangNamed languageNamed() {
		return new SummaryBuilderForLangNamed();
	}
	@SuppressWarnings("serial")
	public static InmutableLangIndependentSummary wrapAsInmutable(final boolean isFullText,
																  final Summary summary) {
		return new InmutableLangIndependentSummary(isFullText) {	
						@Override
						public String asString() {
							return summary.asString();
						}
		};
	}
	public static InmutableLangIndependentSummary wrapAsInmutable(final Summary summary) {
		return SummaryBuilder.wrapAsInmutable(false,
											  summary);
	}
	public static InmutableLangIndependentSummary wrapAsInmutableForFullText(final Summary summary) {
		return SummaryBuilder.wrapAsInmutable(true,
											  summary);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public static class SummaryBuilderForLangDependent {
		@SuppressWarnings("static-method")
		public LangDependentSummary create(final LanguageTexts langTexts) {
			return SummaryLanguageTextsBacked.of(langTexts);	
		}
		@SuppressWarnings("static-method")
		public LangDependentSummary createFullText(final LanguageTexts langTexts) {
			return SummaryLanguageTextsBacked.fullTextOf(langTexts);
		}
		@SuppressWarnings("static-method")
		public LangDependentSummary create(final HasLangDependentNamedFacet hasName) {
			return SummaryHasLanguageDependentNameBacked.of(hasName);
		}
		@SuppressWarnings("static-method")
		public LangDependentSummary createFullText(final HasLangDependentNamedFacet hasName) {
			return SummaryHasLanguageDependentNameBacked.fullTextOf(hasName);
		}
		public LangDependentSummary createFullText(final HasSummaryFacet hasSummary) {
			return this.createFullText(hasSummary.asSummarizable()
												 .getSummary()
												 .asLangDependent()
												 .asLanguageTexts());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public static class SummaryBuilderForLangInDependent {
		@SuppressWarnings("static-method")
		public LangIndependentSummary create(final String summary) {
			return SummaryStringBacked.of(summary);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary createFullText(final String summary) {
			return SummaryStringBacked.fullTextOf(summary);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary create(final HasLangInDependentNamedFacet hasName) {
			return SummaryHasLanguageIndependentNameBacked.of(hasName);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary createFullText(final HasLangInDependentNamedFacet hasName) {
			return SummaryHasLanguageIndependentNameBacked.fullTextOf(hasName);
		}
		public LangIndependentSummary createFullText(final HasSummaryFacet hasSummary) {
			return this.createFullText(hasSummary.asSummarizable()
												 .getSummary()
												 .asLangIndependent()
												 .asString());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public static class SummaryBuilderForLangNamed {
		@SuppressWarnings("static-method")
		public LangIndependentSummary create(final String summary) {
			return SummaryStringBacked.of(summary);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary createFullText(final String summary) {
			return SummaryStringBacked.fullTextOf(summary);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary create(final HasLangNamedFacet hasName) {
			return SummaryHasLanguageNameBacked.of(hasName);
		}
		@SuppressWarnings("static-method")
		public LangIndependentSummary createFullText(final HasLangNamedFacet hasName) {
			return SummaryHasLanguageNameBacked.fullTextOf(hasName);
		}
		public LangIndependentSummary createFullText(final HasSummaryFacet hasSummary) {
			return this.createFullText(hasSummary.asSummarizable()
												 .getSummary()
												 .asLangIndependent()
												 .asString());
		}
	}
}
