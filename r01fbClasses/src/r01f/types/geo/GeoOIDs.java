package r01f.types.geo;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.guids.OIDBaseMutable;
import r01f.guids.OIDTyped;
import r01f.types.annotations.Inmutable;
/**
 * Geo catalog model object's oids
 * <pre>
 * Country
 *   |_Territory 
 *   	 |_State
 *   		 |_Locality
 *   			|_Municipality
 *   				|_District
 *   					|_Street
 * They're modeled after a long code that encapsulates the geo element (country, territory, street, etc code)
 * </pre>
 */
public class GeoOIDs {
///////////////////////////////////////////////////////////////////////////////////////////
// 	base
///////////////////////////////////////////////////////////////////////////////////////////
	public interface GeoID 
			 extends OIDTyped<Long> {
		/* just a marker interface */
	}
	/**
	 * Geo oid
	 */
	@Inmutable
	@NoArgsConstructor
	public static abstract class GeoIDBase
						 extends OIDBaseMutable<Long>
					  implements GeoID {
		private static final long serialVersionUID = 6766060252605584309L;
		public GeoIDBase(final long id) {
			super(id);
		}
		public GeoIDBase(final Long id) {
			super(id);
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  oids
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Country
	 */
	@Inmutable
	@XmlRootElement(name="geoCountryId")
	@NoArgsConstructor
	public static class GeoCountryID 
				extends GeoIDBase {
		private static final long serialVersionUID = -3806247489287958499L;
		public GeoCountryID(final long oid) {
			super(oid);
		}
		public GeoCountryID(final Long oid) {
			super(oid);
		}
		public static GeoCountryID forId(final long id) {
			return new GeoCountryID(id);
		}
		public static GeoCountryID valueOf(final String str) {
			return new GeoCountryID(Long.parseLong(str));
		}
	}
	/**
	 * Territory
	 */
	@Inmutable
	@XmlRootElement(name="geoTerritoryId")
	@NoArgsConstructor
	public static class GeoTerritoryID 
				extends GeoIDBase {
		private static final long serialVersionUID = -5811800490034132576L;
		public GeoTerritoryID(final long oid) {
			super(oid);
		}
		public GeoTerritoryID(final Long oid) {
			super(oid);
		}
		public static GeoTerritoryID forId(final long id) {
			return new GeoTerritoryID(id);
		}
		public static GeoTerritoryID valueOf(final String str) {
			return new GeoTerritoryID(Long.parseLong(str));
		}
	}
	/**
	 * Estate
	 */
	@Inmutable
	@XmlRootElement(name="geoStateId")
	@NoArgsConstructor
	public static class GeoStateID 
				extends GeoIDBase {
		private static final long serialVersionUID = -7636328071565337389L;
		public GeoStateID(final long oid) {
			super(oid);
		}
		public GeoStateID(final Long oid) {
			super(oid);
		}
		public static GeoStateID forId(final long id) {
			return new GeoStateID(id);
		}
		public static GeoStateID valueOf(final String str) {
			return new GeoStateID(Long.parseLong(str));
		}
	}
	/**
	 * Locality
	 */
	@Inmutable
	@XmlRootElement(name="geoLocalityId")
	@NoArgsConstructor
	public static class GeoLocalityID 
				extends GeoIDBase {
		private static final long serialVersionUID = 8445129300980606911L;
		public GeoLocalityID(final long oid) {
			super(oid);
		}
		public GeoLocalityID(final Long oid) {
			super(oid);
		}
		public static GeoLocalityID forId(final long id) {
			return new GeoLocalityID(id);
		}
		public static GeoLocalityID valueOf(final String str) {
			return new GeoLocalityID(Long.parseLong(str));
		}
	}
	/**
	 * Municipality
	 */
	@Inmutable
	@XmlRootElement(name="geoMunicipalityId")
	@NoArgsConstructor
	public static class GeoMunicipalityID 
				extends GeoIDBase {
		private static final long serialVersionUID = -8855341000465307541L;
		public GeoMunicipalityID(final long oid) {
			super(oid);
		}
		public GeoMunicipalityID(final Long oid) {
			super(oid);
		}
		public static GeoMunicipalityID forId(final long id) {
			return new GeoMunicipalityID(id);
		}
		public static GeoMunicipalityID valueOf(final String str) {
			return new GeoMunicipalityID(Long.parseLong(str));
		}
	}
	/**
	 * District
	 */
	@Inmutable
	@XmlRootElement(name="geoDistrictId")
	@NoArgsConstructor
	public static class GeoDistrictID 
				extends GeoIDBase {
		private static final long serialVersionUID = -8855341000465307541L;
		public GeoDistrictID(final long oid) {
			super(oid);
		}
		public GeoDistrictID(final Long oid) {
			super(oid);
		}
		public static GeoDistrictID forId(final long id) {
			return new GeoDistrictID(id);
		}
		public static GeoDistrictID valueOf(final String str) {
			return new GeoDistrictID(Long.parseLong(str));
		}
	}
	/**
	 * Street
	 */
	@Inmutable
	@XmlRootElement(name="geoStreetId")
	@NoArgsConstructor
	public static class GeoStreetID 
				extends GeoIDBase {
		private static final long serialVersionUID = 8671822814362300783L;
		public GeoStreetID(final long oid) {
			super(oid);
		}
		public GeoStreetID(final Long oid) {
			super(oid);
		}
		public static GeoStreetID forId(final long id) {
			return new GeoStreetID(id);
		}
		public static GeoStreetID valueOf(final String str) {
			return new GeoStreetID(Long.parseLong(str));
		}
	}
}
