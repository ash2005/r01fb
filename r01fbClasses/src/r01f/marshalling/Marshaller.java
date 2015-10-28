package r01f.marshalling;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.w3c.dom.Node;

import r01f.debug.Debuggable;
import r01f.encoding.TextEncoder;

/**
 * Marshaller de XML a objetos
 * ----------------------------
 *		IMPORTANTE!!! Hay DOS tipos de Marshallers
 *		<ul>
 *			<li>
 *			Marshaller que se van a utilizar UNA SOLA VEZ y por lo tanto NO es necesario guardar la cache de mapeos
 *			Ej: se carga un XML de configuración, se pasa a objetos y a partir de ahí se utilizan los objetos y NO se
 *			    vuelve a hacer Marshalling
 *			 	En este caso NO hay que guardar la cache de mapeos, así que hay que utilizar las anotaciones que permiten
 *			  	inyectar un SingleUseMarshaller:
 *					<ul>
 *						<li>{@link r01f.marshalling.annotations.SingleUseSimpleMarshaller} para la implementación del marshalling en base a {@link r01f.marshalling.simple.SimpleMarshallerBuilder}</li>
 *						<li>{@link r01f.marshalling.annotations.SingleUseJaxbMarshaller} para la implementación del marshalling en base a JAXB</li>
 *					</ul>
 *			</li>
 *			<li>
 *			Marshaller que se a va REUTILIZAR una y otra vez para hacer marshalling / unmarshalling de objeto y que por lo tanto
 *			conviene cachear para evitar tener que cargar los mapeos repetidamente
 *			Ej: persistir objetos en BBDD en formato XML
 *			En este caso SI hay que guardar la caché de mapeos, así que hay que utilizar las anotaciones que permiten
 *			inyectar un ReusableMarshaller
 *					<ul>
 *						<li>{@link r01f.marshalling.annotations.ReusableUseSimpleMarshaller} para la implementación del marshalling en base a {@link r01f.marshalling.simple.SimpleMarshallerBuilder}</li>
 *						<li>{@link r01f.marshalling.annotations.ReusableUseJaxbMarshaller} para la implementación del marshalling en base a JAXB</li>
 *					</ul>
 *			</li>
 *		</ul>
 *
 * [Creacción del Marshaller]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
 * ----------------------------------------------------------------------------
 * [Opción 1]: Inyectar el Marshaller utilizando GUICE 
 * 			   IMPORTANTE!	el objeto donde se inyecta el Marshaller
 * 			   				DEBE ser también creado por GUICE)	
 * 		En la clase donde se quiere inyectar el Marshaller crear un miembro anotado como
 * 		<pre class='brush:java'>
 * 		public class MyMarshallerService {
 * 			@Inject @ReusableSimpleMarshaller private Marshaller _marshaller;
 * 			...
 * 		}
 * 		</pre>
 * 		o bien inyectar en el constructor:
 * 		<pre class='brush:java'>
 * 		public class MyMarshallerService {
 * 			private Marshaller _marshaller;
 * 			@Inject
 * 			public MyMarshallerService(@ReusableSimpleMarshaller private Marshaller marshaller) {
 * 				_marshaller = marshaller;
 * 			}
 * 			...
 * 		}
 *		</pre>
 *
 * [Opcion 2]: Utilizar el inyector de Guice (no recomendado)
 * 		<pre class='brush:java'>
 * 			Marshaller marshaller = Guice.createInjector(new MarsallerGuiceModule())
 *										 .getInstance(Key.get(Marshaller.class,SingleUseSimpleMarshaller.class))
 *		</pre>
 *		NOTA:	Aplican las mismas consideraciones que en el caso anterior para el tipo de Marshaller a inyectar
 *
 * [OPCION 3]: SIN utilizar Guice (no recomendado)
 * 		Para utilizar el marshaller SIN guice es necesario saber:
 * 		<ol>
 * 			<li>Cómo se van a cargar los mapeos: en base a las anotaciones de las clases / en base a un fichero de mapeo</li>
 * 			<li>Qué tipo de marshaller se va a crear</li>
 * 		</ol>
 * 		Por ejemplo:
 * 		<pre class='brush:java'>
 * 			// [1] Crear los mappings a partir de las anotaciones
 * 			MarshallerMappings mappings = new SimpleMarshallerMappings();
 *			mappings.loadFromAnnotatedTypes(MarshallerMappingsSearch.inPackages("r01m.model"));
 *
 *			// [2] Crear el marshaller (en este caso de un solo uso)
 *			Marshaller marshaller = new SimpleMarshallerSingleUseImpl(mappings);
 * 		</pre>
 * 		O bien mas sencillo utilizando los builders:
 * 		<pre class='brush:java'>
 *			// Crear el marshaller (en este caso de un solo uso)
 *			Marshaller marshaller = new SimpleMarshallerSingleUseImpl(SimpleMarshallerMappings.createFrom(MarshallerMappingsSearch.inPackages("r01m.model")));
 * 		</pre>
 * 		O mas fácil aún:
 * 		<pre class='brush:java'>
 * 			Marshaller marshaller = SimpleMarshaller.createForPackages(""r01m.model")
 * 													.getForSingleUse();
 * 		</pre>
 *
 * [Uso del Marshaller]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
 * ----------------------------------------------------------------------------
 * [Paso 1]: Cargar el mapeo de clases
 * 			 En el caso del SimpleMarshaller hay dos opciones:
 * 				1.- Cargar el mapeo de clases a partir de anotaciones de las clases java a marshallear
 * 					<pre class='brush:java'>
 * 						marshaller.addBeans(MyBean.class);
 * 					</pre>
 * 			    2.- Cargar el mapeo de clases a partir de un fichero XML con la definición (obsoleto)
 * 					<pre class='brush:java'>
 * 						marshaller.addBeans("/mappings/myBeansMappings.xml"
 * 					</pre>
 *
 * [Paso 2]: Marshalling / UnMarshalling
 * 			 Paso de XML a objetos
 * 				<pre class='brush:java'>
 * 					MyBean myBeanInstance = marshaller.beanFromXml(xml)
 * 				</pre>
 * 			 Paso de objetos a XML
 * 				<pre class='brush:java'>
 * 					String xml = marshaller.xmlFromBean(myBeanInstance)
 * 				</pre>
 */
public interface Marshaller
         extends Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
    public static final class MarshallerMappingsSearch {
    	public static Class<?>[] forTypes(final Class<?>... types) {
    		return types;
    	}
    	public static String[] inPackages(final String... packages) {
    		return packages;
    	}
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESO A LOS MAPPINGS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return los mappings del Marshaller
     */
    public abstract MarshallerMappings getMappings();
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE XMLs DE DEFINICIÓN
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Añade los mapeos del fichero cuya ruta (que tiene que ser accesible en el classPath)
	 * se pasa como parametro
	 * @param mapFilePath el path al fichero de mapeo
	 * @throws MarshallerException si el fichero de mapeo es incorrecto
	 */
	public abstract Marshaller addTypes(String mapFilePath) throws MarshallerException;

	/**
	 * Añade los mapeos del stream que se pasa como parametro
	 * @param mapIS el stream con los mapeos
	 * @throws MarshallerException si el fichero de mapeos es incorrecto
	 */
	public abstract Marshaller addTypes(InputStream mapsIS) throws MarshallerException;
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE BEANS ANOTADOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Añade los mapeos examinando las anotaciones de los beans que se pasan como parametro
     * NOTA:  Unicamente es necesario pasar los "beans principales" ya que automaticamente
     * 		  va recorriendo los beans "hijo" presentes como miembro o componentes de una
     * 		  colección / mapa
     * @param annotatedTypes beans "principales" independientes entre sí desde donde comenzar
     * 						 a buscar anotaciones para componer los mapeos
     * @throws MarshallerException si hay algún error al componer los mapeos
     */
    public abstract Marshaller addTypes(Class<?>... annotatedTypes) throws MarshallerException;
    /**
     * Añade los mapeos examinando las anotaciones de los beans contenidos en los paquetes
     * que se pasan como parámetro
     * @param packages los paquetes donde buscar tipos anotados
     * @throws MarshallerException si hay algún error al componer los mapeos
     */
    public abstract Marshaller addTypes(Package... packages) throws MarshallerException;
    /**
     * Carga los beans buscando tipos anotados según las especificaciones que se pasan
     * El uso habitual es:
     * <pre class='brush:java'>
     * 		addBeans(MarshallerMappingsSearch.forTypes(MyType.class,MyOtherType.class),
     * 				 MarshallerMappingsSearch.inPackages("com.a.b","com.c.d"));
     * </pre>
     */
    public Marshaller addTypes(Object... searchSpecs) throws MarshallerException;
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONFIGURACION
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Establece el encoder a utilizar
	 * @param encoder encoder
	 */
	public abstract Marshaller usingEncoder(TextEncoder encoder);

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONVERSION
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene un objeto a partir de un xml en forma de String
	 * @param objectXML El xml a convertir a objetos en forma de String
	 * @return El objeto creado por el marshaller que representa el fichero XML.
	 */
	public abstract <T> T beanFromXml(String beanXml) throws MarshallerException;
	/**
	 * Obtiene un objeto a partir de un xml en forma de String
	 * @param objectXML El xml a convertir a objetos en forma de String
	 * @return El objeto creado por el marshaller que representa el fichero XML.
	 */
	public abstract <T> T beanFromXml(CharSequence beanXml) throws MarshallerException;
	/**
	 * Obtiene un objeto a partir de un xml en forma de byte[]
	 * @param beanXml el xml a convertir a objetos en forma de byte[]
	 * @return
	 * @throws MarshallerException
	 */
	public abstract <T> T beanFromXml(byte[] beanXml) throws MarshallerException;
 	/**
	 * Obtiene un objeto a partir de un xml en forma de String
	 * @param objectXML El xml a convertir a objetos en forma de String
	 * @return El objeto creado por el marshaller que representa el fichero XML.
	 */
	public abstract <T> T beanFromXml(InputStream beanXmlIS) throws MarshallerException;

	/**
	 * Obtiene un objeto a partir de un objeto Node del DOM XML
	 * @param beanXmlNode el objeto Node del DOM XML
	 * @return el objeto creado por el marshaller que representa el ficheor XML
	 */
	public abstract <T> T beanFromXml(Node beanXmlNode) throws MarshallerException;

	/**
	 * Obtiene una cadena xml a partir de un objeto
	 * @param bean el objeto a partir del cual debe crearse el XML
	 * @return cadena que representa el XML creado por el marshaller.
	 */
	public abstract <T> String xmlFromBean(T bean) throws MarshallerException;
	/**
	 * Obtiene una cadena XML en el charset indicado a partir de un objeto
	 * @param bean el objeto a partir del cual debe crearse el XML
	 * @param charset charset en el que se codifica el XML
	 * @return cadena con el XML codificada en el Charset indicado
	 */
	public abstract <T> String xmlFromBean(T bean,
										   Charset charset);
}