package r01f.resources;

import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;

/**
 * ResourcesLoader definition<br/>
 * @see ResourcesLoaderDefBuilder
 */
@XmlRootElement(name="resourcesLoader")
@Accessors(prefix="_")
public class ResourcesLoaderDef 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
// 	RESOURCES LOADERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static enum ResourcesLoaderType {
		CLASSPATH,
		FILESYSTEM,
		URL,
		CONTENT_SERVER,
		BBDD;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	DEFAULT DEFINITION
/////////////////////////////////////////////////////////////////////////////////////////
	public static ResourcesLoaderDef DEFAULT = new ResourcesLoaderDef("DefaultClassPathLoaderDef",
																	  ResourcesLoaderType.CLASSPATH,
																	  ResourcesReloadControlDef.DEFAULT);
///////////////////////////////////////////////////////////////////////////////
// 	FIELDS
///////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="id")
	@Getter	@Setter private String _id;
	
	@XmlAttribute(name="type")
	@Getter @Setter private ResourcesLoaderType _loader;
	
	@XmlElement(name="reloadControl")
	@Getter @Setter private ResourcesReloadControlDef _reloadControlDef;
	
	@XmlElementWrapper(name="props")
	@Getter @Setter private Map<String,String> _loaderProps;
	
	@XmlAttribute(name="charset")
	@Getter @Setter private String _charsetName;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ResourcesLoaderDef() {
		
	}
	public ResourcesLoaderDef(final String id,final ResourcesLoaderType type,final ResourcesReloadControlDef reloadControlDef) {
		_id = id;
		_loader = type;
		_reloadControlDef = reloadControlDef;
		_charsetName = Charset.defaultCharset().name();
	}
	public ResourcesLoaderDef(final String id,
							 final ResourcesLoaderType type,
							 final ResourcesReloadControlDef reloadControlDef,
							 final Charset charset) {
		_id = id;
		_loader = type;
		_reloadControlDef = reloadControlDef;
		_charsetName = charset != null ? charset.name() : Charset.defaultCharset().name();
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public Charset getCharset() {
		return Charset.forName(_charsetName);
	}
	/**
	 * Creates a {@link ResourcesLoader} using this definition object 
	 */
	public ResourcesLoader createResourcesLoader() {
		ResourcesLoader outResLoader = null;
		switch (_loader) {
		case BBDD:
			outResLoader = new ResourcesLoaderFromBBDD(this);
			break;
		case CONTENT_SERVER:
			break;
		case URL:
			outResLoader = new ResourcesLoaderFromURL(this);
			break;
		case FILESYSTEM:
			outResLoader = new ResourcesLoaderFromFileSystem(this);
			break;
		case CLASSPATH:
		default:
			outResLoader = new ResourcesLoaderFromClassPath(this);
		}
		return outResLoader;
	}
	/**
	 * Returns a property using it's key (name)
	 * @param propName the property key
	 * @return the property value
	 */
	public String getProperty(final String propName) {
		String outProp = this.getLoaderProps() != null ? this.getLoaderProps().get(propName)
													   : null;
		return outProp;
	}
	@Override
	public CharSequence debugInfo() {
		StringExtended sw = Strings.create(100);
		sw.addCustomizedIfParamNotNull("\r\n\t\t      id: {}",_id);
		sw.addCustomizedIfParamNotNull("\r\n\t\t    name: {}",(_loader == null ? "null" : _loader.name()))
		  .addCustomizedIfParamNotNull("\r\n\t\t   props: ({})",(_loaderProps == null ? "null":Integer.toString(_loaderProps.size())));
		if (_loaderProps != null) {
			for (Map.Entry<String,String> prop : _loaderProps.entrySet()) {
				sw.addCustomized("\r\n\t\t\t-{}:{}",prop.getKey(),prop.getValue());
			}
		}
		sw.addCustomized("\r\n\t\treloadControl:{}",_reloadControlDef != null ? _reloadControlDef.debugInfo() : "none");
		return sw;
	}
}
