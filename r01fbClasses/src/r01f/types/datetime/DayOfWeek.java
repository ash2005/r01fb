package r01f.types.datetime;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;

@XmlRootElement(name="dayOfWeek")
@Accessors(prefix="_")
public class DayOfWeek 
  implements Serializable,
  			 CanBeRepresentedAsString {

	private static final long serialVersionUID = 7658275370612790932L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter private int _dayOfWeek;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public DayOfWeek(final int dayOfWeek) {
		_set(dayOfWeek);
	}
	public DayOfWeek(final Integer dayOfWeek) {
		_set(dayOfWeek);
	}
	public DayOfWeek(final String month) { 
		int m = Integer.parseInt(month);
		_set(m);
	}
	public static DayOfWeek of(final String dayOfWeek) {
		return new DayOfWeek(dayOfWeek);
	}
	public static DayOfWeek of(final int dayOfWeek) {
		return new DayOfWeek(dayOfWeek);
	}
	public static DayOfWeek valueOf(final String dayOfWeek) {
		return new DayOfWeek(dayOfWeek);
	}
	public static DayOfWeek fromString(final String dayOfWeek) {
		return new DayOfWeek(dayOfWeek);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private void _set(final int dayOfWeek) {
		Preconditions.checkArgument(dayOfWeek <= 7 || dayOfWeek > 0,"Not a valid day of week");
		_dayOfWeek = dayOfWeek;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return Long.toString(_dayOfWeek);
	}
	@Override
	public String asString() {
		return this.toString();
	}
	public int asInteger() {
		return _dayOfWeek;
	}
	public int asIntegerStartingOnMonday() {
		return _dayOfWeek == 1 ? 7					// sunday becomes 7
							   : _dayOfWeek - 1;	// monday becomes 1 and so on
	}
	public int asIntegerStartingOnSunday() {
		return _dayOfWeek;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBefore(final DayOfWeek other) {
		return _dayOfWeek < other.asInteger();
	}
	public boolean isAfter(final DayOfWeek other) {
		return _dayOfWeek > other.asInteger();
	}
	public boolean isBeforeOrEqual(final DayOfWeek other) {
		return _dayOfWeek <= other.asInteger();
	}
	public boolean isAfterOrEqual(final DayOfWeek other) {
		return _dayOfWeek >= other.asInteger();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof DayOfWeek) return ((DayOfWeek)obj).getDayOfWeek() == _dayOfWeek;
		return false;
	}
	@Override
	public int hashCode() {
		return new Integer(_dayOfWeek).hashCode();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final DayOfWeek SUNDAY = new DayOfWeek(1);
	public static final DayOfWeek MONDAY = new DayOfWeek(2);
	public static final DayOfWeek TUESDAY = new DayOfWeek(3);
	public static final DayOfWeek WEDNESDAY = new DayOfWeek(4);
	public static final DayOfWeek THURSDAY = new DayOfWeek(5);
	public static final DayOfWeek FRIDAY = new DayOfWeek(6);
	public static final DayOfWeek SATURDAY = new DayOfWeek(7);
}
