package r01f.model.metadata;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Search engine indexing config used when defined a {@link FieldMetaData} config
 * This config is later translated into a IndexDocumentFieldConfig when defining the search-engine stored document
 */
@XmlRootElement(name="metaDataSearchEngineIndexingConfig")
@Accessors(prefix="_")
public class FieldMetaDataSearchEngineIndexingConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If the field's value is true the value is stored "as-is"
	 * This field's value could be used to be shown in the search results
	 */
	@XmlAttribute(name="store")
	@Getter @Setter private boolean _stored;
	/**
	 * Boosting value
	 */
	@XmlAttribute(name="boost")
	@Getter @Setter private float _boost = 1.0f;
	/**
	 * Is the field value indexed?
	 */
	@XmlElement(name="indexed")
	@Getter @Setter private boolean _indexed = true;
	/**
	 * Is the field indexed value tokenized?
	 */
	@XmlElement(name="tokenized")
	@Getter @Setter private boolean _tokenized;
}
