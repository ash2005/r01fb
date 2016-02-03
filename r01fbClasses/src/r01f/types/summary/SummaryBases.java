package r01f.types.summary;

import java.io.Reader;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.util.types.Strings;

import com.google.common.annotations.GwtIncompatible;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class SummaryBases {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	static abstract class SummaryBase
	           implements Summary {

		private static final long serialVersionUID = -8822340374297098830L;
		/**
		 * Tells if this summary is going to be indexed 
		 */
		@XmlTransient	// do not serialize, do not track changes
		private final transient boolean _fullTextSummary;
		
		@Override
		public boolean isLangDependent() {
			return this instanceof LangDependentSummary;
		}
		@Override
		public boolean isLangIndependent() {
			return this instanceof LangIndependentSummary;
		}
		@Override
		public boolean isFullTextSummary() {
			return _fullTextSummary;
		}
		@Override
		public LangDependentSummary asLangDependent() {
			return (LangDependentSummary)this;
		}
		@Override
		public LangIndependentSummary asLangIndependent() {
			return (LangIndependentSummary)this;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static abstract class LangDependentSummaryBase
			      extends SummaryBase 
               implements LangDependentSummary {

		private static final long serialVersionUID = 2427249816412571251L;
		
		public LangDependentSummaryBase(final boolean fullText) {
			super(fullText);
		}
		@Override
		public String asString(final Language lang) {
			return this.asLanguageTexts() != null ? this.asLanguageTexts().get(lang)
												  : null;
		}
		@Override @GwtIncompatible("GWT does not supports IO")
		public Reader asReader(final Language lang) {
			return Strings.converterOf(this.asString(lang))
						  .asReader();
		}
		@Override
		public boolean isEmpty(final Language lang) {
			return Strings.isNullOrEmpty(this.asString(lang));
		}
		@Override
		public boolean hasData(final Language lang) {
			return Strings.isNOTNullOrEmpty(this.asString(lang));
		}
		@Override
		public Set<Language> getAvailableLanguages() {
			LanguageTexts langTexts = this.asLanguageTexts();
			return langTexts != null ? langTexts.getDefinedLanguages()
									 : null;
		}
	}
	public static abstract class MutableLangDependentSummary 
						 extends LangDependentSummaryBase {
		private static final long serialVersionUID = 5061045724104787443L;
		
		protected MutableLangDependentSummary(final boolean fullText) {
			super(fullText);
		}
	}
	public static abstract class InmutableLangDependentSummaryBase 
					     extends MutableLangDependentSummary {

		private static final long serialVersionUID = 2932174854238079765L;
		protected InmutableLangDependentSummaryBase(final boolean fullText) {
			super(fullText);
		}
		@Override
		public void setSummary(final Language lang,final String summary) {
			throw new UnsupportedOperationException("Summary cannot be set");
		}
		@Override
		public String asString() {
			return this.asString(Language.DEFAULT);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static abstract class LangIndependentSummaryBase 
			      extends SummaryBase 
               implements LangIndependentSummary {
		
		private static final long serialVersionUID = 968762068851924097L;

		public LangIndependentSummaryBase(final boolean fullText) {
			super(fullText);
		}
		@Override @GwtIncompatible("GWT does not supports IO")
		public Reader asReader() {
			return Strings.converterOf(this.asString())
						  .asReader();
		}
		@Override
		public boolean isEmpty() {
			return Strings.isNullOrEmpty(this.asString());
		}
		@Override
		public boolean hasData() {
			return Strings.isNOTNullOrEmpty(this.asString());
		}
	}
	public static abstract class MutableLangIndependentSummary 
				         extends LangIndependentSummaryBase {
	
		private static final long serialVersionUID = 1341093279101233181L;

		protected MutableLangIndependentSummary(final boolean fullText) {
			super(fullText);
		}
	}
	public static abstract class InmutableLangIndependentSummary 
				         extends MutableLangIndependentSummary {

		private static final long serialVersionUID = 4750780736595116663L;
		
		protected InmutableLangIndependentSummary(final boolean fullText) {
			super(fullText);
		}
		@Override
		public void setSummary(final String summary) {
			throw new UnsupportedOperationException("Summary cannot be set");
		}
	}
}
