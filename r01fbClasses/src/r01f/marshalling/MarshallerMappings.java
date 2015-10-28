package r01f.marshalling;

import java.io.InputStream;


/**
 * Carga la configuración de marshalling, bien desde clases anotadas, bien desde algún fichero de configuración
 * y en su caso custodia (guarda y cachea) esta configuración
 */
public interface MarshallerMappings {

///////////////////////////////////////////////////////////////////////////////////////////
//  CARGA DESDE FICHEROS ANOTACIONES
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Añade los mapeos del fichero cuya ruta (que tiene que ser accesible en el classPath)
	 * se pasa como parametro
	 * @param filePath el path al fichero de mapeo
	 * @throws MarshallerException si el fichero de mapeo es incorrecto
	 */
	public abstract void loadFromMappingDefFile(String filesPath) throws MarshallerException;
	/**
	 * Añade los mapeos desde el stream que se pasa como parametro
	 * @param is el stream con la definición del mapeo
	 * @throws MarshallerException si el mapeo es incorrecto o no se puede cargar
	 */
	public abstract void loadFromMappingDef(InputStream is) throws MarshallerException;

///////////////////////////////////////////////////////////////////////////////////////////
//  CARGA DESDE CLASES ANOTADAS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Carga el mapa de clases desde su definición en base a anotaciones en las propias clases java
	 * @param annotatedTypes tipo(s) desde los que empezar a buscar anotaciones
	 * 						 NOTA: 	No es necesario indicar TODOS los tipos sino únicamente el raíz (o raíces si se van a mapear distintos tipos raíz inconexos)
	 * @throws MarshallerException si el mapeo es incorrecto o no se puede cargar
	 */
	public abstract void loadFromAnnotatedTypes(Class<?>... annotatedTypes) throws MarshallerException;
	/**
	 * Carga el mapa de clases escaneando los paquetes en busca de clases anotadas
	 * @param packages loa paquetes que tienen clases anotadas
	 * @throws MarshallerException si el mapeo es incorrecto o no se puede cargar
	 */
	public abstract void loadFromAnnotatedTypesScanningPackages(Package... packages) throws MarshallerException;
	/**
	 * Carga el mapa de clases a partir de anotaciones que se buscan según las especificaciones
	 * que se pasan
     * La forma habitual de utilizarlo es.
     * <pre class='brush:java'>
     * 		loadFromAnnotatedTypes(MarshallerMappingsSearch.forTypes(MyType.class,MyOtherType.class),
     * 								 MarshallerMappingsSearch.forPackages("com.a.b","com.c.d"));
     * </pre>
	 * @param searchSpecs las especificaciones de búsqueda
	 * @throws MarshallerException si el mapeo es incorrecto o no se puede cargar
	 */
	public abstract void loadFromAnnotatedTypes(Object... searchSpecs) throws MarshallerException;
	/**
	 * Comprueba si entre los mapeos se encuentra un determinado tipo
	 * @param type tipo
	 * @return true si el tipo está cargado entre los mapeos
	 * @throws MarshallerException si hay algún error
	 */
	public abstract boolean containsType(Class<?> type) throws MarshallerException;
///////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve información de depuracion
	 * @return
	 */
	public String debugInfo();

}