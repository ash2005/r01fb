package r01f.types.summary;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.summary.SummaryBases.LangIndependentSummaryBase;
import r01f.util.types.Strings;


/**
 * A simple {@link String} based {@link LangIndependentSummary}
 */
@XmlRootElement(name="langIndependentSummary")
@Accessors(prefix="_")
public class SummaryStringBacked
     extends LangIndependentSummaryBase {

	private static final long serialVersionUID = 6179099335861429978L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private String _summaryText;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public SummaryStringBacked() {
		super(false);		// not to be used as a full text summary by default
	}
	public SummaryStringBacked(final String str) {
		super(false);		// not to be used as a full text summary by default
		_summaryText = str;
	}
	public SummaryStringBacked(final String str,final Object... vars) {
		this(Strings.customized(str,vars));
	}
	public SummaryStringBacked(final boolean fullText,
							   final String str) {
		super(fullText);
		_summaryText = str;
	}
	public static SummaryStringBacked create() {
		return new SummaryStringBacked();
	}
	public static SummaryStringBacked of(final String str) {
		return new SummaryStringBacked(false,	// not to be used as a full text summary
									   str);
	}
	public static SummaryStringBacked of(final String str,final Object... params) {
		return new SummaryStringBacked(false,	// not to be used as a full text summary
									   Strings.of(str)
									   		  .customizeWith(params)
									   		  .asString());
	}
	public static SummaryStringBacked fullTextOf(final String str) {
		return new SummaryStringBacked(true,	// to be used as a full text summary
									   str);
	}
	public static SummaryStringBacked fullTextOf(final String str,final Object... params) {
		return new SummaryStringBacked(true,	// to be used as a full text summary
									   Strings.of(str)
									   		  .customizeWith(params)
									   		  .asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return _summaryText;
	}
	@Override
	public String asString() {
		return _summaryText;
	}
	@Override
	public void setSummary(final String summary) {
		_summaryText = summary;
	}
}
