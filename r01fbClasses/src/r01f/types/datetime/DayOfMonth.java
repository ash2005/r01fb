package r01f.types.datetime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.joda.time.LocalDate;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Dates;

@XmlRootElement(name="dayOfMonth")
@Accessors(prefix="_")
public class DayOfMonth 
  implements Serializable,
  			 CanBeRepresentedAsString {

	private static final long serialVersionUID = 7658275370612790932L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter private int _dayOfMonth;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public DayOfMonth(final int dayOfMonth) {
		_set(dayOfMonth);
	}
	public DayOfMonth(final Integer dayOfMonth) {
		_set(dayOfMonth);
	}
	public DayOfMonth(final String month) { 
		int m = Integer.parseInt(month);
		_set(m);
	}
	public static DayOfMonth of(final String dayOfMonth) {
		return new DayOfMonth(dayOfMonth);
	}
	public static DayOfMonth of(final Date date) {
		return new DayOfMonth(Dates.asCalendar(date).get(Calendar.DAY_OF_MONTH));
	}
	public static DayOfMonth of(final int dayOfMonth) {
		return new DayOfMonth(dayOfMonth);
	}
	public static DayOfMonth valueOf(final String dayOfMonth) {
		return new DayOfMonth(dayOfMonth);
	}
	public static DayOfMonth fromString(final String dayOfMonth) {
		return new DayOfMonth(dayOfMonth);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private void _set(final int dayOfMonth) {
		Preconditions.checkArgument(dayOfMonth <= 31 || dayOfMonth > 0,"Not a valid day of month");
		_dayOfMonth = dayOfMonth;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return Long.toString(_dayOfMonth);
	}
	@Override
	public String asString() {
		return this.toString();
	}
	public int asInteger() {
		return _dayOfMonth;
	}
	public boolean isValidAt(final Year year,final MonthOfYear monthOfYear) {
		return DayOfMonth.isValidDayOfMonth(year,monthOfYear,this);
	}
	public static boolean isValidDayOfMonth(final Year year,final MonthOfYear monthOfYear,final DayOfMonth dayOfMonth) {
		LocalDate localDate = new LocalDate(year.asInteger(),monthOfYear.asInteger(),1);
		int dayOfMonthMaxValue = localDate.dayOfMonth().getMaximumValue();
		return dayOfMonthMaxValue <= dayOfMonth.asInteger();		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBefore(final DayOfMonth other) {
		return _dayOfMonth < other.asInteger();
	}
	public boolean isAfter(final DayOfMonth other) {
		return _dayOfMonth > other.asInteger();
	}
	public boolean isBeforeOrEqual(final DayOfMonth other) {
		return _dayOfMonth <= other.asInteger();
	}
	public boolean isAfterOrEqual(final DayOfMonth other) {
		return _dayOfMonth >= other.asInteger();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof DayOfMonth) return ((DayOfMonth)obj).getDayOfMonth() == _dayOfMonth;
		return false;
	}
	@Override
	public int hashCode() {
		return new Integer(_dayOfMonth).hashCode();
	}
}
