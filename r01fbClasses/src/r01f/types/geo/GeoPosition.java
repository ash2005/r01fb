package r01f.types.geo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.locale.LanguageTextsMapBacked;
import r01f.types.GeoPosition2D;
import r01f.types.geo.GeoOIDs.GeoZipCode;

/**
 * Data about a geographical point
 * <pre>
 *		- x,y
 * 		- Country
 *   		|_Territory 
 *   	 		|_State
 *   		 		|_Locality
 *   					|_Municipality
 *   						|_District
 *   							|_Street
 *		- Textual info
 * </pre> 
 * Uso:
 * <pre class='brush:java'>
 * 	GeoPosition pos = GeoPosition.create()
 * 								 .at(GeoPosition2D.usingStandard(GOOGLE)
 * 								 				  .setLocation(lat,lon))
 * 								 .at(GeoCountry.create(GeoCountryOID.forId(34))
 * 								   			   .withNameInLang(Language.SPANISH,"España")
 * 								   			   .positionedAt(GeoPosition2D.usingStandard(GeoPositionStandad.GOOGLE)
 *													 		  			  .setLocation(lat,lon));
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="geoPosition")
@Accessors(prefix="_")
@NoArgsConstructor
public class GeoPosition
  implements Serializable {
	
	private static final long serialVersionUID = 8722622151577217898L;
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Country
	 */
	@XmlElement(name="country")
	@Getter @Setter private GeoCountry _country;
	/**
	 * Territory (groups more than a single state)
	 */
	@XmlElement(name="territory")
	@Getter @Setter private GeoTerritory _territory;
	/**
	 * State or province 
	 */
	@XmlElement(name="state")
	@Getter @Setter private GeoState _state;
	/**
	 * Locality / region (groups some municipalities)
	 */
	@XmlElement(name="locality")
	@Getter @Setter private GeoLocality _locality;
	/**
	 * Municipality
	 */
	@XmlElement(name="municipality")
	@Getter @Setter private GeoMunicipality _municipality;
	/**
	 * District
	 */
	@XmlElement(name="district")
	@Getter @Setter private GeoDistrict _district;
	/**
	 * Street
	 */
	@XmlElement(name="street")
	@Getter @Setter private GeoStreet _street;
	/**
	 * Zip code
	 */
	@XmlElement(name="zipCode")
	@Getter @Setter private GeoZipCode _zipCode;
	/**
	 * X,Y position
	 */
	@XmlElement(name="position2D")
	@Getter @Setter private GeoPosition2D _position;
	/**
	 * Textual directions (language dependent)
	 */
	@XmlElementWrapper(name="directions")
	@Getter @Setter private LanguageTextsMapBacked _directions;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static GeoPosition create() {
		GeoPosition outPos = new GeoPosition();
		return outPos;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public GeoPosition at(final GeoPosition2D pos) {
		_position = pos;
		return this;
	}
	public GeoPosition at(final GeoCountry country) {
		_country = country;
		return this;
	}
	public GeoPosition at(final GeoTerritory territory) {
		_territory = territory;
		return this;
	}
	public GeoPosition at(final GeoState state) {
		_state = state;
		return this;
	}
	public GeoPosition at(final GeoLocality loc) {
		_locality = loc;
		return this;
	}
	public GeoPosition at(final GeoMunicipality mun) {
		_municipality = mun;
		return this;
	}
	public GeoPosition at(final GeoDistrict dist) {
		_district = dist;
		return this;
	}
	public GeoPosition at(final GeoStreet street) {
		_street = street;
		return this;
	}
	public GeoPosition withZipCode(final GeoZipCode zipCode) {
		_zipCode = zipCode;
		return this;
	}
	public GeoPosition withDirections(final LanguageTextsMapBacked directions) {
		_directions = directions;
		return this;
	}
}
