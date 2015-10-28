package r01f.xmlproperties;


import r01f.guids.CommonOIDs.Environment;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppCache.XMLPropertiesForAppCacheFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class XMLPropertiesGuiceModule 
  implements Module {
	@Override
	public void configure(Binder binder) {
		// AssistedInject para inyectar objetos XMLPropertiesCache en base a una factoría (se usa en la clase XMLProperties)
		// NOTA: 	para crear un objeto XMLPropertiesCache son necesarios parametros como el código de aplicación y tamaño de la cache que
		//			solo se conocen en tiempo de ejecución, pero también se necesita inyectar la instancia de ResourcesLoaderFactory
		Module assistedModuleForPropertiesCacheFactory = new FactoryModuleBuilder().implement(XMLPropertiesForAppCache.class,
																							  XMLPropertiesForAppCache.class)
												 		   						   .build(XMLPropertiesForAppCacheFactory.class);
		binder.install(assistedModuleForPropertiesCacheFactory);
		
		// XMLProperties: SINGLETON!!!!!!
		binder.bind(XMLProperties.class)
			  .in(Singleton.class);
		
		// Entorno
		// 		- Si se define la propiedad del sistema r01Env la definición de los componentes (XMLPropertiesComponentDef) se carga
		//		  de la ruta del classPath /components/[env]/[appCode].[component].xml
		//		- Si NO se define la propiedad r01Env, la definición de los componentes (XMLPropertiesComponentDef) se carga de
		//		  la ruta del classPath /components/[appCode].[component].xml
		String envSystemProp = System.getProperty("r01Env");
		Environment theEnv = Strings.isNOTNullOrEmpty(envSystemProp) ? Environment.forId(envSystemProp)
														   			 : Environment.forId("noEnv");		// default
		binder.bind(Environment.class)
			  .annotatedWith(XMLPropertiesEnvironment.class)
			  .toInstance(theEnv);
	}
}
