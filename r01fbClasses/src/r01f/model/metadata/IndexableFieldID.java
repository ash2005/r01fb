package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.guids.OIDBaseMutable;
import r01f.types.annotations.Inmutable;

/**
 * Indexable field unique identifier
 */
@Inmutable
@XmlRootElement(name="indexableFieldId")
@NoArgsConstructor
public class IndexableFieldID
	 extends OIDBaseMutable<String> {

	private static final long serialVersionUID = 8049806262539341650L;

	public IndexableFieldID(final String oid) {
		super(oid);
	}
	public static IndexableFieldID valueOf(final String s) {
		return IndexableFieldID.forId(s);
	}
	public static IndexableFieldID fromString(final String s) {
		return IndexableFieldID.forId(s);
	}
	public static IndexableFieldID forId(final String id) {
		return new IndexableFieldID(id);
	}
	public static IndexableFieldID fromMetaDataId(final FieldMetaDataID id) {
		return new IndexableFieldID(id.asString());
	}
	public FieldMetaDataID asMetaDataId() {
		return FieldMetaDataID.forFieldId(this);
	}
}
