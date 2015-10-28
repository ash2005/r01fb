package r01f.services;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.services.client.ClientAPI;
import r01f.services.client.ClientAPIForBeanServices;
import r01f.services.client.ClientAPIForEJBServices;
import r01f.services.client.ClientAPIForRESTServices;

@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class ClientAPIDef<T extends ClientAPI> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link ClientAPI} interface type
	 */
	@Getter private final Class<T> _apiInterfaceType;
	/**
	 * The {@link ClientAPI} implementation type (one extending {@link ClientAPIForBeanServices}, {@link ClientAPIForRESTServices}, {@link ClientAPIForEJBServices}, etc)
	 */
	@Getter private final Map<ServicesImpl,Class<? extends T>> _apiImplTypesByServiceImpl;
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <T extends ClientAPI> ClientAPIDef<T> createFor(final Class<T> apiInterfaceType) {
		return new ClientAPIDef<T>(apiInterfaceType,
								   new HashMap<ServicesImpl,Class<? extends T>>());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public Class<? extends T> putAPIImplType(final Class<? extends ClientAPI> implType) {
		return _apiImplTypesByServiceImpl.put(ServicesImpl.fromClientAPIType(implType),
											  (Class<? extends T>)implType);
	}
	public Class<? extends T> getAPIImplTypeFor(final ServicesImpl impl) {
		return _apiImplTypesByServiceImpl.get(impl);
	}
}
