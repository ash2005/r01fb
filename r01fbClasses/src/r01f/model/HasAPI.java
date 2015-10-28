package r01f.model;

import r01f.services.client.ClientAPI;

/**
 * Interface for model object extensions that contains an API 
 */
public interface HasAPI {
	/**
	 * @return an instance of the api contained in the extended object and injected by the system
	 */
	public ClientAPI getApi();
	/**
	 * Return a typed instance of the api contained in the extended object and injected by the system
	 * @param apiType
	 * @return
	 */
	public <A extends ClientAPI> A getApiAs(final Class<A> apiType);
}
