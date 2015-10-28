package r01f.types.geo;


import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.locale.LanguageTextsMapBacked;
import r01f.types.GeoPosition2D;
import r01f.types.geo.GeoOIDs.GeoCountryID;

/**
 * Country data
 * <pre>
 * Country
 *   |_Territory 
 *   	 |_State
 *   		 |_Locality
 *   			|_Municipality
 *   				|_District
 *   					|_Street
 * </pre>
 * <pre class='brush:java'>
 *		GeoCountry country = new R01MGeoCountry(GeoCountryID.forId(34),
 *											    LanguageTexts.of(Language.SPANISH,"España")
 *															 .addForLang(Language.ENGLISH,"Spain"),
 *												GeoPosition2D.usingStandard(GeoPositionStandad.GOOGLE)
 *															 .setLocation(lat,lon));
 * </pre>
 * or
 * <pre class='brush:java'>
 * 		GeoCountry country = GeoCountry.create(R01MGeoCountryID.forId(34))
 * 									   .withNameInLang(Language.SPANISH,"España")
 * 									   .positionedAt(GeoPosition2D.usingStandard(GeoPositionStandad.GOOGLE)
 *													 		  	  .setLocation(lat,lon);
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="geoCountry")
@NoArgsConstructor
public class GeoCountry 
     extends GeoLocationBase<GeoCountryID,GeoCountry> {

	private static final long serialVersionUID = -4324327721026938576L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public GeoCountry(final GeoCountryID oid,final LanguageTextsMapBacked name,final GeoPosition2D position2D) {
		super(oid,name,position2D);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static GeoCountry create() {
		return new GeoCountry();
	}
	public static GeoCountry create(final GeoCountryID geoOid) {
		GeoCountry outGeo = new GeoCountry();
		outGeo.setId(geoOid);
		return outGeo;
	}
}
