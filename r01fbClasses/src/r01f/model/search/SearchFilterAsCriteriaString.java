package r01f.model.search;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.StringEncodeUtils;
import r01f.util.types.Strings;

@XmlRootElement(name="searchFilterCriteriaString")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class SearchFilterAsCriteriaString
  implements CanBeRepresentedAsString,
  			 Serializable {
	
	private static final long serialVersionUID = 3196015344923111354L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@XmlValue
	@Getter @Setter private String _filter;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR  & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static SearchFilterAsCriteriaString of(final String criteriaStr) {
		return new SearchFilterAsCriteriaString(criteriaStr);
	}
	public static SearchFilterAsCriteriaString of(final SearchFilter filter) {
		return filter.toCriteriaString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if the filter contains data
	 */
	public boolean hasData() {
		return Strings.isNOTNullOrEmpty(_filter);
	}
	@Override
	public String asString() {
		return _filter;
	}
	/**
	 * @return the filter url encoded
	 */
	public String asStringUrlEncoded() {
		return StringEncodeUtils.urlEncodeNoThrow(_filter)
								.toString();
	}
	@Override
	public String toString() {
		return this.asString();
	}
}
