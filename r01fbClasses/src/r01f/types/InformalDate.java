package r01f.types;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A simple type used to hold info about a {@link Date} when sometimes the day, month or even the year
 * are NOT relevant
 * Sample usages:
 * <pre class='brush:java'>
 * 		InformalDate date1 = new InformalDate("2015");
 * 		InformalDate date2 = new InformalDate("2015","March");
 * 		InformalDate date1 = new InformalDate("2015","March","25 monday");
 * </pre>
 */
@XmlRootElement(name="date")
@Accessors(prefix="_")
public class InformalDate 
  implements Serializable {
	private static final long serialVersionUID = 1110792232316017013L;
/////////////////////////////////////////////////////////////////////////////////////////
//  DATE
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="year")
	@Getter @Setter private String _year;
	
	@XmlAttribute(name="month")
	@Getter @Setter private String _month;
	
	@XmlAttribute(name="day")
	@Getter @Setter private String _day;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public InformalDate() {
		// default no args constructor
	}
	public InformalDate(final String year,final String month,final String day) {
		_year = year;
		_month = month;
		_day = day;
	}
	public InformalDate(final String year,final String month) {
		_year = year;
		_month = month;
	}
	public InformalDate(final String year) {
		_year = year;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static InformalDate create() {
		return new InformalDate();
	}
	public static InformalDate createFor(final String year,final String month,final String day) {
		return new InformalDate(year,month,day);
	}
	public static InformalDate createFor(final String year,final String month) {
		return new InformalDate(year,month);
	}
	public static InformalDate createFor(final String year) {
		return new InformalDate(year);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public InformalDate year(final String year) {
		_year = year;
		return this;
	}
	public InformalDate month(final String month) {
		_month = month;
		return this;
	}
	public InformalDate day(final String day) {
		_day = day;
		return this;
	}
}
