package r01f.xmlproperties;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.types.Path;

@XmlRootElement(name="propertyLocation")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class XMLPropertyLocation 
  implements Serializable {

	private static final long serialVersionUID = -8227530219278024640L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern RES_LOADER_PATTERN = Pattern.compile("([^/]+)$([^/]+)$(.+)");
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="appCode")
	@Getter @Setter private AppCode _appCode;
	@XmlAttribute(name="component")
	@Getter @Setter private AppComponent _component;
	@XmlValue
	@Getter @Setter private Path _xPath;

/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static XMLPropertyLocation createFor(final AppCode appCode,final AppComponent component,final Path path) {
		return new XMLPropertyLocation(appCode,component,path);
	}
	/**
	 * Creates a {@link XMLPropertyLocation} from it's id which is a String like:
	 * {appCode}${component}${xPath}
	 * @param id the identifier
	 * @return the {@link XMLPropertyLocation} composed from the id
	 */
	public static XMLPropertyLocation fromId(final String id) {
		// The location of the property where the ResourcesLoader definition (ResourcesLoaderDef)
		// is expressed like {appCode}${component}${xPath}
		XMLPropertyLocation outLoc = null;
		Matcher m = RES_LOADER_PATTERN.matcher(id);
		if (m.find() && m.groupCount() == 3) {
			AppCode appCode = AppCode.forId(m.group(1));
			AppComponent component = AppComponent.forId(m.group(2));
			Path xPath = Path.of(m.group(3));
			outLoc = new XMLPropertyLocation(appCode,component,xPath);
		}
		return outLoc;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return an id like {appCode}${component}${xPath} 
	 */
	public String composeId() {
		return XMLPropertyLocation.composeId(_appCode,_component,_xPath);
	}
	public static String composeId(final AppCode appCode,final AppComponent component,final Path xPath) {
		return appCode.asString() + "$" + component + "$" + xPath;
	}
}
