package r01f.model.search;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.guids.OIDBaseMutable;
import r01f.types.annotations.Inmutable;

/**
 * Search oids
 */
public class SearchOIDs {
	/**
	 * Search engine data base identifier
	 */
	@Inmutable
	@XmlRootElement(name="searchEngineBDId")
	@NoArgsConstructor
	public static class SearchEngineDBID 
				extends OIDBaseMutable<String> { 	// normally this should extend OIDBaseInmutable BUT it MUST have a default no-args constructor to be serializable
		private static final long serialVersionUID = 5503685235211621466L;
		public SearchEngineDBID(final String oid) {
			super(oid);
		}
		public static SearchEngineDBID forId(final String id) {
			return new SearchEngineDBID(id);
		}
	}
	/**
	 * Search origin identifier
	 */
	@Inmutable
	@XmlRootElement(name="searchSourceId")
	@NoArgsConstructor
	public static class SearchSourceID 
				extends OIDBaseMutable<String> { 	// normally this should extend OIDBaseInmutable BUT it MUST have a default no-args constructor to be serializable
		private static final long serialVersionUID = -6291130534743233006L;
		public SearchSourceID(final String oid) {
			super(oid);
		}
		public static SearchSourceID forId(final String id) {
			return new SearchSourceID(id);
		}
	}
}
