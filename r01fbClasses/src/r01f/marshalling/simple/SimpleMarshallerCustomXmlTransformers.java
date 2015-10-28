package r01f.marshalling.simple;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class SimpleMarshallerCustomXmlTransformers {
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////	
    // Si se utilizan custom transformers en lugar de mapeo de campos...
    // IMPORTANTE:	Son SINGLETONS, es decir, para un beanMap concreto hay UNA SOLA instancia del transformer
    //				por lo tanto, los transformers, NO DEBEN TENER ESTADO
    @Getter private final SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer<?> _xmlReadTransformer;
    @Getter private final SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer _xmlWriteTransformer;
    
/////////////////////////////////////////////////////////////////////////////////////////
//  XML -> OBJETOS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Interfaz que han de implementar las clases responsables de transformar de XML a un objeto java
	 * IMPORTANTE!	Se instancia un único objeto en el {@link BeanMap} correspondiente (actua como un singleton) 
	 * 				así que NO DEBE DE TENER ESTADO
	 * @param <T>
	 */
	public static interface XmlReadCustomTransformer<T> {
		/**
		 * Transforma una cadena de texto en un bean
		 * @param xml el xml (puede ser el valor de un atributo o un xml completo)
		 * @return el bean
		 */
		public T beanFromXml(boolean isAttribute,
							 CharSequence xml);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  OBJETOS -> XML
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Interfaz que han de implementar las clases responsables de transformar de objeto java a xml
	 * IMPORTANTE!	Se instancia un único objeto en el {@link BeanMap} correspondiente (actua como un singleton) 
	 * 				así que NO DEBE DE TENER ESTADO
	 * @param <T>
	 */
	public static interface XmlWriteCustomTransformer {
		/**
		 * Transforma un bean a un xml
		 * @param bean el bean
		 * @return el xml (puede ser el valor de un atributo o un xml completo)
		 */
		public String xmlFromBean(boolean isAttribute,
								  Object bean);
	}
}
