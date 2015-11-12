package r01f.xmlproperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.CommonOIDs.Environment;
import r01f.internal.BuiltInObjectsMarshaller;
import r01f.marshalling.MarshallerException;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDef;
import r01f.types.Path;
import r01f.util.types.Strings;

/**
 * <b>CARGA DE PROPIEDADES:
 * ---------------------</b></br>
 * La carga de propiedades se hace en DOS fases:<br/>
 * <pre>
 * 		FASE 1:	Cargar la definición del componente.
 * 				Se busca un fichero en el ClassPath con el nombre [appCode].[component].xml
 * 				en la ruta /components/[appCode].[component].xml<br/>
 * 		FASE 2: En el fichero de definición del componente se indica cómo cargar los properties
 * 				utilizando cualquier tipo de cargador (FileSystem, ClassPath, BBDD, etc).<br/>
 * </pre>
 * <p>
 * Esta clase representa la definicion de un componente del XMLProperties de una aplicación.<br>
 * El estado de este objeto se carga a partir de un XML que contiene datos que permiten al XMLPropertiesManager
 * saber cómo realizar la carga el fichero de propiedades, es decir, un XMLPropertiesComponentDef contiene las
 * instrucciones de cómo cargar (y recargar) los properties de un componente de una aplicacion (FASE 1).<br>
 *
 * Ejemplo:
 * <ul>
 * <li>Fichero de Propiedades en el classPath
 * <pre class="brush:xml">
 * 	<componentDef>
 * 		<propertiesFileURI>/config/r01fb.properties.xml</propertiesFileURI>
 * 			<resourcesLoader type='CLASSPATH'>
 * 				<reloadControl impl='PERIODIC' enabled='true' checkInterval='2s'/>
 * 			</resourcesLoader>
 *	</componentDef>
 * </pre>
 * </li>
 * <li>Fichero de Propiedades en un registro de BBDD.
 * <pre class="brush:java">
 * 	<componentDef>
 * 		<propertiesFileURI>SELECT ...</propertiesFileURI>
 * 		<resourcesLoader type='BBDD'>
 * 			<props>
 * 				<conx>MyConx</conx>
 * 			</props>
 * 		</resourcesLoader>
 * 	</componentDef>
 * </pre>
 * </li>
 * </ul>
 *
 */
@XmlRootElement(name="componentDef")
@Accessors(prefix="_")
@NoArgsConstructor
public class XMLPropertiesComponentDef 
  implements Serializable,
		     Debuggable {
	
	private static final long serialVersionUID = 646222659405032701L;
///////////////////////////////////////////////////////////////////////////////
// 	MIEMBROS
///////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="name")
	@Getter @Setter private AppComponent _name;
	
	@XmlElement(name="resourcesLoader")
	@Getter @Setter private ResourcesLoaderDef _loaderDef;
	
	@XmlElement(name="propertiesFileURI")
	@Getter @Setter private Path _propertiesFileURI;
	
	@XmlElement(name="numberOfPropertiesEstimation")
	@Getter @Setter private int _numberOfPropertiesEstimation = XMLPropertiesForAppCache.DEFAULT_PROPERTIES_PER_COMPONENT;
///////////////////////////////////////////////////////////////////////////////
// 	METODOS
///////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.create()
					  .addCustomized("\r\n           Name: {}",_name.asString())
				      .addCustomized("\r\npropsEstimation: {}",Integer.toString(_numberOfPropertiesEstimation))
				      .addCustomized("\r\n        fileUri: {}",_propertiesFileURI.asString())
				      .addCustomized("\r\n         loader: {}",_loaderDef != null ? _loaderDef.debugInfo() : "");
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS ESTATICOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Carga el xml de definición de un componente donde se indica cómo se cargan las propiedades de este componente.
     * @param env environment
     * @param appCode Código de aplicación.
     * @param component Componente.
     * @return La definición del componente.
     * @throws XMLPropertiesException Si no se puede cargar el fichero o este está mal formado.
     */
    @SuppressWarnings("resource")
	public static XMLPropertiesComponentDef load(final Environment env,
    											 final AppCode appCode,
    											 final AppComponent component) throws XMLPropertiesException {
    	XMLPropertiesComponentDef outDef = null;
		// Cargar la definición de componentes del fichero /config/appCode/components/appCode.component.xml
        try {
        	// Obtener un InputStream al XML de definición del componente
        	// Este fichero SIEMPRE SE CARGA DEL CLASSPATH en una ruta components/[appCode].[component].xml
        	// OJO!! la ruta es RELATIVA ya que se utiliza
        	//		 el ClassLoader (ver ClassPathResourcesLoader)
        	String filePath = null;
        	if (env == null || env.equals(Environment.NO_ENV)) {
        		filePath = Strings.of("{}/components/{}.{}.xml")							// Ej. /components/r01.default.xml
        						  .customizeWith(appCode,appCode,component).asString();	
        	} else {
        		filePath = Strings.of("{}/{}/components/{}.{}.xml")							// Ej: /components/loc/r01.default.xml
        						   .customizeWith(env,appCode,appCode,component).asString();
        	}
        	
        	ResourcesLoader resourcesLoader = ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER;					
			@Cleanup InputStream defXmlIS = resourcesLoader.getInputStream(filePath,true);	// true: use cache
        	if (defXmlIS != null) {
        		outDef = BuiltInObjectsMarshaller.instance()
        										 .beanFromXml(defXmlIS);
        	} else {
        		throw XMLPropertiesException.componentDefLoadError(env,appCode,component);
        	}
        } catch (MarshallerException msEx) {
    		throw XMLPropertiesException.componentDefXMLError(env,appCode,component,
    														  msEx);
        } catch (IOException ioEx) {
    		throw XMLPropertiesException.componentDefLoadError(env,appCode,component,
    														   ioEx);
        }
        // Añadir el nombre (que no está en el XML o si está NO se utiliza)
        outDef.setName(component);
    	return outDef;
    }
}
