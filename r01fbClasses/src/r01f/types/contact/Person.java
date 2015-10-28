package r01f.types.contact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.marshalling.annotations.XmlTypeDiscriminatorAttribute;
import r01f.model.facets.FullTextSummarizable;
import r01f.model.facets.FullTextSummarizable.HasFullTextSummaryFacet;
import r01f.model.facets.Summarizable;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.model.facets.Summarizable.InmutableSummarizable;
import r01f.types.summary.Summary;
import r01f.types.summary.SummaryStringBacked;
import r01f.util.types.Strings;

/**
 * Person contact data
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="person")
@Accessors(prefix="_")
@NoArgsConstructor
public class Person 
  implements Serializable,
  			 HasSummaryFacet,
  			 HasFullTextSummaryFacet {

	private static final long serialVersionUID = 3678962348416518107L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Person id (ie dni number)
	 */
	@XmlElement(name="id") @XmlTypeDiscriminatorAttribute(name="type")
	@Getter @Setter private PersonID _id;
	/**
	 * Name
	 */
	@XmlElement(name="name") @XmlCDATA
	@Getter @Setter private String _name;
	/**
	 * Surname or first name
	 */
	@XmlElement(name="firstName") @XmlCDATA
	@Getter @Setter private String _surname1;
	/**
	 * Second name
	 */
	@XmlElement(name="secondName") @XmlCDATA
	@Getter @Setter private String _surname2;
	/**
	 * Mr, Ms, Doc, etc
	 */
	@XmlElement(name="salutation") @XmlCDATA
	@Getter @Setter private PersonSalutation _salutation;
	/**
	 * Details about the person
	 */
	@XmlElement(name="details") @XmlCDATA
	@Getter @Setter private String _details;
/////////////////////////////////////////////////////////////////////////////////////////
//  HasSummary
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Summarizable asSummarizable() {
		return new InmutableSummarizable(this.getClass()) {
						@Override
						public Summary getSummary() {
							return _composeSummary();
						}
			   };
	}
	@Override
	public FullTextSummarizable asFullTextSummarizable() {
		return new FullTextSummarizable() {
						@Override
						public Summary getFullTextSummary() {
							return _composeSummary();
						}
			   };
	}
	/**
	 * Returns the surname composing the surname1 and surname2
	 * @return
	 */
	public String getSurname() {
		String outSurname = null;
		if (Strings.isNOTNullOrEmpty(_surname1) && Strings.isNOTNullOrEmpty(_surname2)) {
			outSurname = Strings.customized("{} {}",_surname1,_surname2);						
		} 
		else if (Strings.isNOTNullOrEmpty(_surname1)) {
			outSurname = _surname1;
		} 
		else if (Strings.isNOTNullOrEmpty(_surname2)) {
			outSurname = _surname2;
		}
		return outSurname;
	}
	private Summary _composeSummary() {
		SummaryStringBacked outSummary = null;
		
		String surname = this.getSurname();
		
		if (Strings.isNOTNullOrEmpty(surname) && Strings.isNOTNullOrEmpty(_name)) {
			outSummary = SummaryStringBacked.of(Strings.customized("{}, {}",
																   surname,_name));
		} 
		else if (Strings.isNOTNullOrEmpty(surname)) {
			outSummary = SummaryStringBacked.of(surname);
		} 
		else if (Strings.isNOTNullOrEmpty(_name)) {
			outSummary = SummaryStringBacked.of(_name);
		} 
		else {
			outSummary = SummaryStringBacked.of(Strings.of("--NO summary for {}--")
												       .customizeWith(Person.class)
												       .asString());
		}
		return outSummary;
	}
}
