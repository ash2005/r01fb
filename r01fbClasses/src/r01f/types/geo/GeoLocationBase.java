package r01f.types.geo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsBuilder;
import r01f.locale.LanguageTextsMapBacked;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.types.GeoPosition2D;
import r01f.types.geo.GeoOIDs.GeoID;

/**
 * Geo info base type
 * <pre class='brush:java'>
 * R01MGeoLocationPart<R01MGeoCountry> country = new R01MGeoLocationPart<R01MGeoCountry>(34,
 * 																					    "Spain",
 * 																						GeoPosition2D.usingStandard(GOOGLE).setLocation(lat,long));
 * </pre>
 * @param <GID>
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public abstract class GeoLocationBase<GID extends GeoID,
								      SELF_TYPE extends GeoLocationBase<GID,SELF_TYPE>> 
           implements Serializable {

	private static final long serialVersionUID = -1497083216318413697L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * geo location oid
	 */
	@XmlAttribute(name="oid")
	@Getter @Setter private GID _id;
	/**
	 * location name 
	 */
	@XmlElementWrapper(name="name")	@XmlCDATA	// wrapper is important
	@Getter @Setter private LanguageTextsMapBacked _name;
	/**
	 * Position 2D (lat/long)
	 */
	@XmlElement(name="geoPosition2D")
	@Getter @Setter private GeoPosition2D _position2D;
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public SELF_TYPE withNameInLang(final Language lang,final String name) {
		if (_name == null) _name = LanguageTextsBuilder.createMapBacked()
													   .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
													   .build();
		_name.add(lang,name);
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE withNameForAll(final String name) {
		_name = LanguageTextsBuilder.createMapBacked()
								    .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
								    .addForAll(name)
								    .build();
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE positionedAt(final GeoPosition2D geoPosition) {
		_position2D = geoPosition;
		return (SELF_TYPE)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public String getNameIn(final Language lang) {
		return _name != null ? _name.get(lang) : null;
	}
}
