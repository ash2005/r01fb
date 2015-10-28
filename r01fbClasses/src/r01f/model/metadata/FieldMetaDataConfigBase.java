package r01f.model.metadata;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.LanguageTexts;
import r01f.marshalling.annotations.OidField;
import r01f.marshalling.annotations.XmlCDATA;

@Accessors(prefix="_")
abstract class FieldMetaDataConfigBase 
    implements FieldMetaData {

	private static final long serialVersionUID = 1281643971770301481L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Identifier
     */
    @XmlAttribute(name="oid") @OidField
    @Getter @Setter protected FieldMetaDataID _fieldId;
	/**
     * Name by language
     */
    @XmlElementWrapper(name="name") @XmlCDATA
    @Getter @Setter protected LanguageTexts _name;
    /**
     * Description by language
     */
    @XmlElementWrapper(name="description") @XmlCDATA
    @Getter @Setter protected LanguageTexts	 _description;
    /**
     * Search engine indexing config
     */
    @XmlElement(name="searchEngineIndexingConfig")
    @Getter @Setter protected FieldMetaDataSearchEngineIndexingConfig _searchEngineIndexingConfig;
	/**
	 * The field data type
	 */
	@XmlAttribute(name="dataType")
	@Getter @Setter protected Class<?> _dataType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataConfigBase() {
		super();
	}
	public FieldMetaDataConfigBase(final FieldMetaDataID metaDataId,
							 final LanguageTexts name,final LanguageTexts description,
							 final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
							 final Class<?> dataType) {
		_fieldId = metaDataId;
		_name = name;
		_description = description;	
		_searchEngineIndexingConfig = searchEngineIndexingConfig;
		_dataType = dataType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public IndexableFieldID getIndexableFieldId() {
		return IndexableFieldID.forId(_fieldId.asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override public boolean isStringField()				{ return this instanceof FieldMetaDataForString; 	}
	@Override public boolean isDateField()                  { return this instanceof FieldMetaDataForDate; }
	@Override public boolean isBooleanField()               { return this instanceof FieldMetaDataForBoolean; }
	@Override public boolean isNumberField()				{ return this.isIntegerField() || this.isLongField() || this.isFloatField(); }
	@Override public boolean isIntegerField()               { return this instanceof FieldMetaDataForInteger; }
	@Override public boolean isLongField()                  { return this instanceof FieldMetaDataForLong; }
	@Override public boolean isFloatField()                 { return this instanceof FieldMetaDataForFloat; }
	@Override public boolean isDoubleField()                { return this instanceof FieldMetaDataForDouble; }
	@Override public boolean isOIDField()                   { return this instanceof FieldMetaDataForOID;   	}
	@Override public boolean isPathField()                  { return this instanceof FieldMetaDataForPath; }
	@Override public boolean isUrlField()                   { return this instanceof FieldMetaDataForUrl; }
	@Override public boolean isJavaTypeField() 				{ return this instanceof FieldMetaDataForJavaType; }
	@Override public boolean isLanguageField()              { return this instanceof FieldMetaDataForLanguage; }
	@Override public boolean isEnumField()                  { return this instanceof FieldMetaDataForEnum; }
	@Override public boolean isLanguageTextsField()			{ return this instanceof FieldMetaDataForLanguageTexts; }
	@Override public boolean isSummaryField()               { return this instanceof FieldMetaDataForSummary; }
	@Override public boolean isCollectionField()            { return this instanceof FieldMetaDataForCollection || this instanceof FieldMetaDataForSummary; }
	@Override public boolean isMapField()                   { return this instanceof FieldMetaDataForMap; }
	@Override public boolean isPolymorphicField()			{ return this instanceof FieldMetaDataForPolymorphicType; }
	@Override public boolean isDependentObjectField()       { return this instanceof FieldMetaDataForDependentObject; }
	
	@Override 
	public boolean hasMultipleDimensions() {
		boolean outIsMultiDimensional = this.isMapField()
										||
										this.isLanguageDependent();
		return outIsMultiDimensional;
	}
	@Override
	public boolean isLanguageDependent() {
		boolean outIsLanguageDependent = false;
		if (this.isLanguageTextsField()) {
			outIsLanguageDependent = true;
		}
		else if (this.isSummaryField()) {
			FieldMetaDataForSummary summaryField = (FieldMetaDataForSummary)this;
			outIsLanguageDependent = summaryField.isForLangDependentSummary();
		}
		return outIsLanguageDependent;
	}
	
	@Override
	public boolean hasMultipleValues() {
		return this.isCollectionField();
	}
}
