package r01f.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.guids.OID;
import r01f.marshalling.annotations.OidField;
import r01f.marshalling.annotations.XmlWriteIgnoredIfEquals;
import r01f.model.builders.facets.TrackableBuilder;
import r01f.model.facets.DirtyStateTrackableModelObject;
import r01f.model.facets.DirtyStateTrackableModelObject.HasDirtyStateTrackableModelObjectFacet;
import r01f.model.facets.Facetables;
import r01f.model.facets.ModelObjectFacet;
import r01f.model.facets.TrackableModelObject;
import r01f.model.facets.delegates.DirtyStateTrackableModelObjectDelegate;
import r01f.model.facets.delegates.TrackableDelegate;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.types.annotations.CompositionRelated;

import com.google.common.annotations.GwtIncompatible;

@Accessors(prefix="_")
@NoArgsConstructor
public abstract class PersistableModelObjectBase<O extends OID,
												 SELF_TYPE extends PersistableModelObjectBase<O,SELF_TYPE>>
           implements PersistableModelObject<O>,					// can be persisted
           			  HasDirtyStateTrackableModelObjectFacet {		// Changes in state can be tracked

	private static final long serialVersionUID = 6546937946507238664L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Model object unique identifier
     */
	@XmlAttribute(name="oid") @OidField
    @Getter @Setter protected O _oid;
	/**
	 * Numeric unique id
	 */
	@XmlAttribute(name="numbericId") @XmlWriteIgnoredIfEquals(value="0")
	@Getter @Setter protected long _numericId;
	/**
	 * The field used to achieve the optimistic locking behavior at the persistence layer
	 * (see DBEntityBase)
	 * When persisting this object, optimistic locking is used (assumed that conflicts are unlike to happen)
	 * i.e. There are two web processes running in parallel, both processing the 
	 * 		stock of an store item
	 * 		... let's say that initially we have stock=100
	 * 			----------[100]----------
	 * 			|						|
	 * 		  Load                    Load
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                    Save
	 *          |---------[99]			|
	 *          		  [99]----------| <---WTF!! the stock should have been 98
	 *          									but it ends being 99: WRONG!!
	 * To prevent this situation a last update timestamp or an incrementing version is used
	 * Every time a process want to update an entity it MUST tell us what the version is so
	 * if a conflict occurs it could be detected:
	 * 
	 * 			----------[100]----------
	 * 			|	   (version=1)		|
	 * 			|						|
	 * 	      Load 				       Load 
	 * 	   (version=1) 		       (version=1) 
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                      |
	 *     (version=1)                  |
	 *          |---------[99]			|
	 *          	   (version=2)		|
	 *          			|		   Save
	 *          		CONFLICT!<--(Version=1)
	 *          
	 * As seen, to be able to detect conflicts:
	 * 		- A version number (a timestamp) MUST be stored with the record
	 * 		- The version number MUST be loaded alongside the record and stored at the processing client
	 * 		- The version number MUST be send alongside the record in any update operation 
	 * 		  so the received version could be compared with the provided one
	 */
	@XmlAttribute(name="entityVersion") @XmlWriteIgnoredIfEquals(value="0")
	@Getter @Setter protected long _entityVersion;
    /**
     * Create & update info
     */
	@CompositionRelated @ConvertToDirtyStateTrackable	// force this object trackable
	@XmlElement(name="trackingInfo")
    @Getter @Setter protected ModelObjectTracking _trackingInfo;
    
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	public SELF_TYPE withOid(final O oid) {
    	_oid = oid;
    	return (SELF_TYPE)this;
    }
    @SuppressWarnings("unchecked")
    public SELF_TYPE withNumericId(final long id) {
    	_numericId = id;
    	return (SELF_TYPE)this;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  HasOid
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override @SuppressWarnings("unchecked") 
	public void unsafeSetOid(final OID oid) {
		this.setOid((O)oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HasFacet
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <F extends ModelObjectFacet> F asFacet(final Class<F> facet) {
		return Facetables.asFacet(this,facet);
	}
	@Override
	public <F extends ModelObjectFacet> boolean hasFacet(final Class<F> facet) {
		return Facetables.hasFacet(this,facet);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  DirtyStateTrackable facet accessor
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public DirtyStateTrackableModelObject asDirtyStateTrackable() {
		return new DirtyStateTrackableModelObjectDelegate<SELF_TYPE>((SELF_TYPE)this);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TRACKABLE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public TrackableModelObject asTrackable() {
		return new TrackableDelegate<SELF_TYPE>((SELF_TYPE)this);
	}
	@SuppressWarnings("unchecked")
    public TrackableBuilder<SELF_TYPE,SELF_TYPE> builderForTrackable() {
    	return new TrackableBuilder<SELF_TYPE,SELF_TYPE>((SELF_TYPE)this,
    													 (SELF_TYPE)this);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	COMPOSE METADATA
/////////////////////////////////////////////////////////////////////////////////////////    
	@Override @GwtIncompatible("GWT does NOT suppports ModelObjectMetaData Building")
	public ModelObjectTypeMetaData getModelObjectMetaData() {
		 return ModelObjectTypeMetaDataBuilder.createFor(this.getClass());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////
//	/**
//	 * Copies the persistable common fields from one persistable object to another
//	 * BEWARE it does NOT copies the oid
//	 * @param src
//	 * @param dst
//	 */
//	public static <S extends PersistableModelObject<? extends OID>,
//				   D extends PersistableModelObject<? extends OID>> void copyCommonFieldsExceptOid(final S src,final D dst) {
//		// Copies the persistable common data
//		dst.setNumericId(src.getNumericId());
//		dst.setTrackingInfo(src.getTrackingInfo());
//		dst.setEntityVersion(src.getEntityVersion());
//	}
}
