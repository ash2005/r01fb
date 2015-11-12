package r01f.types.contact;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.patterns.IsBuilder;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class PersonWithContactInfoBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final PersonWithContactInfo _modelObj;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static PersonWithContactInfoBuilderPersonStep create() {
		return new PersonWithContactInfoBuilder(new PersonWithContactInfo())
						.new PersonWithContactInfoBuilderPersonStep();
	}
	public static PersonWithContactInfo create(final Person person,
											   final ContactInfo contactInfo) {
		return new PersonWithContactInfo(person,
										 contactInfo);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersonWithContactInfoBuilderPersonStep {
		public PersonWithContactInfoBuilderContactStep noPerson() {
			return new PersonWithContactInfoBuilderContactStep();
		}
		public PersonWithContactInfoBuilderContactStep forPerson(final Person person) {
			_modelObj.setPerson(person);
			return new PersonWithContactInfoBuilderContactStep();
		}
	}
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersonWithContactInfoBuilderContactStep {
		public PersonWithContactInfoBuilderBuildStep noContactInfo() {
			return new PersonWithContactInfoBuilderBuildStep();
		}
		public PersonWithContactInfoBuilderBuildStep withContactInfo(final ContactInfo contactInfo) {
			_modelObj.setContactInfo(contactInfo);
			return new PersonWithContactInfoBuilderBuildStep();
		}
	}
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersonWithContactInfoBuilderBuildStep {
		public PersonWithContactInfo build() {
			return _modelObj;
		}
	}
}
