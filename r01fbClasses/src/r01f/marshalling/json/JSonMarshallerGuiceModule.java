package r01f.marshalling.json;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Guice Mapping for {@link JSonMarshaller}
 */
public class JSonMarshallerGuiceModule
  implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(Gson.class).toProvider(GSonProvider.class)
			  .in(Singleton.class);
	}
	/**
	 * Provider de instancias de GSon
	 */
	static class GSonProvider 
	  implements Provider<Gson> {
			@Override
			public Gson get() {
				GsonBuilder gsonBilder = new GsonBuilder();
				gsonBilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
				gsonBilder.serializeNulls();
				return gsonBilder.create();
			}
		
	}
	
}
