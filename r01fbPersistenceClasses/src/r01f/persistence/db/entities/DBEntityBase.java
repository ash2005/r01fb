package r01f.persistence.db.entities;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.locale.Language;
import r01f.model.ModelObjectTracking;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasName;
import r01f.model.facets.LangDependentNamed;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;
import r01f.model.facets.LangInDependentNamed;
import r01f.model.facets.LangInDependentNamed.HasLangInDependentNamedFacet;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.persistence.db.DBEntity;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;
import r01f.util.types.Dates;
import r01f.util.types.Strings;


/**
 * An entity
 * @param <R>
 */
@MappedSuperclass
@OptimisticLocking(type=OptimisticLockingType.VERSION_COLUMN,
				   cascade=false)	
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityBase 
		   implements DBEntity {

	private static final long serialVersionUID = -2168679594935612325L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create date
	 */	
	@Column(name="CREATED_AT",
			insertable=true,updatable=false) @Temporal(TemporalType.TIMESTAMP) 
	@Getter @Setter protected Calendar _createDate; 			// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * Last update date
	 */
	@Column(name="LASTMODIFIED_AT",
			insertable=false,updatable=true) @Temporal(TemporalType.TIMESTAMP) 
	@Getter @Setter protected Calendar _lastUpdateDate;		// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * The user code / application code of the creator 
	 */
	@Column(name="CREATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creator;
	/**
	 * The user code / application code of the last updator
	 */
	@Column(name="UPDATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdator;
	/**
	 * Entity Version to prevent conflicts
	 * See:
	 * 		http://www.eclipse.org/eclipselink/documentation/2.5/concepts/descriptors002.htm#CHEEEIEA
	 * 		http://en.wikibooks.org/wiki/Java_Persistence/Locking
	 * 
	 * Optimistic locking is used (assumed that conflicts are unlike to happen)
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
	 * IMPORTANT!!
	 * 		Using ECLIPSELINK, set @OptimisticLocking(type=OptimisticLockingType.VERSION_COLUMN,
	 *			   									  cascade=true)
	 *	    at the entity type
	 */  
	@Column(name="ENTITY_VERSION") @Version	
	@Getter @Setter protected long _entityVersion;
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  AUTO-UPDATE CREATE_DATE AND LAST_UPDATE_DATE
/////////////////////////////////////////////////////////////////////////////////////////
	@PrePersist
	void createdAt() {
		_createDate = _lastUpdateDate = Calendar.getInstance();
		_preCreate();
	}
	@PreUpdate
	void updatedAt() {
		_lastUpdateDate = Calendar.getInstance();
		_preUpdate();
	}
	protected abstract void _preCreate();
	protected abstract void _preUpdate();

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Date getCreateTimeStamp() {
		return Dates.fromCalendar(_createDate);
	}
	@Override
	public void setCreateTimeStamp(final Date newTS) {
		throw new UnsupportedOperationException("Do not call setCreateTimeStamp at the persistence layer!");
	}
	@Override
	public Date getLastUpdateTimeStamp() {
		return Dates.fromCalendar(_lastUpdateDate);
	}
	@Override
	public void setLastUpdateTimeStamp(final Date newTS) {
		throw new UnsupportedOperationException("Do not call setLastUpdateTimeStamp at the persistence layer!");
	}
	@Override 
	public UserCode getCreatorUserCode() {
		return Strings.isNOTNullOrEmpty(_creator) ? UserCode.forId(_creator) : null;
	}
	@Override
	public void setCreatorUserCode(final UserCode creator) {
		if (creator != null) _creator = creator.asString();
	}
	@Override 
	public UserCode getLastUpdatorUserCode() {
		return Strings.isNOTNullOrEmpty(_lastUpdator) ? UserCode.forId(_lastUpdator) : null;
	}
	@Override
	public void setLastUpdatorUserCode(final UserCode lastUpdator) {
		if (lastUpdator != null) _lastUpdator = lastUpdator.asString();
	}
	@Override
	public void setTrackingInfo(final ModelObjectTracking trackingInfo) {
		this.setCreatorUserCode(trackingInfo.getCreatorUserCode());
		this.setLastUpdatorUserCode(trackingInfo.getLastUpdatorUserCode());
	}
	@Override
	public ModelObjectTracking getTrackingInfo() {
		ModelObjectTracking outTrck = new ModelObjectTracking();
		if (_createDate != null) outTrck.setCreateDate(_createDate.getTime());
		if (_lastUpdateDate != null) outTrck.setLastUpdateDate(_lastUpdateDate.getTime());
		if (_creator != null) outTrck.setCreatorUserCode(UserCode.forId(_creator));
		if (_lastUpdator != null) outTrck.setLastUpdatorUserCode(UserCode.forId(_lastUpdator));
		return outTrck;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tries to get a name from the object
	 * @param modelObject
	 * @return
	 */
	protected static <R extends PersistableModelObject<? extends OID>> String _persistableModelObjectInternalName(final R modelObject) {
		String outName = null;
		
		// [1] try to get the name from the summary
		if (modelObject.hasFacet(HasSummaryFacet.class)) {
			Summary summary = modelObject.asFacet(HasSummaryFacet.class)
											.asSummarizable()
												.getSummary();
			if (summary != null) {
				if (summary.isLangDependent()) {
					LangDependentSummary langDepSum = summary.asLangDependent();
					StringBuilder sb = new StringBuilder();
					for (Iterator<Language> langIt = langDepSum.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
						Language lang = langIt.next();
						sb.append("[" + lang + "]: " + langDepSum.asString(lang));
						if (langIt.hasNext()) sb.append(", ");
					}
					outName = sb.toString();
				} else {
					LangIndependentSummary langIndepSum = summary.asLangIndependent();
					outName = langIndepSum.asString();
				}
			}
		}
		// [2]... if no name was retrieved, try to use the model object's name
		if (Strings.isNullOrEmpty(outName) && modelObject.hasFacet(HasName.class)) {
			if (modelObject.hasFacet(HasLangDependentNamedFacet.class)) {
				LangDependentNamed langNames = modelObject.asFacet(HasLangDependentNamedFacet.class)
												   		  .asLangDependentNamed();
				StringBuilder sb = new StringBuilder();
				for (Iterator<Language> langIt = langNames.getAvailableLanguages().iterator(); langIt.hasNext(); ) {
					Language lang = langIt.next();
					sb.append("[" + lang + "]: " + langNames.getNameIn(lang));
					if (langIt.hasNext()) sb.append(", ");
				}
				outName = sb.toString();
			} else {
				LangInDependentNamed name = modelObject.asFacet(HasLangInDependentNamedFacet.class)
													   .asLangInDependentNamed();
				outName = name.getName();
			}
		} 
		// There's nowhere to get a summary
		if (Strings.isNullOrEmpty(outName)) outName = "Could NOT get the object's name neither from the summary or the name";

		return outName;
	}
}
