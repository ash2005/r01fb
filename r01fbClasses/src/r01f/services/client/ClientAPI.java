package r01f.services.client;

import r01f.usercontext.UserContext;


/**
 * Client API 
 */
public interface ClientAPI {
	/**
	 * @return the user context
	 */
	public <U extends UserContext> U getUserContext();
	/**
	 * @return an aggregator of proxies for the services real services impl
	 */
	public <S extends ServiceProxiesAggregator> S getServiceProxiesAggregator();
	/**
	 * @param aggregatorType
	 * @return an aggregator of proxies for the services real services impl
	 */
	public <S extends ServiceProxiesAggregator> S getServiceProxiesAggregatorAs(Class<S> aggregatorType);
	/**
	 * Returns the {@link ClientAPI} typed
	 * @param type
	 * @return
	 */
	public <A extends ClientAPI> A as(final Class<A> type);
}