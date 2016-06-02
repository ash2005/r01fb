package r01f.types.datetime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Dates;

@XmlRootElement(name="year")
@Accessors(prefix="_")
@NoArgsConstructor
public class Year 
  implements Serializable,
  			 CanBeRepresentedAsString {

	private static final long serialVersionUID = 7658275370612790932L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private int _year;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public Year(final int year) {
		_set(year);
	}
	public Year(final Integer year) {
		_set(year);
	}
	public Year(final String year) {
		_set(Integer.parseInt(year));
	}
	public static Year of(final String year) {
		return new Year(year);
	}
	public static Year of(final Date date) {
		return new Year(Dates.asCalendar(date).get(Calendar.YEAR));
	}
	public static Year of(final int year) {
		return new Year(year);
	}
	public static Year valueOf(final String year) {
		return new Year(year);
	}
	public static Year fromString(final String year) {
		return new Year(year);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private void _set(final int year) {
		Preconditions.checkArgument(year > 0,"Not a valid day year");
		_year = year;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return Long.toString(_year);
	}
	@Override
	public String asString() {
		return this.toString();
	}
	public int asInteger() {
		return _year;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBefore(final Year other) {
		return _year < other.asInteger();
	}
	public boolean isAfter(final Year other) {
		return _year > other.asInteger();
	}
	public boolean isBeforeOrEqual(final Year other) {
		return _year <= other.asInteger();
	}
	public boolean isAfterOrEqual(final Year other) {
		return _year >= other.asInteger();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof Year) return ((Year)obj).getYear() == _year;
		return false;
	}
	@Override
	public int hashCode() {
		return new Integer(_year).hashCode();
	}
}
