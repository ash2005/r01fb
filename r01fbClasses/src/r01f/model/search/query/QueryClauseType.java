package r01f.model.search.query;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;

@XmlRootElement(name="queryClauseType")
public enum QueryClauseType 
 implements EnumExtended<QueryClauseType> {
	BOOLEAN,		// join of two or more clauses
	EQUALS,			// a = b
	CONTAINED_IN,	// a,b,c...
	CONTAINS_TEXT,	// text*
	RANGE,			// (..10) / (1..10) (10..)
	HAS_DATA;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static EnumExtendedWrapper<QueryClauseType> WRAPPER = EnumExtendedWrapper.create(QueryClauseType.class);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static QueryClauseType fromName(final String name) {
		return WRAPPER.fromName(name);
	}
	@Override
	public boolean isIn(final QueryClauseType... els) {
		return WRAPPER.isIn(this,els);
	}
	@Override
	public boolean is(final QueryClauseType el) {
		return WRAPPER.is(this,el);
	}
	
}
