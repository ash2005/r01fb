package r01f.model.search.query;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.marshalling.annotations.XmlReadTransformer;
import r01f.marshalling.annotations.XmlWriteTransformer;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.QueryClauseXMLMarshallers.HasDataQueryXMLCustomMarshallers;

/**
 * Query clause that checks if there's any data on a field
 * Usage
 * <pre class='brush:java'>
 *		HasDataQueryClause hasData = HasDataQueryClause.forMetaData("myField");
 * </pre>
 * @param <T>
 */
@XmlRootElement(name="hasDataClause") @XmlReadTransformer(using=HasDataQueryXMLCustomMarshallers.class) @XmlWriteTransformer(using=HasDataQueryXMLCustomMarshallers.class)
@Accessors(prefix="_")
@NoArgsConstructor
public class HasDataQueryClause
     extends QueryClauseBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	HasDataQueryClause(final IndexableFieldID fieldId) {
		super(fieldId);
	}
	public static HasDataQueryClause forField(final IndexableFieldID fieldId) {
		return new HasDataQueryClause(fieldId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public QueryClauseType getClauseType() {
		return QueryClauseType.HAS_DATA;
	}	
	@Override
	public <V> V getValue() {
		throw new IllegalStateException(Throwables.message("{} clauses do not have values",HasDataQueryClause.class));
	}
	@Override
	public <V> Class<V> getValueType() {
		throw new IllegalStateException(Throwables.message("{} clauses do not have values",HasDataQueryClause.class));
	}
}
