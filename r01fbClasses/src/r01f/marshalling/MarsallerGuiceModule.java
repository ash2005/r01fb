package r01f.marshalling;


import r01f.marshalling.annotations.ReusableJaxbMarshaller;
import r01f.marshalling.annotations.ReusableSimpleMarshaller;
import r01f.marshalling.annotations.SingleUseJaxbMarshaller;
import r01f.marshalling.annotations.SingleUseSimpleMarshaller;
import r01f.marshalling.jaxb.JAXBMarshallerReusableImpl;
import r01f.marshalling.jaxb.JAXBMarshallerSingleUseImpl;
import r01f.marshalling.simple.SimpleMarshallerMappings;
import r01f.marshalling.simple.SimpleMarshallerBuilder.SimpleMarshallerReusableImpl;
import r01f.marshalling.simple.SimpleMarshallerBuilder.SimpleMarshallerSingleUseImpl;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class MarsallerGuiceModule 
  implements Module {
	
	@Override
	public void configure(final Binder binder) {
		// Crear DOS bindings del MarshallerMappingsLoader que se encarga de cargar (y custodiar)
		// la definición del marshalling
		// ----------------------------------------------------------------------------------------
		// 1.- SINGLETON para ser utilizado cuando es necesario hacer operaciones de marshalling/unmarshalling
		//	   repetidamente (ej: pasar objetos a XML para guardarlos en BBDD)
		binder.bind(MarshallerMappings.class).annotatedWith(Names.named("SimpleMarshallerMappingsSINGLETON"))
											 .to(SimpleMarshallerMappings.class)
											 .in(Singleton.class);		// <-- importante!!!
		
		// 2.- Instancias para ser utilizadas individualmente cuando es necesario hacer UNA SOLA operación
		// 	   de marshalling/unmarshalling (ej: cargar a objetos una configuración de XML y a partir de ahí trabajar con los objetos)
		binder.bind(MarshallerMappings.class).annotatedWith(Names.named("SimpleMarshallerMappingsnNEWINSTANCE"))
											 .to(SimpleMarshallerMappings.class);	// <-- NO es singleton!!!
		
		
		// Bindings para la inyección de Marshallers en base a la anotación del miembro Marshaller
		// ---------------------------------------------------------------------------------------
		binder.bind(Marshaller.class).annotatedWith(ReusableSimpleMarshaller.class)
									 .to(SimpleMarshallerReusableImpl.class);
		binder.bind(Marshaller.class).annotatedWith(SingleUseSimpleMarshaller.class)
									 .to(SimpleMarshallerSingleUseImpl.class);
		binder.bind(Marshaller.class).annotatedWith(ReusableJaxbMarshaller.class)
									 .to(JAXBMarshallerReusableImpl.class);
		binder.bind(Marshaller.class).annotatedWith(SingleUseJaxbMarshaller.class)
									 .to(JAXBMarshallerSingleUseImpl.class);
			
	}
}
