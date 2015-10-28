package r01f.model.search.query;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.metadata.IndexableFieldID;

/**
 * Base type for query clauses
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public abstract class QueryClauseBase 
           implements QueryClause {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="onField")
    @Getter @Setter private IndexableFieldID _fieldId;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <Q extends QueryClause> Q cast() {
		return (Q)this;
	}
}
