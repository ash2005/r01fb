package r01f.types.summary;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsMapBacked;
import r01f.types.summary.SummaryBases.LangDependentSummaryBase;

/**
 * Summary in multiple languages for an object 
 */
@XmlRootElement(name="langDependentSummary")
@Accessors(prefix="_")
public class SummaryLanguageTextsBacked 
     extends LangDependentSummaryBase {

	private static final long serialVersionUID = 4848087173773787358L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Summary in each language
	 */
	@XmlValue
	@Getter @Setter private LanguageTexts _summaries;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public SummaryLanguageTextsBacked() {
		super(false);	
	}
	public SummaryLanguageTextsBacked(final boolean fullText,
									  final LanguageTexts langTexts) {
		super(fullText);
		_summaries = langTexts;
	}
	public static SummaryLanguageTextsBacked create() {
		SummaryLanguageTextsBacked outSummary = new SummaryLanguageTextsBacked(false,	// not to be used as a full text summary
																			   LanguageTextsMapBacked.create(LangTextNotFoundBehabior.THROW_EXCEPTION,null));
		return outSummary;
	}
	public static SummaryLanguageTextsBacked of(final LanguageTexts langTexts) {
		SummaryLanguageTextsBacked outSummary = new SummaryLanguageTextsBacked(false,	// not to be used as a full text summary
																			   langTexts);
		return outSummary;
	}
	public static SummaryLanguageTextsBacked fullTextOf(final LanguageTexts langTexts) {
		SummaryLanguageTextsBacked outSummary = new SummaryLanguageTextsBacked(true,	//to be used as a full text summary
																			   langTexts);
		return outSummary;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString(final Language lang) {
		return _summaries != null ? _summaries.get(lang)
								  : null;
	}
	@Override
	public String asString() {
		return this.asString(Language.DEFAULT);
	}
	@Override
	public void setSummary(final Language lang,final String summary) {
		if (_summaries != null) _summaries.add(lang,summary);
	}
	public SummaryLanguageTextsBacked addForLang(final Language lang,final String summary) {
		this.setSummary(lang,summary);
		return this;
	}
	@Override
	public LanguageTexts asLanguageTexts() {
		return _summaries;
	}
}
