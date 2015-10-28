package r01f.internal;

import r01f.marshalling.MarsallerGuiceModule;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.annotations.SingleUseSimpleMarshaller;
import r01f.xmlproperties.XMLPropertiesComponentDef;

import com.google.inject.Guice;
import com.google.inject.Key;

/**
 * Encapsula un Marshaller de los objetos usados internamente por R01F
 * (es un SINGLETON)
 */
public class BuiltInObjectsMarshaller {
	/**
	 * Devuelve el marshaller de objetos built-in
	 * @return
	 */
	public static Marshaller instance() {
		return MarshallerSingleton.MARSHALLER.instance();
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	 Singleton-Holder (ver http://java.dzone.com/articles/java-memes-which-refuse-die)
/////////////////////////////////////////////////////////////////////////////////////////
    private enum MarshallerSingleton {
    	MARSHALLER;
    	private final Marshaller _marshallerInstance;
    	private MarshallerSingleton() {
    		_marshallerInstance = _createMarshaller();
    	}
    	public Marshaller instance() {
    		return _marshallerInstance;
    	}
		/**
		 * Inicializa la instancia estática del marshaller de los XML de definición de 
		 * ResourcesLoader a objetos {@link ResourcesLoaderDef}
		 * @return el marshaller
		 */
		private static Marshaller _createMarshaller() {
			Marshaller marshaller = null;
			try {
				marshaller = Guice.createInjector(new MarsallerGuiceModule())
								  .getInstance(Key.get(Marshaller.class,SingleUseSimpleMarshaller.class));
				marshaller.addTypes(XMLPropertiesComponentDef.class);
			} catch(MarshallerException msEx) {
				/* ignore (es imposible) */
			}
			return marshaller;
		}
    }
}
