package r01f.types.datetime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.patterns.Memoized;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Dates;
import r01f.util.types.Strings;

@XmlRootElement(name="time")
@Accessors(prefix="_")
@NoArgsConstructor
public class Time 
  implements Serializable,
  			 CanBeRepresentedAsString {


	private static final long serialVersionUID = -7084816234257337127L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private String _time;

	private transient final Memoized<HourMinutesMilis> _hourMinutesMilis = new Memoized<HourMinutesMilis>() {
																					@Override
																					protected HourMinutesMilis supply() {
																						return _hourMinutesMilisfrom(_time);
																					}
																		  };														

	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private class HourMinutesMilis {
		final int hour;
		final int minutes;
		final int milis;
	}
	private static final transient Pattern TIME_PATTERN = Pattern.compile("([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])(?::([0-9]{3}))?(\\s)?(?i)(am|pm)?");
	private HourMinutesMilis _hourMinutesMilisfrom(final String timeStr) {
			Matcher m = TIME_PATTERN.matcher(timeStr);
			if (!m.find()) throw new IllegalStateException(timeStr + " is not a valid time: MUST match " + TIME_PATTERN);
			int hour = Integer.parseInt(m.group(1));
			int minutes = Integer.parseInt(m.group(2));
			int milis = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
			String ampm = m.group(4);
			
			if (Strings.isNOTNullOrEmpty(ampm)
			 && ampm.equalsIgnoreCase("PM") 
			 && hour < 12) {
				hour = 12 + hour;
			}
			return new HourMinutesMilis(hour,minutes,milis);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public Time(final int hour,final int minutes,final int milis) {
		_set(hour,minutes,milis);
	}
	public Time(final int hour,final int minutes) {
		_set(hour,minutes,0);
	}
	public Time(final String time) {
		_set(time);
	}
	public static Time of(final String time) {
		return new Time(time);
	}
	public static Time of(final Date date) {
		Calendar cal = Dates.asCalendar(date);
		return new Time(cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE),cal.get(Calendar.MILLISECOND));
	}
	public static Time of(final int hour,final int minutes,final int milis) {
		return new Time(hour,minutes,milis);
	}
	public static Time of(final int hour,final int minutes) {
		return new Time(hour,minutes,0);
	}
	public static Time valueOf(final String time) {
		return new Time(time);
	}
	public static Time fromString(final String time) {
		return new Time(time);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private void _set(final int hour,final int minutes,final int milis) {
		Preconditions.checkArgument(hour >= 0,"Not a valid hour");
		Preconditions.checkArgument(minutes >= 0,"Not a valid minutes");
		Preconditions.checkArgument(milis >= 0,"Not a valid milis");
		_time = Strings.customized("{}:{}:{}",
									String.format("%02d",hour),
									String.format("%02d",minutes),
									String.format("%03d",milis));		
	}
	private void _set(final String timeStr) {
		HourMinutesMilis hourMinMilis = _hourMinutesMilisfrom(timeStr);
		_set(hourMinMilis.hour,hourMinMilis.minutes,hourMinMilis.milis);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return _time;
	}
	@Override
	public String asString() {
		return this.toString();
	}
	public int getHour() {
		return _hourMinutesMilis.get().hour;
	}
	public int getMinutes() {
		return _hourMinutesMilis.get().minutes;
	}
	public int getMilis() {
		return _hourMinutesMilis.get().milis;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isBefore(final Time other) {
		HourMinutesMilis t = _hourMinutesMilis.get();
		HourMinutesMilis o = other._hourMinutesMilis.get();
		return (t.hour < o.hour)
			|| (t.hour == o.hour && t.minutes < o.minutes)
			|| (t.hour == o.hour && t.minutes == o.minutes && t.milis < o.milis);
	}
	public boolean isAfter(final Time other) {
		HourMinutesMilis t = _hourMinutesMilis.get();
		HourMinutesMilis o = other._hourMinutesMilis.get();
		return (t.hour > o.hour)
			|| (t.hour == o.hour && t.minutes > o.minutes)
			|| (t.hour == o.hour && t.minutes == o.minutes && t.milis > o.milis);
	}
	public boolean isBeforeOrEqual(final Time other) {
		HourMinutesMilis t = _hourMinutesMilis.get();
		HourMinutesMilis o = other._hourMinutesMilis.get();
		return (t.hour < o.hour)
			|| (t.hour == o.hour && t.minutes < o.minutes)
			|| (t.hour == o.hour && t.minutes == o.minutes && t.milis <= o.milis);
	}
	public boolean isAfterOrEqual(final Time other) {
		HourMinutesMilis t = _hourMinutesMilis.get();
		HourMinutesMilis o = other._hourMinutesMilis.get();
		return (t.hour > o.hour)
			|| (t.hour == o.hour && t.minutes > o.minutes)
			|| (t.hour == o.hour && t.minutes == o.minutes && t.milis >= o.milis);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof Time) {
			Time other = (Time)obj;
			HourMinutesMilis t = _hourMinutesMilis.get();
			HourMinutesMilis o = other._hourMinutesMilis.get();
			return (t.hour > o.hour)
				|| (t.hour == o.hour && t.minutes > o.minutes)
				|| (t.hour == o.hour && t.minutes == o.minutes && t.milis == o.milis);
		}
		return false;
	}
	@Override
	public int hashCode() {
		HourMinutesMilis t = _hourMinutesMilis.get();
		return new Integer(t.hour * t.minutes * t.milis).hashCode();
	}
}
