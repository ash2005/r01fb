package r01f.marshalling.simple;

import java.io.File;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.marshalling.Marshaller.MarshallerMappingsSearch;
import r01f.marshalling.MarshallerMappings;
import r01f.patterns.IsBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Marshaller de XML a objetos y viceversa
 * La forma habitual de utilizar es la siguiente:
 * 	<pre class='brush:java'>
 * 		// obtener una instancia del marshaller
 * 		Marshaller marshaller = SimpleMarshaller.createForTypes(MyObj.class)
 * 												.getForSingleUse()
 * 		// Pasar de objetos a XML
 * 		MyObj myObjInstance = marshaller.beanFrom(xml);
 * 		// Pasar del XML a objetos
 * 		String xml = marshaller.xmlFrom(myObjInstance);
 * </pre>
 * 
 * Para inicializar el Marshaller es necesario un XML de definición de cómo hacer las transformaciones
 * hay detalles de cómo configurar el XML de definición en {@link SimpleMarshallerMappingsFromXMLLoader} 
 */
public class SimpleMarshallerBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER FOR SINGLE USE
/////////////////////////////////////////////////////////////////////////////////////////
	public static MarshallerTypeFactory create() {
		MarshallerMappings mappings = SimpleMarshallerMappings.createFrom(( Class<?>[])null);
		return new MarshallerTypeFactory(mappings);
	}
	public static MarshallerTypeFactory createForTypes(final Class<?>... annotatedTypes) {
		MarshallerMappings mappings = SimpleMarshallerMappings.createFrom(annotatedTypes);
		return new MarshallerTypeFactory(mappings);
	}
	public static MarshallerTypeFactory createForPackages(final String... packages) {
		MarshallerMappings mappings = new SimpleMarshallerMappings();
		mappings.loadFromAnnotatedTypes((Object[])MarshallerMappingsSearch.inPackages(packages));
		return new MarshallerTypeFactory(mappings);
	}
	public static MarshallerTypeFactory createForMappings(final File mapFile) {
		MarshallerMappings mappings = SimpleMarshallerMappings.createFrom(mapFile);
		return new MarshallerTypeFactory(mappings);
	}
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public static class MarshallerTypeFactory {
		private final MarshallerMappings _mappings;
		public Marshaller getForSingleUse() {
			return new SimpleMarshallerSingleUseImpl(_mappings);
		}
		public Marshaller getForMultipleUse() {
			return new SimpleMarshallerReusableImpl(_mappings);
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  MULTIPLES USOS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Extiende a {@link SimpleMarshallerBase} haciendo que la instancia de {@link r01f.marshalling.MarshallerMappings}
	 * que contiene la definición de mapeos sea una referencia al SINGLETON custodiado por GUICE, es decir,
	 * se REUTILIZA el mapeo, o lo que es lo mismo, se CACHEA
	 */
	public static class SimpleMarshallerReusableImpl 
	            extends SimpleMarshallerBase {
		/**
		 * Ver @MarshallerGuiceModule
		 * Guice inyecta una nueva instancia de SimpleMarshallerMappings
		 * @param mappings los mappings inyectados por guice
		 */
		@Inject
		public SimpleMarshallerReusableImpl(@Named("SimpleMarshallerMappingsSINGLETON") final MarshallerMappings mappings) {
			_mappings = mappings;
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  UN SOLO USO
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Extiende a {@link SimpleMarshallerBase} haciendo que la instancia de {@link r01f.marshalling.MarshallerMappings}
	 * que contiene la definición de mapeos sea una referencia una NUEVA INSTANCIA inyectadaor GUICE, es decir,
	 * se NO SE REUTILIZA el mapeo, o lo que es lo mismo, NO se CACHEA
	 */
	public static class SimpleMarshallerSingleUseImpl 
	            extends SimpleMarshallerBase {         
		/**
		 * Ver @MarshallerGuiceModule
		 * Guice inyecta una nueva instancia de SimpleMarshallerMappings
		 * @param mappings los mappings inyectados por guice
		 */
		@Inject
		public SimpleMarshallerSingleUseImpl(@Named("SimpleMarshallerMappingsnNEWINSTANCE") final MarshallerMappings mappings) {
			_mappings = mappings;
		}
	}

}
