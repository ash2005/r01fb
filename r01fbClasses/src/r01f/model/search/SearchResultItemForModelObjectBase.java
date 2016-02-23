package r01f.model.search;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.marshalling.annotations.XmlTypeDiscriminatorAttribute;
import r01f.marshalling.annotations.XmlWriteIgnoredIfEquals;
import r01f.model.IndexableModelObject;
import r01f.model.facets.Summarizable;
import r01f.model.search.SearchOIDs.SearchEngineDBID;
import r01f.model.search.SearchOIDs.SearchSourceID;
import r01f.types.summary.Summary;
import r01f.util.types.Strings;

@Accessors(prefix="_")
public abstract class SearchResultItemForModelObjectBase<O extends OID,M extends IndexableModelObject>
           implements SearchResultItemForModelObject<O,M>,
           			  Debuggable {
	
	private static final long serialVersionUID = 126535994364020659L;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT'S FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Oid
     */
	@XmlElement(name="oid") @XmlTypeDiscriminatorAttribute(name="type")
    @Getter @Setter private O _oid;
	/**
	 * Numeric Id
	 */
	@XmlElement(name="numericId") @XmlWriteIgnoredIfEquals(value="0")
	@Getter @Setter private long _numericId;
	/**
	 * A summary / abstract of the search result
	 * (it's NOT serialized)
	 */
	@XmlElement
	@Getter @Setter private transient Summary _summary;
	/**
	 * The model object type
	 */
	@XmlElement(name="modelObjectType")
	@Getter @Setter private Class<M> _modelObjectType;
	/**
	 * A type code for the model object type
	 */
	@XmlElement(name="modelObjectTypeCode")
	@Getter @Setter private long _modelObjectTypeCode;
	/**
	 * The found model object (not mandatory)
	 */
	@XmlElement
	@Getter @Setter private M _modelObject;
	/**
	 * Entity Version used to achieve the optimistic locking behavior
	 */
	@XmlElement(name="entityVersion")
	@Getter @Setter private long _entityVersion;
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH ENGINE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Search results source
     */
	@XmlAttribute(name="sourceOid")
    @Getter @Setter private SearchSourceID _sourceOid;
    /**
     * Search engine database 
     */
	@XmlAttribute(name="db")
    @Getter @Setter private SearchEngineDBID _dbOid = SearchEngineDBID.forId("default");
    /**
     * item number within results
     */
	@XmlAttribute(name="orderNumberWithinResults")
    @Getter @Setter private int _orderNumberWithinResults = -1;
    /**
     * Percentage/ranking of the item: the degree of confidence that the item verifies the searcher expectations
     */
	@XmlAttribute(name="score")
    @Getter @Setter private float _score = -1;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchResultItemForModelObjectBase() {
		// nothing
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HasSummary
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Summarizable asSummarizable() {
		return new Summarizable() {
						@Override
						public Summary getSummary() {
							return _summary;
						}
						@Override
						public void setSummary(final Summary summary) {
							_summary = summary;
						}
		};
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <U extends IndexableModelObject> void unsafeSetModelObject(final U modelObject) {
		_modelObject = (M)modelObject;
	}
	@Override @SuppressWarnings("unchecked")
	public <U extends IndexableModelObject> void unsafeSetModelObjectType(final Class<U> modelObjectType) {
		_modelObjectType = (Class<M>)modelObjectType;
	}
	@Override @SuppressWarnings("unchecked")
	public void unsafeSetOid(final OID oid) {
		_oid = (O)oid;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		return Strings.of("      oid: {}\n" + 
						  "numericId: {}\n" + 
						  "  summary: {}")
					  .customizeWith(this.getOid(),
							  		 this.getNumericId(),
							  		 this.getSummary())
					  .asString();
	}


}
