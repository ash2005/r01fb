package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.guids.OIDBaseMutable;
import r01f.types.annotations.Inmutable;

/**
 * MetaData unique identifier
 */
@Inmutable
@XmlRootElement(name="fieldMetaDataId")
@NoArgsConstructor
public class FieldMetaDataID
	 extends OIDBaseMutable<String> {
	
	private static final long serialVersionUID = 3733734411099465018L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public FieldMetaDataID(final String oid) {
		super(oid);
	}
	public static FieldMetaDataID valueOf(final String s) {
		return FieldMetaDataID.forId(s);
	}
	public static FieldMetaDataID fromString(final String s) {
		return FieldMetaDataID.forId(s);
	}
	public static FieldMetaDataID forId(final String id) {
		return new FieldMetaDataID(id);
	}
	public static FieldMetaDataID forFieldId(final IndexableFieldID fieldId) {
		return FieldMetaDataID.forId(fieldId.asString());
	}
	public IndexableFieldID asFieldId() {
		return IndexableFieldID.forId(this.asString());
	}
}
