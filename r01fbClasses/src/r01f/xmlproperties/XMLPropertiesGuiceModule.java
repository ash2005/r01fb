package r01f.xmlproperties;


import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import r01f.guids.CommonOIDs.Environment;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppCache.XMLPropertiesForAppCacheFactory;

public class XMLPropertiesGuiceModule 
  implements Module {
	@Override
	public void configure(Binder binder) {
		// XMLProperties: SINGLETON!!!!!!
		binder.bind(XMLProperties.class)
			  .in(Singleton.class);
		
		// AssistedInject to inject XMLPropertiesForAppCache object when creating a XMLProperties object
		// To create an instance of XMLPropertiesForAppCache the appCode and an estimation of the property number are needed
		// both variables are only known at RUNTIME (when readed from the properties component definition file)
		Module assistedModuleForPropertiesCacheFactory = new FactoryModuleBuilder().implement(XMLPropertiesForAppCache.class,
																							  XMLPropertiesForAppCache.class)
												 		   						   .build(XMLPropertiesForAppCacheFactory.class);
		binder.install(assistedModuleForPropertiesCacheFactory);
		
		
		// Environment
		// 		- If a system property named r01Env is defined, the properties component definitions (XMLPropertiesComponentDef) are loaded from
		//		  a classpath folder at /{appCode}/components/{r01Env}/{appCode}.{component}.xml
		//		- If the system property named r01Env is NOT defined, the properties component definition (XMLPropertiesComponentDef) are loaded from
		//		  a classPath folder at /{appCode}/components/{appCode}.{component}.xml
		String envSystemProp = System.getProperty("r01Env");
		Environment theEnv = Strings.isNOTNullOrEmpty(envSystemProp) ? Environment.forId(envSystemProp)
														   			 : Environment.forId("noEnv");		// default
		binder.bind(Environment.class)
			  .annotatedWith(XMLPropertiesEnvironment.class)
			  .toInstance(theEnv);
	}
}
