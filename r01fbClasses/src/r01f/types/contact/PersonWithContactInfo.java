package r01f.types.contact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.model.facets.Summarizable;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.model.facets.Summarizable.InmutableSummarizable;
import r01f.types.summary.Summary;

@ConvertToDirtyStateTrackable
@XmlRootElement(name="personWithContact")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class PersonWithContactInfo
  implements Serializable,
		     HasSummaryFacet {
	
	private static final long serialVersionUID = 1530908840360246971L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Person data: name, surname, etc
	 */
	@XmlElement(name="person")
	@Getter @Setter private Person _person;
	/**
	 * Contact Info
	 */
	@XmlElement(name="contactInfo")
	@Getter @Setter private ContactInfo _contactInfo;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHOS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Summarizable asSummarizable() {
		return new InmutableSummarizable(this.getClass()) {
						@Override
						public Summary getSummary() {
							// delegate to person's summary
							return _person != null ? _person.asSummarizable()
															.getSummary()
												   : null;
						}
			   };
	}
}
