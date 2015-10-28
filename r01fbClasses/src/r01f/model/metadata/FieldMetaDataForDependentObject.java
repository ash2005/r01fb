package r01f.model.metadata;

import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

import com.google.common.collect.Sets;


@XmlRootElement(name="metaDataConfigForDependentObjectField")
@Accessors(prefix="_")
public class FieldMetaDataForDependentObject 
	 extends FieldMetaDataConfigBase 
  implements HasFieldMetaDataConfig {

	private static final long serialVersionUID = 1281643971770301481L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElementWrapper(name="childMetaData")
	@Getter @Setter private Set<FieldMetaData> _childMetaData;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForDependentObject() {
		super();
	}
	public FieldMetaDataForDependentObject(final FieldMetaDataForDependentObject other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 other.getDataType(),
			 other.getChildMetaData());
	}
	public FieldMetaDataForDependentObject(final FieldMetaDataID fieldId,
								   		   final LanguageTexts name,final LanguageTexts description,
								   		   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								   		   final Class<?> type,
								   		   final Set<FieldMetaData> childMetaData) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  type);
		_childMetaData = childMetaData;
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a new child metadata
	 * @param metaData
	 * @return
	 */
	public FieldMetaDataForDependentObject addChildMetaData(final FieldMetaData metaData) {
		if (_childMetaData == null) _childMetaData = Sets.newHashSet();
		_childMetaData.add(metaData);
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Iterator<FieldMetaData> metaDataConfigIterator() {
		return _childMetaData != null ? _childMetaData.iterator()
									  : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public IndexableFieldID getIndexableFieldId() {
		throw new IllegalStateException(Throwables.message("{} is a compound metaData",FieldMetaDataForDependentObject.class));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		throw new IllegalArgumentException(Throwables.message("{} is a compound metaData",FieldMetaDataForDependentObject.class));
	}
}
