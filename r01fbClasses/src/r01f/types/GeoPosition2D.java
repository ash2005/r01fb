package r01f.types;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

/**
 * Coordenadas de una posición en dos dimensiones: latitud y longitud
 * <code>
 * 		GeoPosition2D geo = GeoPosition2D.usingStandard(GOOGLE)
 * 										 .setLocation(lat,lon);
 * </code>
 */
@Inmutable
@XmlRootElement(name="geoPosition2D")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class GeoPosition2D
  implements Serializable {
	
	private static final long serialVersionUID = 3126318415213511386L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="standard")
	@Getter @Setter private GeoPositionStandad _standard;
	
	@XmlAttribute(name="latitude")
	@Getter @Setter private double _latitude;
	
	@XmlAttribute(name="longitude")
	@Getter @Setter private double _longitude;
/////////////////////////////////////////////////////////////////////////////////////////
//  ENUM DE ESTANDARES DE MEDICION DE lat/long
/////////////////////////////////////////////////////////////////////////////////////////
	public static enum GeoPositionStandad {
		GOOGLE,
		ISO;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API: FACTORÍA
/////////////////////////////////////////////////////////////////////////////////////////
	public static GeoPosition2DWithoutCoords usingStandard(final GeoPositionStandad standard) {
		return new GeoPosition2DWithoutCoords(standard);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API CLASE AUXILIAR
/////////////////////////////////////////////////////////////////////////////////////////	
	@RequiredArgsConstructor
	public static class GeoPosition2DWithoutCoords {
		private final GeoPositionStandad _theStandard;
		public GeoPosition2D setLocation(final double latitude,final double longitude) {
			return new GeoPosition2D(_theStandard,latitude,longitude);
		}
	}
}
