package r01f.model.search.query;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.annotations.XmlReadTransformer;
import r01f.marshalling.annotations.XmlWriteTransformer;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.QueryClauseXMLMarshallers.ContainedInQueryXMLCustomMarshallers;

/**
 * Usage
 * <pre class='brush:java'>
 *		ContainedInQueryClause<Integer> spectrum = ContainedInQueryClause.<Integer>forMetaData("myField")
 *																	  	 .within(new Integer[] {2,3});
 * </pre>
 * @param <T>
 */
@XmlRootElement(name="containedInClause") @XmlReadTransformer(using=ContainedInQueryXMLCustomMarshallers.class) @XmlWriteTransformer(using=ContainedInQueryXMLCustomMarshallers.class)
@Accessors(prefix="_")
@NoArgsConstructor
public class ContainedInQueryClause<T>
     extends QueryClauseBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private T[] _spectrum;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	ContainedInQueryClause(final IndexableFieldID fieldId,
						   final T[] spectrum) {
		super(fieldId);
		_spectrum = spectrum;
	}	
	public static <T> ContainedInQueryClauseStep1Builder<T> forField(final IndexableFieldID fieldId) {
		return new ContainedInQueryClauseStep1Builder<T>(fieldId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public QueryClauseType getClauseType() {
		return QueryClauseType.CONTAINED_IN;
	}	
	@Override @SuppressWarnings("unchecked")
	public <V> V getValue() {
		return (V)_spectrum;
	}
	@Override @SuppressWarnings("unchecked")
	public <V> Class<V> getValueType() {
		return (Class<V>)_spectrum.getClass().getComponentType();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public void setSpectrumFrom(final Collection<T> spectrum) {
		_spectrum = (T[])spectrum.toArray();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ContainedInQueryClauseStep1Builder<T> {
		private final IndexableFieldID _fieldId;
		
		public ContainedInQueryClause<T> within(final T[] spectrum) {
			return new ContainedInQueryClause<T>(_fieldId,
											  	 spectrum);
		}
	} 
}
