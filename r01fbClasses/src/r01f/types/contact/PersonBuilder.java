package r01f.types.contact;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.locale.Language;
import r01f.patterns.IsBuilder;

/**
 * Builder for {@link Person} objects
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class PersonBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  ID
/////////////////////////////////////////////////////////////////////////////////////////
	public static PersonBuilderNameStep createPersonWithId(final PersonID id) {
		Person person = new Person();
		person.setId(id);
		return new PersonBuilder() {/* nothing */}
						.new PersonBuilderNameStep(person) {/* nothing */};
	}
	public static PersonBuilderNameStep createPersonWithoutId() {
		Person person = new Person();
		return new PersonBuilder() {/* nothing */}
						.new PersonBuilderNameStep(person) {/* nothing */};
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  NAME, SURNAME & SALUTATION
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderNameStep {
		private final Person _person;
		
		public PersonBuilderSurnamesStep withName(final String name) {
			_person.setName(name);
			return new PersonBuilderSurnamesStep(_person) {/* nothing */};
		}
		public PersonBuilderSurnamesStep noName() {
			return new PersonBuilderSurnamesStep(_person) {/* nothing */};
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderSurnamesStep {
		private final Person _person;
		
		public PersonBuilderSalutationStep withSurname(final String surname1) {
			_person.setSurname1(surname1);
			return new PersonBuilderSalutationStep(_person) {/* nothing */};
		}
		public PersonBuilderSalutationStep withSurnames(final String surname1,final String surname2) {
			_person.setSurname1(surname1);
			_person.setSurname2(surname2);
			return new PersonBuilderSalutationStep(_person) {/* nothing */};
		}
		public PersonBuilderSalutationStep noSurnames() {
			return new PersonBuilderSalutationStep(_person) {/* nothing */};
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderSalutationStep {
		private final Person _person;
		
		public PersonBuilderPrefLangStep useSalutation(final PersonSalutation salutation) {
			_person.setSalutation(salutation);
			return new PersonBuilderPrefLangStep(_person) {/* nothing */};
		}
		public PersonBuilderPrefLangStep noSalutation() {
			return new PersonBuilderPrefLangStep(_person) {/* nothing */};
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderPrefLangStep {
		private final Person _person;
		
		public PersonBuilderDetailsStep preferredLanguage(final Language lang) {
			_person.setPreferredLang(lang);
			return new PersonBuilderDetailsStep(_person) {/* nothing */};
		}
		public PersonBuilderDetailsStep noPreferredLanguage() {
			return new PersonBuilderDetailsStep(_person) {/* nothing */};
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderDetailsStep {
		private final Person _person;
		
		public PersonBuilderBuildStep withDetails(final String details) {
			_person.setDetails(details);
			return new PersonBuilderBuildStep(_person) {/* nothing */ };
		}
		public PersonBuilderBuildStep noDetails() {
			return new PersonBuilderBuildStep(_person) {/* nothing */ };
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public abstract class PersonBuilderBuildStep {
		private final Person _person;
		
		public Person build() {
			return _person;
		}
	}
}
