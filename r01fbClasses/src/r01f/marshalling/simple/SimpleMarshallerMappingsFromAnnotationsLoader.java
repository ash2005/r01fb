package r01f.marshalling.simple;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.Sets;
import com.googlecode.gentyref.GenericTypeReflector;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsI18NBundleBacked;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.annotations.OidField;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.marshalling.annotations.XmlDateFormat;
import r01f.marshalling.annotations.XmlInline;
import r01f.marshalling.annotations.XmlReadTransformer;
import r01f.marshalling.annotations.XmlTypeDiscriminatorAttribute;
import r01f.marshalling.annotations.XmlWriteIgnoredIfEquals;
import r01f.marshalling.annotations.XmlWriteTransformer;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.reflection.Reflection;
import r01f.reflection.ReflectionUtils;
import r01f.reflection.ReflectionUtils.FieldAnnotated;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Clase que se encarga de obtener los mapeos de un bean a partir de anotaciones java
 * 
 * Utilización:
 * <pre class='brush:java'>
 * 		SimpleMarshallerMappingsFromAnnotationsLoader loader = new SimpleMarshallerMappingsFromAnnotationsLoader(MyType.class,
 * 																												 MyOterType.class,
 * 																												 ...);
 * 		Map<String,BeanMap> mappings = loader.getLoadedBeans();
 * </pre>
 * Otra forma de utilizarlo:
 * <pre class='brush:java'>
 * 		SimpleMarshallerMappingsFromAnnotationsLoader loader = new SimpleMarshallerMappingsFromAnnotationsLoader(MarshallerMappingsSearch.forTypes(MyType.class,
 * 																												 								   MyOterType.class,
 * 																												 								   ...),
 * 																												 MarshallerMappingsSearch.forPackages("com.a.b",
 * 																																					  "c.d.e",
 * 																																					  ...));
 * 		Map<String,BeanMap> mappings = loader.getLoadedBeans();
 * </pre>
 * 
 * Guia de anotación de clases:
 * [1] Para indicar el tag xml que "engloba" una clase, utilizar la anotación {@link XmlRootElement}
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			...
 *     		}
 *     </pre>
 * ------------------------------------------------------------------------------------------------------------------
 * [2] Para indicar que un miembro ha de mapearse en un atributo del tag xml que "engloba" el tipo
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlAttribute(name="myAttr")
 *     			private String myAttrField;
 *     		}
 *     </pre> 
 * ------------------------------------------------------------------------------------------------------------------
 * [3] Para indicar que un miembro ha de mapearse en un elemento del tag xml que "engloba" el tipo
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlElement(name="myEl")
 *     			private String myElementField;
 *     		}
 *     </pre>
 * ------------------------------------------------------------------------------------------------------------------
 * [4] Para que un miembro se mapee DENTRO del tag xml que "engloba" el tipo
 *     <pre class='brush:java'>
 * 			@XmlRootElement(name="myType")
 * 			public class MyType {
 * 				@XmlValue
 * 				private String myElementField;
 * 			}
 *     </pre>
 * ------------------------------------------------------------------------------------------------------------------
 * [5] Para que un miembro se mapee como un tag XML CDATA
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlElement(name="myCDATAEl") @XmlCDATA
 *     			private String myElementField;
 *     		}
 *     </pre>
 * ------------------------------------------------------------------------------------------------------------------
 * [6] Para NO serializar un field de xml<->java
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlTransient
 *     			private int notSerializedField;
 *     			@XmlElement(name="myCDATAEl") @XmlCDATA
 *     			private String myElementField;
 *     		}
 *     </pre>  
 * ------------------------------------------------------------------------------------------------------------------
 * [7] Se detectan los tipos inmutables y se invoca al constructor correcto
 * 	   En este caso es IMPORTANTE tener en cuenta que para poder crear un tipo inmutable es necesario
 * 	   tener el valor de todos los miembros finales en el momento de la construcción; por esta razón
 * 	   es importante tener en cuenta que TODOS los miembros finales deben ser mapeados en ATRIBUTOS de
 * 	   elemento XML
 *     Por ejemplo, el siguiente xml:
 *     <pre class='brush:xml'>
 *     		<myType myInmutableAttr='attrValue'/>
 *     </pre>
 *     se puede mapear:
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlAttribute(name="myInmutableAttr")
 *     			private MyInmutableType myElementInmutableField;
 *     		}
 *     		@RequiredArgsConstructor
 *     		public class MyInmutableType {
 *     			@XmlValue
 *     			private final String _value;
 *     		}
 *     </pre>
 *     Si por el contrario, el objeto inmutable el un elemento:
 *     <pre class='brush:xml'>
 *     		<myType>
 *     			<myInmutableEl finalField='finalFieldValue'>
 *     				<nonFinalField>elValue<nonFinalField>
 *     			</myInmutableEl>
 *     		<myType>
 *     </pre>
 *     se puede mapear:
 *     <pre class='brush:java'>
 *     		@XmlRootElement(name="myType")
 *     		public class MyType {
 *     			@XmlElement(name="myInmutableEl")
 *     			private MyInmutableType myElementInmutableField;
 *     		}
 *     		@RequiredArgsConstructor
 *     		public class MyInmutableType {
 *     			@XmlAttribute(name="finalField")
 *     			private final String _finalField;
 *     			@XmlElement(name="nonFinalField")
 *     			private final String _nonFinalField;
 *     		}
 *     </pre> 	
 * ------------------------------------------------------------------------------------------------------------------
 * [8] Colecciones (mapas y listas)
 * 	   El nombre del tag de cada elemento de la colección se toma de:
 * 			a) la anotación @XmlElement del miembro tipo colección
 * 			b) la anotación @XmlRoot del tipo de los elementos de la colección
 * 
 * 	   Para "envolver" a los items de la colección hay dos casos:
 * 	   CASO 1: Colecciones donde los elementos (item) NO están "envueltas" en un tag
 * 		<pre class='brush:xml'>
 * 				<myType>
 * 					<myChildType attr='attr1'/>
 * 					<myChildType attr='attr2'/>
 * 				<myType>
 * 		</pre>
 * 		<pre class='brush:java'>
 *     		public class MyType {
 *     			private Collection<MyChildType> myColField;
 *     		}
 *     		@XmlRoot(name="myChildType")
 *     		public class MyChildType {
 *     			@XmlAttribute(name="attr")
 *     			private String _attrField;
 *     		}
 * 		</pre> 
 * 		CASO 2: Colecciones donde los elementos (item) están "envueltos" en un tag
 * 		<pre class='brush:xml'>
 * 				<myType>
 * 					<myChilds>
 * 						<myChildType attr='attr1'/>
 * 						<myChildType attr='attr2'/>
 * 					</myChilds>
 * 				<myType>
 * 		</pre>
 * 		<pre class='brush:java'>
 *     		public class MyType {
 *     			@XmlElelmentWrapper(name="myChilds")
 *     			private Collection<MyChildType> myColField;
 *     		}
 *     		@XmlRoot(name="myChildType")
 *     		public class MyChildType {
 *     			@XmlAttribute(name="attr")
 *     			private String _attrField;
 *     		}
 * 		</pre>
 *  ------------------------------------------------------------------------------------------------------------------
 * [9] Mapas
 *     Para poder hacer marshalling de un mapa, es necesario especificar cual es el miembro que hace de oid en el tipo
 *     de los objetos que contiene, es decir, por qué miembro se va a indexar en el mapa
 *     Para ello, basta con poner la anotación @OidField en dicho miembro
 * 		<pre class='brush:xml'>
 * 				<myType>
 * 					<myChilds>
 * 						<myChildType attr='attr1'>Value1</myChildType>
 * 						<myChildType attr='attr2'>Value2</myChildType>
 * 					</myChilds>
 * 				<myType>
 * 		</pre>
 * 		<pre class='brush:java'>
 *     		public class MyType {
 *     			@XmlElelmentWrapper(name="myChilds")
 *     			private Map<String,MyChildType> myMapField;
 *     		}
 *     		@XmlRoot(name="myChildType")
 *     		public class MyChildType {
 *     			@XmlAttribute(name="attr") @OidField
 *     			private String _attrField;
 *     			@XmlValue
 *     			private String _valField;
 *     		}
 * 		</pre>
 *  ------------------------------------------------------------------------------------------------------------------
 *  [11] Serializar / deserializar objetos colecciones (Mapas, Listas o Sets a primer nivel)
 *  	 Por ejemplo, si se tiene un xml que ha de mapearse en un objeto Map como:
 *  	 <pre class='brush:xml'>
 *  		<myMap>
 *  			<MyType oid='1'>uno</MyType>
 *  			<MyType oid='2'>dos</MyType>
 *  		</myMap>
 *  	 </pre>
 *  	 Hay que crear un tipo que extienda o implemente el interfaz Map y que esté anotado
 *  	 <pre class='brush:java'>
 *  		// Extendiendo...
 *			@XmlRootElement(name="myMap")
 *			private static class MyMap
 *		           extends HashMap<String,MyType> {
 *			}
 *			// Delegando...
 *			@XmlRootElement(name="myMapDelegated")
 *			private static class MyMapDelegated
 *		     		  implements Map<String,MyType> {
 *				@Delegate @XmlTransient
 *				private final Map<String,MyType> _mapDelegate = new HashMap<String,MyType>();
 *			}
 *  	 </pre>
 *  
 *  	Si NO se quiere especificar el nombre del tag que envuelve el Mapa o Colección, 
 *  	por defecto se asigna "map" a un Map y "collection" a un List o Set
 *  	En este caso NO es necesario crear ningún tipo
 *  	 
 *  ------------------------------------------------------------------------------------------------------------------
 *  [11] Custom Marshalling
 *  	 En ocasiones puede ser necesario que determinados tipos se transformen xml<->java "a mano".
 *  	 Para esto, basta con anotar el tipo con @XmlReadTransformer y @XmlWriteTransformer
 *  	 e implemntar dos clases:
 *  	 <pre>
 *  		- Una que implementa el interfaz {@link r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlReadTransformer}
 *  	      para pasar de XML a Java 
 *  		- Otra que implementa el interfaz {@link r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlWriteTransformer}
 *  		  para pasar de Java a XML
 *  	 </pre>
 * 		 <pre class='brush:java'>
 *     		public class MyType {
 *     			private MyCustomTransformedType myCustomTransformedField;
 *     		}
 *     		@XmlReadTransformer(using=MyCustomReadTransformer.class) @XmlWriteTransformer(using=MyCustomWriteTransformer.class)
 *     		public class MyCustomTransformedType {
 *     			...
 *     		}
 * 		 </pre>
 * ------------------------------------------------------------------------------------------------------------------
 * [12] Miembros definidos por interfaces o clases base (abstractas)
 * 		Ej: Se tiene:
 * 		<pre class='brush:java'>
 * 			// Interface
 * 			public interface MyInterface {
 * 			}
 * 			// Implementacion A del interface
 * 			@XmlRootElement(name="typeA")
 * 			public class MyInterfaceTypeA implements MyInterface {
 * 			}
 * 			// Implementacion B del interface
 * 			@XmlRootElement(name="typeB")
 * 			public class MyInterfaceTypeA implements MyInterface {
 * 			}
 * 		</pre>
 * 		Si en una clase se tiene un miembro definido con el interface, en principio en el XML puede venir cualquiera
 * 		de los dos tipos de objetos MyInterfaceTypeA o MyInterfaceTypeB
 * 		<pre class='brush:java'>
 * 			@XmlRootElement(name="mytType")
 * 			public class MyType {
 * 				@Getter @Setter private MyInterface myField;
 * 			}
 * 		</pre>
 * 		Hay DOS posiblidades para mapear estos casos:
 * 		CASO 1: En el XML cada tipo tiene un nodo 
 * 				<pre class='brush:xml'>
 * 					<myType>
 * 						<typeA>
 * 							...
 *						</typeA>
 * 					</myType>
 * 				</pre>
 * 				o bien:
 * 				<pre class='brush:xml'>
 * 					<myType>
 * 						<typeB>
 * 							...
 *						</typeB>
 * 					</myType>
 * 				</pre>
 * 				En este caso basta con NO indicar explicitamente el nombre del nodo XML que engloba el objeto
 * 				<pre class='brush:java'>
 * 					@XmlRootElement(name="mytType")
 * 					public class MyType {
 * 						@XmlElement		// <-- NO se indica el atributo NAME
 * 						@Getter @Setter private MyInterface myField;
 * 					}
 *				</pre>
 *		CASO 2: En el XML los dos tipos van a llegar con el mismo nombre de nodo:
 * 				En este caso se indica explicitamente el nombre del nodo XML que engloba el objeto
 * 				<pre class='brush:java'>
 * 					@XmlRootElement(name="mytType")
 * 					public class MyType {
 * 						@XmlElement(name="myField")		// <-- SI se indica el atributo NAME
 * 						@Getter @Setter private MyInterface myField;
 * 					}
 *				</pre>
 *				y el XML en AMBOS casos será:
  * 			<pre class='brush:xml'>
 * 					<myType>
 * 						<myField>		<!-- aqui llega cualquiera de los dos tipos -->
 * 							...
 *						</myField>
 * 					</myType>
 * 				</pre>
 * 				Es necesario dar alguna "pista" al mapeador de qué tipo debe instanciar; para esto se utiliza el atributo XmlTypeDiscriminatorAttribute
 * 				<pre class='brush:java'>
 * 					@XmlRootElement(name="mytType")
 * 					public class MyType {
 * 						@XmlElement(name="myField") @XmlTypeDiscriminatorAttribute(name="type")		// <-- SI se indica el atributo NAME
 * 						@Getter @Setter private MyInterface myField;
 * 					}
 *				</pre>
 *				Con esto, el XML generado será:
  * 			<pre class='brush:xml'>
 * 					<myType>
 * 						<myField type='typeA'>
 * 							...
 *						</myField>
 * 					</myType>
 * 				</pre>
 * 				o bien:
 * 				<pre class='brush:xml'>
 * 					<myType>
 * 						<myField type='typeA'>
 * 							...
 *						</myField>
 * 					</myType>
 * 				</pre>
 * 				Es importante tener en cuenta que el valor del atributo discriminador (atributo type en el ejemplo) es el valor de la notación @XmlRootElement
 * 				de MyInterfaceTypeA y MyInterfaceTypeB
 * ------------------------------------------------------------------------------------------------------------------
 * [13] Valores que NO se quiere que aparezcan en el xml
 * 	 	En muchas ocasiones, especialmente con números o booleans, NO se quiere que en el XML serializado aparezca
 * 		un elemento o atributo si el valor del campo es el valor por defecto del tipo (ej: false en un boolean)
 * 		Ej: si se serializa el siguiente tipo a XML:
 * 		<pre class='brush:java'>
 * 			@XmlRootElement(name="myType")
 * 			@Accessors(prefix="_")
 * 			public class MyType {
 * 				@XmlAttribute(name="myField")  
 * 				@Getter @Setter private boolean _myField;
 * 			}
 * 		</pre> 
 * 		el resultado será:
 * 		<pre class='brush:xml'>
 * 			<myType myField='false'/>
 * 		</pre>
 * 		Sin embargo, es posible que NO se quiera que aparezca el atributo myField si el valor es false (valor por defecto para un boolean)
 * 		En este caso, basta con anotar el field _myField con @XmlWriteIgnoreIfEquals(value="false")
 * 		<pre class='brush:java'>
 * 			@XmlRootElement(name="myType")
 * 			@Accessors(prefix="_")
 * 			public class MyType {
 * 				@XmlAttribute(name="myField")  @XmlWriteIgnoreIfEquals(value="false")
 * 				@Getter @Setter private boolean _myField;
 * 			}
 * 		</pre> 
 */
@Accessors(prefix="_")
@Slf4j
public class SimpleMarshallerMappingsFromAnnotationsLoader {
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////    
    @Getter private Map<String,BeanMap> _loadedBeans = new HashMap<String,BeanMap>();    // Mapa que contiene las clases relacionadas con su nombre
    		private Set<Class<?>> _types;		// Tipos en los que buscar anotaciones
    		private Set<String> _packages;		// Paquetes en los que buscar anotaciones
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor a partir de un array de tipos, un array de paquetes o ambos
     * La forma habitual de utilizarlo es.
     * <pre class='brush:java'>
     * 		SimpleMarshallerMappingsFromAnnotationsLoader(MarshallerMappingsSearch.forTypes(MyType.class,MyOtherType.class),
     * 													  MarshallerMappingsSearch.forPackages("com.a.b","com.c.d"));
     * </pre>
     * @param searchSpecs
     */
    public SimpleMarshallerMappingsFromAnnotationsLoader(Object... searchSpecs) {
    	if (CollectionUtils.isNullOrEmpty(searchSpecs)) return;
    	
    	// [0] Cargar los tipos y paquetes
    	for (Object spec : searchSpecs) {
    		if (CollectionUtils.isArray(spec.getClass())) { 
    		    if ( !(spec.getClass().getComponentType() == Class.class || spec.getClass().getComponentType() == String.class) ) throw new IllegalArgumentException("The SimpleMarshallerMappingsFromAnnotationsLoader constructor only accepts a types array or a packages array that are used to find annotated types");
    		    
    		    if (spec.getClass().getComponentType() == Class.class) {
    		    	if (_types == null) _types = Sets.newLinkedHashSet();
    		    	for (Class<?> type : (Class<?>[])spec) _types.add(type);
    		    	
    		    } else if (spec.getClass().getComponentType() == String.class) {
    		    	if (_packages == null) _packages = Sets.newLinkedHashSet();
    		    	for (String pckg : (String[])spec) _packages.add(pckg);
    		    }
    		    
    		} else {
    			if ( !(spec.getClass() == Class.class || spec.getClass() == String.class)) throw new IllegalArgumentException("The SimpleMarshallerMappingsFromAnnotationsLoader constructor only accepts a types or a packages that are used to find annotated types");
    			
	    		if (spec.getClass() == Class.class) {
	    			if (_types == null) _types = Sets.newLinkedHashSet();
	    			_types.add((Class<?>)spec);
	    			
	    		} else if (spec.getClass() == String.class) {
	    			if (_packages == null) _packages = Sets.newLinkedHashSet();
	    			_packages.add((String)spec);
	    		}
    		}
    	}
    	// [1] Procesar paquetes y tipos
    	_processTypesAndPackages();
    }
    /**
     * Constructor en base a los tipos
     * @param types tipos cuyas anotaciones hay que procesar
     */
    public SimpleMarshallerMappingsFromAnnotationsLoader(Class<?>... types) throws MarshallerException {
    	if (CollectionUtils.isNullOrEmpty(types)) return;
    
    	// [0] Cargar los tipos
    	_types = Sets.newLinkedHashSetWithExpectedSize(types.length);
    	for(Class<?> t : types) _types.add(t);
    	
    	// [1] Procesar paquetes y tipos
    	_processTypesAndPackages();
    }
    /**
     * Constructor en base a los paquetes
     * @param packages paquetes cuyos tipos hay que procesar
     */
    public SimpleMarshallerMappingsFromAnnotationsLoader(Package... packages) throws MarshallerException {
    	if (CollectionUtils.isNullOrEmpty(packages)) return;
    	
    	// [0] Cargar los paquetes
    	_packages = Sets.newLinkedHashSetWithExpectedSize(packages.length);
    	for (Package p : packages) {
    		_packages.add(p.getName());
    	}
    	// [1] Procesar paquetes y tipos
    	_processTypesAndPackages();
    }
    private void _processTypesAndPackages() {
    	// [1] Procesar los tipos
    	_processTypes(_types);
    	
    	// [2] Procesar los paquetes
    	_processPackages(_packages);
    	
    	// [3] Terminar de configurar cosas pendientes
		// Conecta el objeto {@link BeanMap} en los miembros {@link FieldMap} de tipo objeto o colección
		// Este proceso hay que hacerlo DESPUES de cargar todos los beans 
    	SimpleMarshallerMappings.connectBeanMappings(_loadedBeans);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTROSPECCION DE LAS CLASES EN BASE A LAS ANOTACIONES
///////////////////////////////////////////////////////////////////////////////////////////
    private void _processTypes(final Set<Class<?>> types) {
    	if (CollectionUtils.hasData(types)) {
	    	for (Class<?> type : types) {
	    		if (type.getAnnotation(XmlRootElement.class) == null) continue;	// sometimes if the type is a subtype of another annotated with @XmlRootElement is detected as annotated
	    		
	    		// Abstract types MUST not be annotated with @XmlRootElement
	    		if (Modifier.isAbstract(type.getModifiers())) throw new MarshallerException(Throwables.message("Abstract types MUST NOT be annotated with @{}: {}",
	    																									   XmlRootElement.class,type));
	    		_beanMapFromBeanAnnotations(type);
	    	}
    	}
    }
    private void _processPackages(final Set<String> packages) {
    	if (CollectionUtils.hasData(_packages)) {
    		// [1] - Find all types annotated with @XmlRootElement 
    		Set<Class<?>> allPckgsTypes = Sets.newHashSet(); 
    		
			List<URL> urls = new ArrayList<URL>();
			//urls.addAll(ClasspathHelper.forPackage("javax.xml.bind.annotation"));
    		for (String p : packages) {
    			//Reflections typeScanner = new Reflections(p);
				urls.addAll(ClasspathHelper.forPackage(p));	// see https://code.google.com/p/reflections/issues/detail?id=53
				log.debug("Scanning package {} for @XmlRootElement annotated types",p);
    		}
			Reflections typeScanner = new Reflections(new ConfigurationBuilder()
																.setUrls(urls));    				
			Set<Class<?>> pckgTypes = typeScanner.getTypesAnnotatedWith(XmlRootElement.class);
			if (CollectionUtils.hasData(pckgTypes)) {
					for (Class<?> type : pckgTypes) log.trace(">Type {}",type);
				allPckgsTypes.addAll(pckgTypes);
			} else {
				log.debug("NO types annotated with @XmlRootElement");
			}
    		// [2] - Process...
    		_processTypes(allPckgsTypes);
    	}
    }
	/**
	 * Obtiene el mapeo de un bean inspeccionando las anotaciones
	 * @param bean el bean
	 * @return
	 */
	private void _beanMapFromBeanAnnotations(final Class<?> type) throws MarshallerException {
		log.trace("... processing annotations from type {}",type.getName());
		BeanMap beanMap = null;
		
		// CASO 1: (NO habitual) Beans transformados xml<->java de forma customizada
		beanMap = _beanMapFromCustomXmlTransformers(type);
		// CASO 2: (habitual) 	 Beans transformados xml<->java en base a las anotaciones
		if (beanMap == null) beanMap = new BeanMap(_typeNormalizedDesc(type));	//type.getName());
	
		// Definición de la clase: obtener el nombre del tag xml y la forma de acceso
		XmlRootElement rootAnnot = ReflectionUtils.typeAnnotation(type,
																  XmlRootElement.class);
		String beanNodeName = _nodeNameFromAnnotation((rootAnnot != null ? rootAnnot.name() : null),				// nombre indicado en la anotación o null
												      ReflectionUtils.classNameFromClassNameIncludingPackage(type.getName()));		// nombre por defecto = nombre de la clase
		beanMap.getXmlMap().setNodeName(beanNodeName);
		
		// CASO 1::::::::::::::::: en los beans procesados via CustomTransformer NO hay que procesar los fields
		if (beanMap.isCustomXmlTransformed()) return;
		
		// CASO 2::::::::::::::::: procesar los fields
		// Poner el nuevo bean en la salida...
		_loadedBeans.put(type.getName(),beanMap);
		
		XmlAccessorType accessorType = ReflectionUtils.typeAnnotation(type,XmlAccessorType.class);
		if (accessorType != null) beanMap.setUseAccessors(accessorType.value() == XmlAccessType.PROPERTY);
		
		// [Elemento XMLValue]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		FieldAnnotated<XmlValue>[] xmlValueFields = ReflectionUtils.fieldsAnnotated(type,XmlValue.class);
		if (xmlValueFields != null) {
			for (FieldAnnotated<XmlValue> fAnnot : xmlValueFields) {
				// Asegurarse de que NO es transient
				if (fAnnot.getField().getAnnotation(XmlTransient.class) != null) throw new MarshallerException("La anotacion @XmlTransient NO puede aparecer junto a @XmlValue. Revisa el campo " + fAnnot.getField().getName() + " de la clase " + type.getName());
				
				// Asegurarse de que NO está anotado con XmlAttribute, XmlElement o XmlElementWrapper
				if (fAnnot.getField().getAnnotation(XmlAttribute.class) != null
				 || fAnnot.getField().getAnnotation(XmlElement.class) != null
				 || fAnnot.getField().getAnnotation(XmlElementWrapper.class) != null) throw new MarshallerException("La anotación @XmlValue NO puede aparecer junto a @XmlAttribute, @XmlElement o @XmlElementWrapper. Revisa el campo " + fAnnot.getField().getName() + " de la clase " + type.getName());
				
				
				log.trace("\t\t-field {}",fAnnot.getField().getName());
				
				// Crear el FieldMap
				FieldMap fieldMap = _fieldMapFromField(type,fAnnot.getField(),
													   false);		// not a xml attribute
				fieldMap.getXmlMap().setAttribute(false);
				fieldMap.setFinal(Modifier.isFinal(fAnnot.getField().getModifiers()));		// es final?
				
				// El nombre del nodo coincide con el nombre del nodo asignado a la clase (anotación @XmlRootElement)
				String fieldNodeName = beanNodeName;
				fieldMap.getXmlMap().setNodeName(fieldNodeName);
				
				// Asegurarse de que ningún otro miembro está anotado con XmlValue
				if (beanMap.getFieldFromXmlNode(fieldNodeName,false) != null) throw new MarshallerException("El field " + fAnnot.getField().getName() + " del bean " + beanMap.getTypeName() + " está anotado con @XmlValue PERO hay otro field también anotado con @XmlValue en el mismo bean!!");
				
//				if (fieldMap.getDataType().isCollection() || fieldMap.getDataType().isMap()) {
//					fieldMap.getXmlMap().setExplicitNodeName(false);
//					fieldMap.getXmlMap().setExplicitColElsNodeName(false);
//				}
				
				// Si el miembro es de un tipo complejo o una colección, hay que hacer una llamada recursiva para mapearlo
				_recursiveMapBean(fieldMap.getDataType());
				
				// añadirlo al bean padre
				beanMap.addField(fieldMap);
			}
		}
		
		
		// [Atributos XML]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		FieldAnnotated<XmlAttribute>[] xmlAttrFields = ReflectionUtils.fieldsAnnotated(type,XmlAttribute.class);
		if (xmlAttrFields != null) {
			for (FieldAnnotated<XmlAttribute> fAnnot : xmlAttrFields) {
				// Asegurarse de que NO es transient
				if (fAnnot.getField().getAnnotation(XmlTransient.class) != null) throw new MarshallerException("La anotacion @XmlTransient NO puede aparecer junto a @XmlAttribute. Revisa el campo " + fAnnot.getField().getName() + " de la clase " + type.getName());
				
				log.trace("\t\t-field {}",fAnnot.getField().getName());
				
				// Crear el fieldMap
				FieldMap fieldMap = _fieldMapFromField(type,fAnnot.getField(),
													   true);						// a xml attribute
				fieldMap.getXmlMap().setAttribute(true);							// es atributo
				fieldMap.setFinal(Modifier.isFinal(fAnnot.getField().getModifiers()));		// es final?
				
				// Obtener el nombre del nodo
				XmlAttribute xmlAttrAnnot = fAnnot.getAnnotation();
				String fieldNodeName = _nodeNameFromAnnotation((xmlAttrAnnot != null ? xmlAttrAnnot.name() : null),		// nombre indicado en la anotación o null
								  				  	   		   fieldMap.getName());										// nombre por defecto = nombre del miembro
				fieldMap.getXmlMap().setNodeName(fieldNodeName);
				if (xmlAttrAnnot != null) fieldMap.getXmlMap().setExplicitNodeName(!_isXmlAttributeAnnotationDefaultValue(xmlAttrAnnot));	// true si el nombre del nodo xml se ha dado explicitamente con una anotación, false si se ha calculado como el nombre del miembro
				
				// añadirlo al bean padre
				beanMap.addField(fieldMap);
			
				// Si el tipo de dato del atributo es un objeto, ver si se puede mapear con un XmlCustomTransformer
				if (fieldMap.getDataType().isObject()) _beanMapFromCustomXmlTransformers(fieldMap.getDataType().getType());
				
				// Si el miembro es de un tipo complejo o una colección, hay que hacer una llamada recursiva para mapearlo
				_recursiveMapBean(fieldMap.getDataType());
			}
		}
		
		
		// [Elementos XML]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		// - CASO 1: Anotados con @XmlElement
		//				- Objetos en los que se especifica el nombre del nodo que "envuelve" el objeto
		//				- Colecciones / Mapas en las que se especifica el nombre del nodo que "envuelve" los elementos
		//				  NOTA: En este caso pueden o no estar también anotados con @XmlElementWrapper así que hay que tenerlo en cuenta para el CASO 2)
		FieldAnnotated<XmlElement>[] xmlElFields = ReflectionUtils.fieldsAnnotated(type,XmlElement.class);
		if (xmlElFields != null) {
			for (FieldAnnotated<XmlElement> fAnnot : xmlElFields) {
				// Asegurarse de que NO es transient
				if (fAnnot.getField().getAnnotation(XmlTransient.class) != null) throw new MarshallerException("La anotacion @XmlTransient NO puede aparecer junto a @XmlElement. Revisa el campo " + fAnnot.getField().getName() + " de la clase " + type.getName());
				
				log.trace("\t\t-field {}",fAnnot.getField().getName());
				
				// Crear el fieldMap
				FieldMap fieldMap = _fieldMapFromField(type,fAnnot.getField(),
													   false);		// not a xml attribute
				fieldMap.getXmlMap().setAttribute(false);
				
				// Nombre del nodo que engloba el miembro y sus elementos
				//	- si se trata de un objeto "simple" el nombre del tag se indica en la anotación @XmlElement
				//	- si se trata de una colección / mapa:
				//		* el nombre del tag que "engloba" cada objeto de la colección / mapa se toma de la anotación @XmlElement
				//		* el nombre del tag que "engloba" la colección / mapa puede ser:
				//			a.- Lo que se especifique en la anotación @XmlElementWrapper
				//			b.- El nombre del tag que "engloba" el objeto que contiene el miembro colección / mapa indicado en su
				//				anotación @XmlRootElement
				XmlElement xmlElAnnot = fAnnot.getAnnotation();
				if (fieldMap.getDataType().isCollection() 
				 || fieldMap.getDataType().isMap()) {
					
					XmlElementWrapper xmlElWrapAnnot = fAnnot.getField().getAnnotation(XmlElementWrapper.class);
					// Colección / Mapa
					String wrapperNodeName = xmlElWrapAnnot != null ? xmlElWrapAnnot.name() : beanNodeName;					// Por defecto el nombre del nodo que engloba al bean
					String elementsNodeName = _nodeNameFromAnnotation((xmlElAnnot != null ? xmlElAnnot.name() : null),
																	  fieldMap.getName());
					fieldMap.getXmlMap().setNodeName(wrapperNodeName);
					if (xmlElWrapAnnot != null) fieldMap.getXmlMap().setExplicitNodeName(!_isXmlElementWrapperAnnotationDefaultValue(xmlElWrapAnnot));	// true si el nombre del nodo xml se ha dado explicitamente con una anotación, false si se ha calculado como el nombre del miembro
					fieldMap.getXmlMap().setColElsNodeName(elementsNodeName);
					if (xmlElAnnot != null) fieldMap.getXmlMap().setExplicitColElsNodeName(!_isXmlElementAnnotationDefaultValue(xmlElAnnot));					// true si el nombre del nodo xml se ha dado explicitamente con una anotación, false si se ha calculado como el nombre del miembro
					
					// Si se fija el nombre de los elementos de la colección y estos NO son instanciables, hay que incluir un atributo
					// discriminador del tipo
					if (fieldMap.getXmlMap().isExplicitColElsNodeName() && fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() == null 
					&& ( (fieldMap.getDataType().isMap() && !fieldMap.getDataType().asMap().getValueElementsDataType().isInstanciable()) 
					  || (fieldMap.getDataType().isCollection() && !fieldMap.getDataType().asCollection().getValueElementsDataType().isInstanciable()) )) {
						throw new MarshallerException("El miembro " + fAnnot.getField().getName() + " del tipo " + type.getName() + " es una colección / mapa de elementos definidos con un interfaz; SI se anota con @XmlElement indicando un nombre para cada item mediante el atributo 'name', es necesario que el XML contenga un atributo discriminador del tipo que permita pasar de XML a java, para lo cual hay que utilizar la anotación @XmlTypeDiscriminatorAttribute; alternativamente se puede quitar el atributo 'name' de la anotacion @XmlElement de forma que cada item estará englobado por el tag correspondiente a su tipo concreto");
					}
					
					// Si el nodo que envuelve al elemento coincide con el nodo que envuelve al tipo, lanzar un error ya que se debería anotar con @XmlValue
					if (xmlElWrapAnnot != null && wrapperNodeName.equals(beanNodeName)) throw new MarshallerException("La anotacion @XmlElementWrapper establece que el nodo que envuelve a la coleccion " + fAnnot.getField().getName() + " de la clase " + type.getName() + " coincide con el nombre del nodo que envuelve la clase " + type.getName() + "; Utiliza la anotación @XmlValue si quieres que los elementos de la colección cuelguen del nodo que envuelve la clase " + type.getName());
					
				} else {
					// Simple object 
					String fieldNodeName = _nodeNameFromAnnotation((xmlElAnnot != null ? xmlElAnnot.name() : null),		// nombre indicado en la anotación o null
											 					   fieldMap.getName());									// nombre por defecto = nombre del miembro
					// TODO this IF is NOT totally tested! and it's related with a change at XMLFromObjsBuilder (line 427)
					// If the XmlElement annotation do not set an explicit node name AND the field data type is NOT instanciable AND NO type discriminator was set, 
					// set the node name null 
//					if (!fieldMap.getXmlMap().isExplicitNodeName() && !fieldMap.getDataType().isInstanciable() && fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() == null) {
//						fieldNodeName = null;
//					}
					fieldMap.getXmlMap().setNodeName(fieldNodeName);
					if (xmlElAnnot != null) fieldMap.getXmlMap().setExplicitNodeName(!_isXmlElementAnnotationDefaultValue(xmlElAnnot));		// true si el nombre del nodo xml se ha dado explicitamente con una anotación, false si se ha calculado como el nombre del miembro
				}
				
				// añadirlo al bean padre
				beanMap.addField(fieldMap);
				
				// Si el miembro es de un tipo complejo o una colección, hay que hacer una llamada recursiva para mapearlo
				_recursiveMapBean(fieldMap.getDataType());
			}
		}
		// - CASO 2: Colecciones / mapas anotadas con @XmlElementWrapper donde NO se especifica el nombre del nodo que "envuelve" a cada uno 
		//		     de los elementos, es decir, NO se indica la anotación @XmlElement y por lo tanto NO han sido procesados en el caso 1
		//			 NOTA: Hay que eliminar aquellos miembros procesados en el caso 1
		FieldAnnotated<XmlElementWrapper>[] xmlWrappedFields = ReflectionUtils.fieldsAnnotated(type,XmlElementWrapper.class);
		if (xmlWrappedFields != null) {
			for (FieldAnnotated<XmlElementWrapper> fAnnot : xmlWrappedFields) {
				// Asegurarse de que NO es transient
				if (fAnnot.getField().getAnnotation(XmlTransient.class) != null) throw new MarshallerException("La anotacion @XmlTransient NO puede aparecer junto a @XmlElementWrapper. Revisa el miembro " + fAnnot.getField().getName() + " de la clase " + type.getName());  
				
				// puede ser que el field YA esté procesado en el CASO 1
				String fieldName = _fieldName(fAnnot.getField());
				if (beanMap.getField(fieldName) != null) continue;
				
				log.trace("\t\t-field {}",fAnnot.getField().getName());
				
				// Crear el fieldMap
				FieldMap fieldMap = _fieldMapFromField(type,fAnnot.getField(),
													   false);		// not a xml attribute
				fieldMap.getXmlMap().setAttribute(false);
				
				// Asegurarse de que es un mapa / colección
				if (!(fieldMap.getDataType().isCollection() || fieldMap.getDataType().isMap())) throw new MarshallerException("Se ha utilizado la anotación @XmlElementWrapper en el miembro " + fAnnot.getField().getName() + " del tipo " + type.getName() + " pero este miembro NO es una colección o mapa; @XmlElementWrapper SOLO se puede utilizar en miembros tipo Map/Set/Collection...");
				
				// Nombre del nodo que envuelve a los elementos
				// NOTA: 	el nombre del nodo que envuelve a cada elemento NO se conoce ya que NO se indica la anotación @XmlElement y se 
				//  		tomará el nombre indicado con la anotación @XmlRootElement de la clase del objeto de la colección / mapa
				XmlElementWrapper xmlElWrapAnnot = fAnnot.getAnnotation();
				String wrapperNodeName = _nodeNameFromAnnotation((xmlElWrapAnnot != null ? xmlElWrapAnnot.name() : null),
																 fieldMap.getName()); 
				fieldMap.getXmlMap().setNodeName(wrapperNodeName);
				if (xmlElWrapAnnot != null) fieldMap.getXmlMap().setExplicitNodeName(!_isXmlElementWrapperAnnotationDefaultValue(xmlElWrapAnnot));		// true si el nombre del nodo xml se ha dado explicitamente con una anotación, false si se ha calculado como el nombre del miembro
				fieldMap.getXmlMap().setExplicitColElsNodeName(false);
				
				// Si el nodo que envuelve a los elementos del mapa coincide con el nodo que envuelve al tipo, lanzar un error ya que se debería anotar con @XmlValue
				if (wrapperNodeName.equals(beanNodeName)) throw new MarshallerException("La anotacion @XmlElementWrapper establece que el nodo que envuelve a la coleccion " + fAnnot.getField().getName() + " de la clase " + type.getName() + " coincide con el nombre del nodo que envuelve la clase " + type.getName() + "; Utiliza la anotación @XmlValue si quieres que los elementos de la colección cuelguen del nodo que envuelve la clase " + type.getName());
				
				// añadirlo al bean padre
				beanMap.addField(fieldMap);
				
				// Si el miembro es de un tipo complejo o una colección, hay que hacer una llamada recursiva para mapearlo
				_recursiveMapBean(fieldMap.getDataType());
			}
		}
	}
	/**
	 * Llamada recursiva si se trata de:
	 * 		- Un objeto complejo (no un int, long, string, etc)
	 * 		- Una colección de objetos complejos
	 * @param dataType el tipo de dato del miembro
	 * @throws MarshallerException si no se puede mapear
	 */
	private void _recursiveMapBean(final DataType dataType) throws MarshallerException {
		Set<DataType> childDataTypes = Sets.newHashSet();
		
		// Obtener el objeto DataType hijo
		if (dataType.isObject()) {
			if (dataType.isInstanciable()) childDataTypes.add(dataType);
			
		} else if (dataType.isCollection()) {
			if (dataType.asCollection().getValueElementsDataType().isObject()) childDataTypes.add(dataType.asCollection().getValueElementsDataType());
			
		} else if (dataType.isMap()) {
			if (dataType.asMap().getKeyElementsDataType().isObject()) childDataTypes.add(dataType.asMap().getKeyElementsDataType());
			if (dataType.asMap().getValueElementsDataType().isObject()) childDataTypes.add(dataType.asMap().getValueElementsDataType());
		}
		
		// Hacer una llamada recursiva para obtener su definición (solo si es un tipo conocido y no se ha cargado ya)
		if (CollectionUtils.hasData(childDataTypes)) {
			for (DataType dt : childDataTypes) {
				if (dt != null && dt.asObject().isKnownType()) {
					String childTypeName = dt.getName();
					if (!_loadedBeans.containsKey(childTypeName)) _beanMapFromBeanAnnotations(ReflectionUtils.typeFromClassName(childTypeName));
				}
			}
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS ESTATICOS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve un esqueleto de objeto {@link FieldMap} a partir de un {@link Field} de un {@link java.lang.reflect.Type}
	 * Este esqueleto unicamente incluye:
	 * 		- el nombre del field
	 * 		- el tipo de dato
	 * 		- si es oid o no 
	 * @param type el tipo
	 * @param field el field
	 * @return
	 */
	private static FieldMap _fieldMapFromField(final Class<?> type,final Field field,
											   final boolean isAttribute) {
		// Obtener la anotacion @OidField (si la tiene)
		OidField oidFieldAnnot = field.getAnnotation(OidField.class);
		boolean isOid = oidFieldAnnot != null ? true : false;
		
		// Crear el fieldMap
		DataType dataType = _dataTypeFromType(type,field);
		FieldMap fieldMap = new FieldMap(_fieldName(field),	// Nombre del miembro 
										 dataType,			// tipo de datos
										 isOid);			// oid true / false
		
		// Si el tipo de dato es simple, es posible especificar la anotación @XmlWriteIgnoreIfEquals
		// que provoca que NO se serialice el field en el XML si coincide con el valor especificado
		XmlWriteIgnoredIfEquals xmlWriteIgnoreIfEqualsAnnot = field.getAnnotation(XmlWriteIgnoredIfEquals.class);
		if (fieldMap.getDataType().isSimple() && xmlWriteIgnoreIfEqualsAnnot != null) {
			String val = xmlWriteIgnoreIfEqualsAnnot.value();
			if (!Strings.isNullOrEmpty(val)) fieldMap.getXmlMap().setValueToIgnoreWhenWritingXML(xmlWriteIgnoreIfEqualsAnnot.value());
		} else if (xmlWriteIgnoreIfEqualsAnnot != null) {
			throw new MarshallerException(Throwables.message("El field {} está anotado con la anotacion @{}, sin embargo NO es de un tipo simple, así que NO es posible ese comportamiento",
															 field.getName(),XmlWriteIgnoredIfEquals.class.getName()));
		}
		

		//System.out.println("---->" + type.getName() + "." + field.getName() + ": " + dataType.getName() + " > " + dataType.getType());
		XmlTypeDiscriminatorAttribute discriminatorAttrAnnot = field.getAnnotation(XmlTypeDiscriminatorAttribute.class);
        if (!ReflectionUtils.isInstanciable(dataType.getType())) {
//        	 Si el tipo es un tipo parametrizado se puede saber el tipo del field:
//        	 Ej:	public class MyGenericTypeBase<T> {
//        				private T field;	
//        			}
//        	if (field.getGenericType() instanceof TypeVariable && type.getTypeParameters() != null) {
//        		TypeVariable<?> fieldType = ((TypeVariable<?>)field.getGenericType());
//        		TypeVariable<?>[] typeParams = type.getTypeParameters();
//        		if (CollectionUtils.hasData(typeParams)) {
//        			for (TypeVariable<?> typeParam : typeParams) {
//        				if (typeParam.getGenericDeclaration() instanceof Class) {
//        					if (fieldType.getName().equals(typeParam.getName())) {
//        						Type[] bounds = typeParam.getBounds();
//        						// ... no llego a nada...
//        					}
//        				}
//        			}
//        		}
//        	} else  {
				// Si el tipo de dato NO es instanciable hay DOS posibilidades:
				// A.- El campo se anota con @XmlElement SIN indicar el nombre del tag, con lo que en tiempo de ejecución se averigua:
				//			- java->xml: el nombre del tag indicado en la anotación @XmlRootElement del tipo correspondiente a la instancia 
				//			- xml->java: el tipo concreto corresponde al tipo anotado con @XmlRootElement igual al nombre del tag
				// B.- El campo se anota con @XmlElement(name=xxx) INDICANDO el nombre del tag, con lo que dado que todos los posibles tipos que 
				//	   implementan la interfaz o extienden de la clase abstracta van a llegar con mismo tag, es necesario "algo" para discriminar
				//	   el tipo concreto. Se utiliza un atributo del tag indicado en la anotación @XmlTypeDiscriminatorAttribute
	        	if (isAttribute) {
	        		// if the type can be built from a string
	        		if (dataType.isImplementingAnyOf(OID.class)) {
	        			// although the concrete type is not known it's sure that it's an oid that can be created using 
	        			// an static valueOf(String) method
	        		} else {
	        			throw new MarshallerException("El miembro " + field.getType().getName() + " " + field.getName() + " de la clase " + type.getName() + " es un interfaz o una clase abstracta y está definido como ATRIBUTO. SOLO es posible definir miembros con interfaces o clases abstractas en ELEMENTOS del XML");
	        		}
	        	}
	        	// Caso B: la anotación @XmlElement lleva el atributo name y además se indica la anotación @XmlTypeDiscriminatorAttribute
	        	XmlElement xmlElAnnot = field.getAnnotation(XmlElement.class);
	        	if (xmlElAnnot != null && !_isXmlElementAnnotationDefaultValue(xmlElAnnot)		// NO se indica el atributo name de la anotacion @XmlElement
	        	 && discriminatorAttrAnnot == null) throw new MarshallerException("El miembro " + field.getType().getName() + " " + field.getName() + " de la clase " + type.getName() + " es un interfaz o una clase abstracta donde todos los posibles tipos van a estar marcados por el mismo tag XML (se ha anotado con @XmlElement indicando un atributo 'name'). En este caso es necesario anotar el miembro con @XmlTypeDiscriminatorAttribute para incluir en el XML un atributo que permita conocer el tipo concreto al pasar de XML a java; una alternativa es anotar el miembro con @XmlElement SIN indicar el nombre del tag, en este caso NO es necesario anotar con @" + XmlTypeDiscriminatorAttribute.class.getSimpleName());
//        	}
        }
        if (discriminatorAttrAnnot != null) fieldMap.getXmlMap().setDiscriminatorWhenNotInstanciable(discriminatorAttrAnnot.name());
		
		// Ver si el campo es CDATA
		if (!fieldMap.getXmlMap().isAttribute()) {
			XmlCDATA xmlCDATAFieldAnnot = field.getAnnotation(XmlCDATA.class);
			boolean isCDATA = xmlCDATAFieldAnnot != null ? true : false;
			fieldMap.getXmlMap().setCdata(isCDATA);
		}
		return fieldMap;
	}
	/**
	 * Obtiene el nombre de un nodo XML a partir de lo indicado en una anotacion {@link XmlRootElement}, {@link XmlElement} o {@link XmlAttribute}
	 * teniendo en cuenta que si NO se indica valor en la propiedad name(), el valor por defecto es ##default
	 * @param name nombre del indicado en el valor name() de la anotación (puede ser null)
	 * @param defaultName valor por defecto en caso de que NO se indique nada en el valor name() de la anotación
	 * @return
	 */
	private static String _nodeNameFromAnnotation(final String name,final String defaultName) {
		String outName = null;
		if (name != null && !name.equals("##default")) {
			outName = name;
		} else {
			outName = defaultName.replaceAll("\\$","_");
		}
		return outName;
	}
	private static boolean _isXmlAttributeAnnotationDefaultValue(final XmlAttribute annot) {
		return annot.name().equals("##default");
	}	
	private static boolean _isXmlElementAnnotationDefaultValue(final XmlElement annot) {
		return annot.name().equals("##default");
	}
	private static boolean _isXmlElementWrapperAnnotationDefaultValue(final XmlElementWrapper annot) {
		return annot.name().equals("##default");
	}
	/**
	 * Obtiene el tipo de dato de un field a partir de la definición de la clase y el field
	 * @param type el tipo
	 * @param field el field
	 * @return el tipo de dato
	 */
	private static DataType _dataTypeFromType(final Class<?> type,final Field field) {
		// [PASO 0]: Si el miembro está anotado con @XmlInline significa que es un XML inile
		if (field.getAnnotation(XmlInline.class) != null) {
			DataType outDataType = DataType.create("XML");
			return outDataType;
		}
		// [PASO 1]: Averiguar el tipo actual del miembro
		Class<?> actualFieldType = null;
		if (field.getGenericType() instanceof Class) {
			// [A] Miembro NO generico
			actualFieldType = field.getType();
			
		} else {
			// [B] Miembro genérico --> Hay que intetar obtener el Parametro del tipo genérico
			// Ver http://blog.vityuk.com/2011/03/java-generics-and-reflection.html
			// El interfaz java.lang.reflect.Type tiene varias sub-clases:
			// 		java.lang.reflect.Type
			//			|-- java.lang.Class 						-> Clase normal
			//			|-- java.lang.reflect.ParameterizedType		-> Clase con un parámetro genérico (ej: String en List<String>)
			//			|-- java.lang.reflect.TypeVariable			-> Parámetro genérico de una clase (ej: T en List<T>)
			//			|-- java.lang.reflect.WildcardType			-> wildcard type (ej: ? extends Number en List<? extends Number>
			//			|-- java.lang.reflect.GenericArrayType		-> Tipo genérico de un array (ej: T en T[])
			
			// Intentar encontrar el tipo exacto
			Type concreteType = GenericTypeReflector.getExactFieldType(field,type);
			// ... si no se ha encontrado es un error
			if (concreteType == null || !(concreteType instanceof Class)) {
				if (field.getGenericType() instanceof TypeVariable) {
					// NO se ha podido encontrar el tipo exacto
					throw new MarshallerException("El miembro " + field.getType().getName() + " " + field.getName() + " de la clase " + type.getName() + " es de un tipo generico y NO se ha podido encontrar una clase que encaje con la parametrización " + 
												  "Es imposible instanciar el tipo adecuado a partir del tag xml ya que NO se sabe qué tipo crear");
				} else if (field.getGenericType() instanceof ParameterizedType) {
					// Se ha encontrado el tipo exacto
					ParameterizedType pType = (ParameterizedType)field.getGenericType();
					concreteType = pType.getRawType();		// ... se devuelve el tipo Raw
				}
			} 
			actualFieldType = (Class<?>)concreteType;
		}
		
		// [PASO 2]: Componer la Descripción
		String dataTypeDesc = _fieldTypeStandardDesc(type,field,
													   actualFieldType);
		//System.out.println("------->" + type.getName() + "." + field.getName() + "." + actualFieldType.getName() + ":  " + dataTypeDesc);
		DataType outDataType = DataType.create(dataTypeDesc);	// Si se trata de una colección queda pendiente crear el type de los elementos
		
		if (outDataType.isDate()) {
			// Buscar una anotación que indica el formato
			XmlDateFormat dateFmtAnnot = field.getAnnotation(XmlDateFormat.class);
			if (dateFmtAnnot != null && dateFmtAnnot.value() != null) {
				String fmt = dateFmtAnnot.value();
				outDataType.asDate().setDateFormat(fmt);
			}
		}
		
//		if (outDataType.isCollection()) {
//			DataType colElsType = DataType.create(outDataType.asCollection().getValueElementsTypeName());	// Queda pendiente referenciar el BeanMap de este tipo
//			outDataType.asCollection().setValueElementsType(colElsType);
//		}
		return outDataType;
	}
	/**
	 * Obtiene la descripción del tipo de un field en un formato normalizado a partir de ciertos datos de la clase, el field, etc
	 * @param type el tipo que contiene el field
	 * @param field el field
	 * @param actualFieldType el tipo de dato actual del field (resolviendo genéricos, etc)
	 * @return
	 */
	private static String _fieldTypeStandardDesc(final Class<?> type,
											 	 final Field field,final Class<?> actualFieldType) {
		String dataTypeDesc = null;
		if (actualFieldType != Object.class && ReflectionUtils.isTypeDef(actualFieldType)) {
			// Definición de clase java
			dataTypeDesc = Class.class.getCanonicalName(); 	// "java.lang.Class";
			
		} else if (ReflectionUtils.isImplementing(actualFieldType,LanguageTexts.class) && !actualFieldType.equals(LanguageTextsI18NBundleBacked.class)) {
			//"Map:(r01f.locale.Language,java.lang.String)";
			dataTypeDesc = "Map:" + actualFieldType.getName() + "(" + Language.class.getCanonicalName() + "," + String.class.getCanonicalName() + ")";	
			
		} else if (CollectionUtils.isMap(actualFieldType)) {
			// Mapa
			Class<?> keyAndValueComponentTypes[] = _mapFieldKeyValueComponentTypes(type,field);
			if (keyAndValueComponentTypes != null && keyAndValueComponentTypes.length == 2) {
				String keyType = keyAndValueComponentTypes[0] != null ? keyAndValueComponentTypes[0].getName() 
																	  : Object.class.getCanonicalName(); //"java.lang.Object";
				String valueType = keyAndValueComponentTypes[1] != null ? keyAndValueComponentTypes[1].getName() 
																		: Object.class.getCanonicalName(); //"java.lang.Object";
				dataTypeDesc = "Map:" + actualFieldType.getName() + "(" + keyType + "," + valueType + ")";
			} else {
				dataTypeDesc = "Map:" + actualFieldType.getName();
			}
			
		} else if (CollectionUtils.isCollection(actualFieldType)) {
			// Colección
			Class<?> componentType = _collectionFieldComponentType(type,field);
			if (componentType != null) {
				dataTypeDesc = "Collection:" + actualFieldType.getName() + "(" + componentType.getName() + ")";
			} else {
				dataTypeDesc = "Collection:" + actualFieldType.getName();
			}
			
		} else if (CollectionUtils.isArray(actualFieldType)) {
			// Array
			dataTypeDesc = _collectionFieldComponentType(type,field).getName() + "[]";
			
		} else if (actualFieldType.isEnum() || actualFieldType.getAnnotation(XmlEnum.class) != null) {
			// Enumeración
			dataTypeDesc = "Enum(" + actualFieldType.getName() + ")";
			
		} else if (!actualFieldType.isPrimitive() && !ReflectionUtils.isInstanciable(actualFieldType)) {
			// [C] Es un interfaz
			dataTypeDesc = field.getType().getName();
	
		} else {
			// [D] Tipo NO generico (el caso más normal)
			dataTypeDesc = actualFieldType.getName();
		}
		return dataTypeDesc;
	}
	private static String _typeNormalizedDesc(final Class<?> type) {
		String dataTypeDesc = null;
		if (CollectionUtils.isMap(type)) {
			//@SuppressWarnings("unchecked")
			//Class<? extends Map<?,?>> mapType =  (Class<? extends Map<?,?>>)type;
			// Map:(java.lang.Object,java.lang.Object)
			dataTypeDesc = "Map:" + type.getName() + "(" + Object.class.getCanonicalName() + "," + Object.class.getCanonicalName() + ")";
			
		} else if (CollectionUtils.isCollection(type)) {
			//@SuppressWarnings("unchecked")
			//Class<? extends Collection<?>> colType =  (Class<? extends Collection<?>>)type;
			dataTypeDesc = "Collection:" + type.getName() + "(" + Object.class.getCanonicalName() + ")";	
			
		} else if (type.isEnum() || type.getAnnotation(XmlEnum.class) != null) {
			dataTypeDesc = "Enum(" + type.getName() + ")";
			
		} else {
			dataTypeDesc = type.getName();
		}
		return dataTypeDesc;
	}
	/**
	 * Obtiene el nombre de un miembro teniendo en cuenta que el nombre puede comenzar por el prefijo "_"
	 * en cuyo caso se elimina
	 * NOTA: 	Normalmente el prefijo se indica con la anotación {@link Accessors}, PERO el problema es
	 * 			que esta anotación SOLO se mantiene en el codigo, y NO en tiempo de ejecución 
	 * @param type el tipo
	 * @param f el miembro del tipo
	 * @return el nombre del miembro eliminando los prefijos indicados en {@link Accessors}
	 */
	private static String _fieldName(Field f) {
		String prefix = "_";
		
		String outFieldName = f.getName();
		if (f.getName().startsWith(prefix)) outFieldName = f.getName().substring(prefix.length());
		return outFieldName;
	}
    /**
     * Devuelve el tipo de elementos la clave y valor de un Mapa {@link Map} parametrizado (Map<K,V>)
     * NOTA:	Debido al type erasure de los genericos en java el tipo de K y V en un Map<K,V> SOLO se puede obtener
     * 			si se trata de un field de una clase (miembro), pero NO si se trata de una variable en un método
     * @param type el tipo donde está definido el field 
     * @param field el miembro de una clase
     * @return un array de dos posiciones: 0=tipo de la clave, 1=tipo del value
     */
    @SuppressWarnings("unchecked")
	public static Class<?>[] _mapFieldKeyValueComponentTypes(final Type type,final Field field) {
    	Class<?>[] outKeyAndValueTypes = new Class<?>[2];			// array de dos posiciones: 0=key, 1=value
    	
    	Type genericFieldType = field.getGenericType();
    	if (genericFieldType instanceof Class) {
    		// Se trata de una clase que extiende de un mapa parametrizado
    		// Ej: public class LanguageTexts extends Map<Language,String> {...}
    		// Hay que obtener un type = Map<Language,String>
    		genericFieldType =  GenericTypeReflector.getExactSuperType(field.getGenericType(),
    																   Map.class);
    	} 
    	
		// Aqui genericFieldType contiene SIEMPRE un mapa parametrizado (ej: Map<String,String>)
    	ParameterizedType mapComponentType = (ParameterizedType)genericFieldType;
    	if (mapComponentType.getActualTypeArguments().length == 2) {
    		// El field es un mapa
    		// ej: 	private Map<String,String> _theMap;
	    	outKeyAndValueTypes[0] = ReflectionUtils.classOfType(mapComponentType.getActualTypeArguments()[0]);
	    	outKeyAndValueTypes[1] = ReflectionUtils.classOfType(mapComponentType.getActualTypeArguments()[1]);
    	} else {
    		// El field es una clase que extiende de un mapa
    		// ej: 	private MyTypeImplementingMap _theMap;
    		//		siendo public class MyTypeImplementingMap extends HashMap<String,String> {...}
    		mapComponentType = (ParameterizedType)field.getType().getGenericSuperclass();
	    	outKeyAndValueTypes[0] = ReflectionUtils.classOfType(mapComponentType.getActualTypeArguments()[0]);
	    	outKeyAndValueTypes[1] = ReflectionUtils.classOfType(mapComponentType.getActualTypeArguments()[1]);
    	}
    	// Si alguno de los componentes es una variable generica hay que ver si se puede obtener el tipo concreto a través de la parametrización
    	if (outKeyAndValueTypes[0] == null || outKeyAndValueTypes[1] == null) {
    		for (int i=0; i <= 1; i++) {
    		 	if (outKeyAndValueTypes[i] == null && mapComponentType.getActualTypeArguments()[i] instanceof TypeVariable) {
    		 		outKeyAndValueTypes[i] = _collectionComponentType(type,(TypeVariable<? extends Class<?>>)mapComponentType.getActualTypeArguments()[i]);
    		 	}
    		}
    	}
    	return outKeyAndValueTypes;
    }
    /**
     * Devuelve el tipo de elementos de una colección {@link java.util.Collection} o un array parametrizado (Collection<E>)
     * NOTA:	Debido al type erasure de los genericos en java el tipo de un List<tipo> SOLO se puede obtener
     * 			si se trata de un field de una clase (miembro), pero NO si se trata de una variable dentro del flujo 
     * @param field el miembro de una clase
     * @return el tipo 
     */
    @SuppressWarnings("unchecked")
    public static Class<?> _collectionFieldComponentType(final Type type,final Field field) {
    	Class<?> outCollectionComponentClass = null;
    	
    	
    	if (field.getType().isArray()) {
    		Type compType = GenericTypeReflector.getArrayComponentType(field.getType());	// field.getType().getComponentType();
    		outCollectionComponentClass = (compType != null  && compType instanceof Class) ? (Class<?>)compType
    																					   : Object.class;
    	} else {
    		Type genericFieldType = field.getGenericType();
    		if (genericFieldType instanceof Class) {
	    		// Se trata de una clase que extiende de una coleccion parametrizada
	    		// Ej: public class LanguageTexts extends Map<Language,String> {...}
	    		// Hay que obtener un type = Map<Language,String>
	    		genericFieldType =  GenericTypeReflector.getExactSuperType(field.getGenericType(),
		    															   Collection.class);
		    } 
    		// Aqui genericFieldType contiene SIEMPRE una colección parametrizada (ej: Collection<String>)
	        ParameterizedType collectionComponentType = (ParameterizedType)genericFieldType;
	        outCollectionComponentClass = ReflectionUtils.classOfType(collectionComponentType.getActualTypeArguments()[0]);
	        // If the component type is an abstract type, Object is returned
	        if (outCollectionComponentClass != null 
	         && Modifier.isAbstract(outCollectionComponentClass.getModifiers())) outCollectionComponentClass = Object.class;
	        
	        // Si alguno de los componentes es una variable generica hay que ver si se puede obtener el tipo concreto a través de la parametrización
	        if (outCollectionComponentClass == null && collectionComponentType.getActualTypeArguments()[0] instanceof TypeVariable) {
	        	TypeVariable<? extends Class<?>> typeVar = (TypeVariable<? extends Class<?>>)collectionComponentType.getActualTypeArguments()[0];
	        	outCollectionComponentClass = _collectionComponentType(type,
	        														   typeVar);
	        }
    	}
    	return outCollectionComponentClass;
    }
    /**
     * Tries to guess a collection's component concrete type
     * If it cannot guess the type, {@link Object} is returned
     * @param type
     * @param typeVar
     * @return
     */
    private static Class<?> _collectionComponentType(final Type type,final TypeVariable<? extends Class<?>> typeVar) {    	
    	Class<?> outComponentType = null;
 		Type compType = GenericTypeReflector.getTypeParameter(type,typeVar);
 		outComponentType = (compType != null  
 				         && compType instanceof Class 
 				         && !Modifier.isAbstract(((Class<?>)compType).getModifiers())) ? (Class<?>)compType 
 																					   : Object.class;
 		return outComponentType;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  CUSTOM TRANSFORMERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene los transformers xml<->java definidos para un tipo utilizando las anotaciones {@link XmlReadTransformer} y {@link XmlWriteTransformer}
	 * @param type el tipo del que se quieren los transformers xml<->java
	 * @return null si el tipo no tiene las anotaciones o bien no tiene las dos anotaciones
	 * @throws MarshallerException si el tipo tiene una sola de las anotaciones requeridas
	 */
	private static SimpleMarshallerCustomXmlTransformers _customXmlTransformersOf(Class<?> type) {
		XmlReadTransformer readTransformerAnnot = ReflectionUtils.typeOrSuperTypeAnnotation(type,XmlReadTransformer.class);
		XmlWriteTransformer writeTransformerAnnot = ReflectionUtils.typeOrSuperTypeAnnotation(type,XmlWriteTransformer.class);
		if ( (readTransformerAnnot != null && writeTransformerAnnot == null) 
		||   (readTransformerAnnot == null && writeTransformerAnnot != null) ) throw new MarshallerException("Las anotaciones XmlReadTransformer y XmlWriteTransformer tienen que venir en pareja (si se incluye una, se debe incluir la otra");
		
		SimpleMarshallerCustomXmlTransformers outXmlTransformers = null;
		if (readTransformerAnnot != null && writeTransformerAnnot != null) {
			outXmlTransformers = new SimpleMarshallerCustomXmlTransformers((SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer<?>)Reflection.type(readTransformerAnnot.using()).load().instance(),
																		   (SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer)Reflection.type(writeTransformerAnnot.using()).load().instance());		}
		return outXmlTransformers;
	}
	private BeanMap _beanMapFromCustomXmlTransformers(Class<?> type) {
		BeanMap outBeanMap = null;
		SimpleMarshallerCustomXmlTransformers customXmlTransformers = _customXmlTransformersOf(type);
		if (customXmlTransformers != null) {
			outBeanMap = new BeanMap(type.getName());
			outBeanMap.setCustomXMLTransformers(customXmlTransformers);
			
			// Poner el nuevo bean en la salida...
			_loadedBeans.put(type.getName(),outBeanMap);
		}
		return outBeanMap;
	}
}
