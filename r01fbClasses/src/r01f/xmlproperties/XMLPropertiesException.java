package r01f.xmlproperties;

import java.io.IOException;

import r01f.exceptions.EnrichedRuntimeException;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Environment;
import r01f.marshalling.MarshallerException;
import r01f.util.types.Strings;

public class XMLPropertiesException 
     extends EnrichedRuntimeException {
	
	private static final long serialVersionUID = -285396264237852297L;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
	public XMLPropertiesException(final String msg,
								  final XMLPropertiesErrorType type) {
		super(XMLPropertiesErrorType.class,
			  msg,
			  type);
	}
	public XMLPropertiesException(final String msg,
								  final Throwable cause,
								  final XMLPropertiesErrorType type) {
		super(XMLPropertiesErrorType.class,
			  msg,
			  cause,
			  type);
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
///////////////////////////////////////////////////////////////////////////////////////////
	public static XMLPropertiesException componentDefLoadError(final Environment env,final AppCode appCode,final AppComponent component) {
		return XMLPropertiesException.componentDefLoadError(env,appCode,component,
							   		  						null);
	}
	public static XMLPropertiesException componentDefLoadError(final Environment env,final AppCode appCode,final AppComponent component,
															   final IOException ioEx) {
		String err = null;
		if (env == null || env.equals(Environment.NO_ENV)) {
			err = Strings.customized("Error trying to load the component definition xml for appCode/component={}/{}; Ensure that the file /{}/components/{}.{}.xml is in the application classpath (ie: /config/[appCode]/components/{}.{}.xml",
						 			 appCode,component,appCode,appCode,component);
		} else {
			err = Strings.customized("Error trying to load the component definition xml for env/appCode/component={}/{}/{}; Ensure that the file /{}/{}/components/{}.{}.xml  is in the application classpath (ie: /config/[appCode]/components/{}.{}.xml",
						 			 env,appCode,component,env,appCode,appCode,component);
		}
		return new XMLPropertiesException(err,
										  ioEx,
										  XMLPropertiesErrorType.COMPONENTDEF_NOT_FOUND);
	}
	public static XMLPropertiesException componentDefXMLError(final Environment env,final AppCode appCode,final AppComponent component,
															  final MarshallerException msEx) {
		String err = null;
		if (env == null || env.equals(Environment.NO_ENV)) {
			err = Strings.customized("Error trying to load the component definition for appCode/component={}/{}; Ensure that the file /{}/components/{}.{}.xml in the classpath is correct and well formed",
						 			 appCode,component,appCode,appCode,component);
		} else {
			err = Strings.customized("Error trying to load the component definition for env/appCode/component={}/{}/{}; Ensure that the file /{}/{}/components/{}.{}.xml in the classpath is correct and well formed",
						 			 env,appCode,component,env,appCode,appCode,component);
		}
		return new XMLPropertiesException(err,
										  msEx,
										  XMLPropertiesErrorType.COMPONENTDEF_XML_MALFORMED);
	}
	public static XMLPropertiesException propertiesLoadError(final Environment env,final AppCode appCode,final AppComponent component) {
		XMLPropertiesComponentDef compDef = null;
		try {
			compDef = XMLPropertiesComponentDef.load(env,appCode,component);	// Load again the definition 
		} catch(XMLPropertiesException xmlEx) {
			/* ignore */
		}
        String err = null;
        if (env == null || env.equals(Environment.NO_ENV)) {
        	err = Strings.customized("The XML properties file is NOT found for appCode/component={}/{} loaded with: {}",
        				             appCode,component,compDef != null ? compDef.debugInfo() : "the definition was NOT found");
        } else {
        	err = Strings.customized("The XML properties file is NOT found for env/appCode/component={}/{}/{} loaded with: {}",
        				  			 env,appCode,component,compDef != null ? compDef.debugInfo() : "the definition was NOT found");        	
        }
		return new XMLPropertiesException(err,
										  XMLPropertiesErrorType.PROPERTIES_NOT_FOUND);
	}
	public static XMLPropertiesException propertiesXMLError(final Environment env,final AppCode appCode,final AppComponent component) {
		String err = null;
		if (env == null || env.equals(Environment.NO_ENV)) {
			err = Strings.customized("The properties XML for appCode/contentType={}/{} contains some error or is malformed",
						 			 appCode,component);
		} else {
			err = Strings.customized("The properties XML for env/appCode/contentType={}/{}/{} contains some error or is malformed",
						 			 env,appCode,component);			
		}
		return new XMLPropertiesException(err,
										  XMLPropertiesErrorType.PROPERTIES_XML_MALFORMED);
	}
}
