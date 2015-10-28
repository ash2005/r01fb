package r01f.model.search.query;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.guids.OID;
import r01f.marshalling.annotations.XmlReadTransformer;
import r01f.marshalling.annotations.XmlWriteTransformer;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.QueryClauseXMLMarshallers.EqualsQueryXMLCustomMarshallers;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Dates;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;

/**
 * Usage:
 * <pre class='brush:java'>
 * 		EqualsQueryClause<AppCode> eq = EqualsQueryClause.forField("myField")
 * 														 .of(AppCode.forId("r01"));
 * </pre>
 *
 * @param <T>
 */
@XmlRootElement(name="equalsClause") @XmlReadTransformer(using=EqualsQueryXMLCustomMarshallers.class) @XmlWriteTransformer(using=EqualsQueryXMLCustomMarshallers.class)
@Accessors(prefix="_")
@NoArgsConstructor
public class EqualsQueryClause<T>
     extends QueryClauseBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private T _eqValue;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	EqualsQueryClause(final IndexableFieldID fieldId,
					  final T value) {
		super(fieldId);
		_eqValue = value;
	}
	public static EqualsQueryClauseBuilder forField(final IndexableFieldID fieldId) {
		return new EqualsQueryClauseBuilder(fieldId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public QueryClauseType getClauseType() {
		return QueryClauseType.EQUALS;
	}	
	@Override @SuppressWarnings("unchecked")
	public <V> V getValue() {
		return (V)_eqValue;
	}
	@Override @SuppressWarnings("unchecked")
	public <V> Class<V> getValueType() {
		return (Class<V>)_eqValue.getClass();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isNumberEquals() {
		return Numbers.isNumberType(_eqValue.getClass());
	}
	public boolean isTextEquals() {
		return _eqValue.getClass().equals(String.class);
	}
	public boolean isDateEquals() {
		return _eqValue.getClass().equals(Date.class);
	}
	public boolean isEnumEquals() {
		return ReflectionUtils.isImplementing(_eqValue.getClass(),Enum.class);
	}
	public boolean isEnumWithCodeEquals() {
		return ReflectionUtils.isImplementing(_eqValue.getClass(),EnumWithCode.class);
	}
	public boolean isOIDEquals() {
		return ReflectionUtils.isImplementing(_eqValue.getClass(),OID.class);
	}
	@SuppressWarnings("unchecked")
	public <N extends Number> N getValueAsNumber() {
		if (!this.isNumberEquals()) throw new IllegalStateException(Strings.customized("The {} type is NOT extending Number & Comparable",_eqValue.getClass()));
		if (_eqValue.getClass().equals(Integer.class)) {
			return (N)this.getValue();
		} else if (_eqValue.getClass().equals(Long.class)) {
			return (N)this.getValue();
		} else if (_eqValue.getClass().equals(Double.class)) {
			return (N)this.getValue();
		} else if (_eqValue.getClass().equals(Float.class)) {
			return (N)this.getValue();
		} else {
			throw new IllegalStateException(Strings.customized("The {} type is NOT extending Number & Comparable",_eqValue.getClass()));
		}
	}
	@SuppressWarnings("unchecked")
	public <N extends Number> Class<N> getNumberType() {
		if (!this.isNumberEquals()) throw new IllegalStateException(Strings.customized("The {} type is NOT extending Number & Comparable",_eqValue.getClass()));
		if (_eqValue.getClass().equals(Integer.class)) {
			return (Class<N>)Integer.class;
		} else if (_eqValue.getClass().equals(Long.class)) {
			return (Class<N>)Long.class;
		} else if (_eqValue.getClass().equals(Double.class)) {
			return (Class<N>)Double.class;
		} else if (_eqValue.getClass().equals(Float.class)) {
			return (Class<N>)Float.class;
		} else {
			throw new IllegalStateException(Strings.customized("The {} type is NOT extending Number & Comparable",_eqValue.getClass()));
		}
	}
	public long getDateMilis() {
		if (!this.isDateEquals()) throw new IllegalStateException(Strings.customized("The {} type is NOT extending java.util.Date",_eqValue.getClass()));
		return Dates.asMillis((Date)_eqValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class EqualsQueryClauseBuilder {
		private final IndexableFieldID _fieldId;
		
		public <T> EqualsQueryClause<T> of(final T value) {
			return new EqualsQueryClause<T>(_fieldId,
											value);	
		}
		@SuppressWarnings("unchecked")
		public <N extends Number> EqualsQueryClause<N> of(final N value) {
			if (value == null) throw new IllegalArgumentException("invalid number value");
			if (value instanceof Integer) {
				return (EqualsQueryClause<N>)new EqualsQueryClause<Integer>(_fieldId,
															 				value.intValue());
			} else if (value instanceof Long) {
				return (EqualsQueryClause<N>)new EqualsQueryClause<Long>(_fieldId,
															 			value.longValue());
			} else if (value instanceof Double) {
				return (EqualsQueryClause<N>)new EqualsQueryClause<Double>(_fieldId,
															 			   value.doubleValue());
			} else 	if (value instanceof Float) {
				return (EqualsQueryClause<N>)new EqualsQueryClause<Float>(_fieldId,
															 			  value.floatValue());
			} 
			throw new IllegalArgumentException(Strings.customized("{} does NOT support {} type",RangeQueryClause.class,value.getClass()));
		}
	}
}
