package r01f.marshalling.jaxb;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.w3c.dom.Node;

import r01f.encoding.TextEncoder;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.MarshallerMappings;

public class JAXBMarshallerBase
  implements Marshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESO A LOS MAPPINGS
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public MarshallerMappings getMappings() {
    	return null;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE XSDs
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Marshaller addTypes(String mapFilesPath) throws MarshallerException {
		return null;
	}
	@Override
	public Marshaller addTypes(InputStream mapsIS) 	throws MarshallerException {
		return null;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE BEANS ANOTADOS
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Marshaller addTypes(Class<?>... annotatedTypes) throws MarshallerException {
    	return null;
    }
    @Override
    public Marshaller addTypes(Package... packages) throws MarshallerException {
    	return null;
    }
    @Override
    public Marshaller addTypes(Object... searchSpecs) throws MarshallerException {
    	return null;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONFIGURACION
/////////////////////////////////////////////////////////////////////////////////////////// 
	@Override
	public Marshaller usingEncoder(TextEncoder encoder) {
		return null;
	}
    @Override
    public String debugInfo() {
    	return null;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONVERSION
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <T> T beanFromXml(String beanXml) throws MarshallerException {
		return null;
	}
	@Override
	public <T> T beanFromXml(CharSequence beanXml) throws MarshallerException {
		return null;
	}
	@Override
	public <T> T beanFromXml(byte[] beanXml) throws MarshallerException {
		return null;
	}
	@Override
	public <T> T beanFromXml(InputStream beanXmlIS) throws MarshallerException {
		return null;
	}
	@Override
	public <T> T beanFromXml(Node beanXmlNode) throws MarshallerException {
		return null;
	}
	@Override
	public String xmlFromBean(Object bean) throws MarshallerException {
		return null;
	}
	@Override
	public <T> String xmlFromBean(final T bean,
								  final Charset charset) {
		return null;
	}
}
