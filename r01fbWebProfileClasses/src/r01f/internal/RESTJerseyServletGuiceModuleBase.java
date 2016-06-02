package r01f.internal;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Every GUICE module is installed at {appCode}Injector singleton holder
 */
@Slf4j
@RequiredArgsConstructor
public class RESTJerseyServletGuiceModuleBase 
	 extends JerseyServletModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final Class<? extends Application> _jerseyAppType;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureServlets() {
		// Route all requests through GuiceContainer
		// IMPORTANT!
		//		If this property is NOT defined, GUICE will try to crate REST resource instances
		//		for every @Path annotated types defined at the injector
		Map<String,String> params = new HashMap<String,String>();
		params.put("javax.ws.rs.Application",
				   _jerseyAppType.getName());
//		params.put("jersey.config.disableMetainfServicesLookup","1");
		this.serve("/*").with(GuiceContainer.class,
						 	  params);
		log.info("REST Application: javax.ws.rs.Application={}",_jerseyAppType.getName());
	}
}
