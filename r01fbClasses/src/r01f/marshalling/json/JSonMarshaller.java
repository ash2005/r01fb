package r01f.marshalling.json;

import java.lang.reflect.Type;

import javax.inject.Inject;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.marshalling.json.JSonMarshallerGuiceModule.GSonProvider;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * Marshaller de objetos simples a JSON:
 * 
 * Uso:
 * <pre class='brush:java'>
 * 		jsonMarshaller.forType(MyType.class)
 * 					  .jsonToObject(jsonStr);
 * </pre>
 * Si el tipo es un genérico, hay que utilizar un TypeToken
 * <pre class='brush:java'>
 * 		jsonMarshaller.forType(new TypeToken<List<MyType>>() {})
 * 					  .jsonToObject(jsonStr);
 * </pre>
 * En ocasiones, NO se dispone del tipo, sino de una cadena con el nombre del tipo
 * (por ejemplo, si se envia el nombre del tipo como cadena a un endpoint REST)
 * Ej: r01f.test.MyType o bien java.util.List<r01f.test.MyType>
 * En este caso se puede utilizar:
 * <pre class='brush:java'>
 * 		jsonMarshaller.forType("java.util.List<r01f.test.MyType>")
 * 					  .jsonToObject(jsonStr);
 * </pre>
 * 
 * [Creacción del Marshaller]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
 * ----------------------------------------------------------------------------
 * [Opción 1]: Inyectar el Marshaller utilizando GUICE
 * 		En la clase donde se quiere inyectar el Marshaller crear un miembro anotado como
 * 		<pre class='brush:java'>
 * 		public class MyMarshallerService {
 * 			@Inject @JsonMarshaller private JSonMarshaller _jsonMarshaller;
 * 			...
 * 		}
 * 		</pre>
 * 		o bien inyectar en el constructor:
 * 		<pre class='brush:java'>
 * 		public class MyMarshallerService {
 * 			private JSonMarshaller _jsonMarshaller;
 * 			@Inject
 * 			public MyMarshallerService(@JSonMarshaller private JSonMarshaller marshaller) {
 * 				_jsonMarshaller = marshaller;
 * 			}
 * 			...
 * 		}
 *		</pre>
 *
 * [Opcion 2]: Utilizar el inyector de Guice (no recomendado)
 * 		<pre class='brush:java'>
 * 			JSonMarshaller marshaller = Guice.createInjector(new JSonMarshallerGuiceModule())
 *										 	 .getInstance(JSonMarshaller.class)
 *		</pre>
 *
 * [OPCION 3]: SIN utilizar Guice (no recomendado ya que crea un objeto GSon cada vez que se invoca a create()
 * 		<pre class='brush:java'>
 * 			JSonMarshaller jsonMarshaller = JSonMarshaller.create();
 * 		</pre>
 */
@Accessors(prefix="_")
@NoArgsConstructor
@Slf4j
public class JSonMarshaller {
///////////////////////////////////////////////////////////////////////////////
// 	INJECT
///////////////////////////////////////////////////////////////////////////////
	@Inject Gson _gson;
///////////////////////////////////////////////////////////////////////////////
// ESTADO
///////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public JSonMarshaller(final Gson gson) {
		_gson = gson;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Método de creación de un {@link JSonMarshaller}
	 * <b>IMPORTANTE</b>
	 * Es recomendable utilizar el objeto JSonMarshaller utilizando GUICE ya que se
	 * cachea el objeto GSon y NO hay que crearlo cada vez  
	 * @return el objeto {@link JSonMarshaller}
	 */
	public static JSonMarshaller create() {		
		GSonProvider gsonProvider = new GSonProvider();
		JSonMarshaller outMarshaller = new JSonMarshaller(gsonProvider.get());
		return outMarshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT
/////////////////////////////////////////////////////////////////////////////////////////
	public JSonMarshallerFromTypeAsString forType(final String type) {
		return new JSonMarshallerFromTypeAsString(type);
	}
	public JSonMarshallerFromTypeAsClass forType(final Class<?> type) {
		return new JSonMarshallerFromTypeAsClass(type);
	}
	public JSonMarshallerFromTypeAsTypeToken forType(final TypeToken<?> typeToken) {
		return new JSonMarshallerFromTypeAsTypeToken(typeToken);
	}
///////////////////////////////////////////////////////////////////////////////
// 	MARSHALLER A PARTIR DEL TIPO COMO UN String (el nombre del tipo)
///////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public class JSonMarshallerFromTypeAsString {
		private final String _type;
		
		@SuppressWarnings("unchecked")
		public <T> T jsonToObject(final String json) {
			return _type != null ? (T)_gson.fromJson(json,_objectType(_type))
								 : null;
		}
		public <T> String objectToJson(final T value) {
			return _type != null ? _gson.toJson(value,_objectType(_type))
								 : _gson.toJson(value);
		}
		/**
		 * Gets the Type from it-s String rep
		 * @param type the type in String format
		 * @return a {@link Type}
		 */
		private Type _objectType(final String type) {
			if (Strings.isNullOrEmpty(type)) throw new IllegalArgumentException("The type cannot be null in order to get its Type");
			Type objType = null;
			try {
				objType = ReflectionUtils.getObjectType(type);
			} catch(ReflectionException refEx) {
				log.error("The type {} contains a class which could NOT be found in the classPath!",type,refEx);
			}
			return objType;
		}
	}
///////////////////////////////////////////////////////////////////////////////
// 	MARSHALLER A PARTIR DEL TIPO COMO UN Class
///////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public class JSonMarshallerFromTypeAsClass {
		private final Class<?> _type;
		
		@SuppressWarnings("unchecked")
		public <T> T jsonToObject(final String json) {
			return _type != null ? (T)_gson.fromJson(json,_type)
								 : null;
		}
		public <T> String objectToJson(final T value) {
			return _type != null ? _gson.toJson(value,_type)
								 : _gson.toJson(value);
		}
	}
///////////////////////////////////////////////////////////////////////////////
// 	MARSHALLER A PARTIR DEL TIPO COMO UN TypeRef
///////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public class JSonMarshallerFromTypeAsTypeToken {
		private final TypeToken<?> _type;
		
		@SuppressWarnings("unchecked")
		public <T> T jsonToObject(final String json) {
			return _type != null ? (T)_gson.fromJson(json,_type.getType())
								 : null;
		}
		public <T> String objectToJson(final T value) {
			return _type != null ? _gson.toJson(value,_type.getType())
								 : _gson.toJson(value);
		}
	}

}
