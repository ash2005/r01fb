package r01f.types;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.GwtIncompatible;

import r01f.util.types.Numbers;

/**
 * Represents some time interval o lapse
 * Usage:
 * <pre class='brush:java'>
 * 		TimeLapse timeLapse = TimeLapse.createFor("5s");
 * 		long milis = timeLapse.get();
 * </pre>
 */
public class TimeLapse
  implements Serializable {

	private static final long serialVersionUID = 8201041020863160970L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern TIMELAPSE_PATTERN = Pattern.compile("\\s*([0-9]+)\\s*(s|m|h|d)\\s*");
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private long _timeLapse;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public TimeLapse(final long milis) {
		_timeLapse = milis;
	}
	public TimeLapse(final String timeSpec) {
		_timeLapse = _parseTimeLapseSpec(timeSpec);
	}
	public static TimeLapse valueOf(final String timeSpec) {
		return new TimeLapse(timeSpec);
	}
	/**
	 * Creates a {@link TimeLapse} from milis
	 * @param millis the milis
	 * @return
	 */
	public static TimeLapse createFor(final long milis) {
		return new TimeLapse(milis);
	}
	/**
	 * Creates a {@link TimeLapse} from units
	 * @param units
	 * @param unit
	 * @return
	 */
	public static TimeLapse createFor(final long units,final TimeUnit unit) {
		return TimeLapse.createFor(unit.toMillis(units));
	}
	/**
	 * Creates a {@link TimeLapse} from a textual spec like some of the following
	 * <ul>
	 * 		<li>1d for one day</li>
	 * 		<li>1h for one hour</li>
	 * 		<li>30m for 30 minutes</li>
	 * 		<li>100s for 100 seconds</li>
	 * </ul>
	 * @param timeSpec the spec
	 * @return
	 */
	@GwtIncompatible("uses regexp")
	public static TimeLapse createFor(final String timeSpec) {
		return new TimeLapse(_parseTimeLapseSpec(timeSpec));
	}
	/**
	 * Creates a {@link TimeLapse} from a textual spec like some of the following
	 * <ul>
	 * 		<li>1d for one day</li>
	 * 		<li>1h for one hour</li>
	 * 		<li>30m for 30 minutes</li>
	 * 		<li>100s for 100 seconds</li>
	 * </ul>
	 * @param timeSpec the spec
	 * @return
	 */
	@GwtIncompatible("uses regexp")
	public static TimeLapse of(final String timeSpec) {
		return new TimeLapse(_parseTimeLapseSpec(timeSpec));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public long asMilis() {
		return _timeLapse;
	}
	public String asString() {
		return Long.toString(_timeLapse);
	}
	@Override
	public String toString() {
		return this.asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE STATIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@GwtIncompatible("uses regexp")
	static long _parseTimeLapseSpec(final String periodSpec) {
		long outPeriod = -1;
		Matcher m = TIMELAPSE_PATTERN.matcher(periodSpec);
		if (m.matches()) {
			long periodValue = Long.parseLong(m.group(1));
			String periodUnitStr = m.group(2);
			if (periodUnitStr.equalsIgnoreCase("s")) {
				outPeriod = periodValue * 1000l;					// 1 sg = 1000 millis
			} else if (periodUnitStr.equalsIgnoreCase("m")) {
				outPeriod = periodValue * 60l * 1000l;				// 1 min = 60 sg = 60 * 1000 millis
			} else if (periodUnitStr.equalsIgnoreCase("h")) {
				outPeriod = periodValue * 60l * 60l * 1000l;		// 1 h = 60 min = 60 * 60 * 1000 millis
			}  else if (periodUnitStr.equalsIgnoreCase("d")) {
				outPeriod = periodValue * 24l * 60l * 60l * 1000l;	// 1 d = 24h = 24 * 60 * 60 * 1000 millis
			}
		} else if (Numbers.isLong(periodSpec)) {
			outPeriod = Long.parseLong(periodSpec);
		}
		return outPeriod;
	}
}
