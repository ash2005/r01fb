package r01f.types.datetime;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;

@XmlRootElement(name="monthOfYear")
@Accessors(prefix="_")
public class MonthOfYear 
  implements Serializable,
  			 CanBeRepresentedAsString {

	private static final long serialVersionUID = 7658275370612790932L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private int _monthOfYear;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public MonthOfYear(final int month) {
		this._set(month);
	}
	public MonthOfYear(final String month) { 
		int m = Integer.parseInt(month);
		this._set(m);
	}
	public static MonthOfYear of(final String monthOfYear) {
		return new MonthOfYear(monthOfYear);
	}
	public static MonthOfYear of(final int monthOfYear) {
		return new MonthOfYear(monthOfYear);
	}
	public static MonthOfYear valueOf(final String monthOfYear) {
		return new MonthOfYear(monthOfYear);
	}
	public static MonthOfYear fromString(final String monthOfYear) {
		return new MonthOfYear(monthOfYear);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private void _set(final int month) {
		Preconditions.checkArgument(month <= 12 || month > 0,"Not a valid month");
		_monthOfYear = month;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return Long.toString(_monthOfYear);
	}
	@Override
	public String asString() {
		return this.toString();
	}
	public int asInteger() {
		return _monthOfYear;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBefore(final MonthOfYear other) {
		return _monthOfYear < other.asInteger();
	}
	public boolean isAfter(final MonthOfYear other) {
		return _monthOfYear > other.asInteger();
	}
	public boolean isBeforeOrEqual(final MonthOfYear other) {
		return _monthOfYear <= other.asInteger();
	}
	public boolean isAfterOrEqual(final MonthOfYear other) {
		return _monthOfYear >= other.asInteger();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof MonthOfYear) return ((MonthOfYear)obj).getMonthOfYear() == _monthOfYear;
		return false;
	}
	@Override
	public int hashCode() {
		return new Integer(_monthOfYear).hashCode();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final MonthOfYear JANUARY = new MonthOfYear(1);
	public static final MonthOfYear FEBRUARY = new MonthOfYear(2);
	public static final MonthOfYear MARCH = new MonthOfYear(3);
	public static final MonthOfYear APRIL = new MonthOfYear(4);
	public static final MonthOfYear MAY = new MonthOfYear(5);
	public static final MonthOfYear JUNE = new MonthOfYear(6);
	public static final MonthOfYear JULY = new MonthOfYear(7);
	public static final MonthOfYear AUGUST = new MonthOfYear(8);
	public static final MonthOfYear SEPTEMBER = new MonthOfYear(9);
	public static final MonthOfYear OCTOBER = new MonthOfYear(10);
	public static final MonthOfYear NOVEMBER = new MonthOfYear(11);
	public static final MonthOfYear DECEMBER = new MonthOfYear(12);
}
