package r01f.model.metadata;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;
import r01f.types.Range;

@XmlRootElement(name="metaDataConfigForRangeField")
@Accessors(prefix="_")
public class FieldMetaDataForRange
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -8019257197049447852L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="rangeDataType")
	@Getter @Setter private Class<? extends Comparable<?>> _rangeDataType;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForRange() {
		super();
	}
	@SuppressWarnings("unchecked")
	public FieldMetaDataForRange(final FieldMetaDataForRange other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 (Class<? extends Comparable<?>>)other.getDataType());
	}
	public FieldMetaDataForRange(final FieldMetaDataID fieldId,
								 final LanguageTexts name,final LanguageTexts description,
								 final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								 final Class<? extends Comparable<?>> rangeDataType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Range.class);
		_rangeDataType = rangeDataType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Range;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Range.class.getSimpleName(),value.getClass().getSimpleName()));
		Range<?> rangeValue = (Range<?>)value;
		acceptable = ReflectionUtils.isSameClassAs(rangeValue.getDataType(),_rangeDataType);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {}<{}> FIELD (the provided value it's a {}<{}> type)",
																			   _fieldId,Range.class.getSimpleName(),_rangeDataType,value.getClass().getSimpleName(),rangeValue.getDataType())); 
	}
}
