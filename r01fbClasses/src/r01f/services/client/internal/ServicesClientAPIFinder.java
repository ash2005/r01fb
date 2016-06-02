package r01f.services.client.internal;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesPackages;
import r01f.services.client.ClientAPI;
import r01f.services.client.ClientAPIImplBase;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.util.types.collections.CollectionUtils;

@RequiredArgsConstructor
public class ServicesClientAPIFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  CLIENT API AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link Set} that contains {@link ClientAPIDef} objects that relates the {@link ClientAPI} interfaces 
	 * with it's implementations ({@link ClientAPIForBeanServices}, {@link ClientAPIForEJBServices}, {@link ClientAPIForRESTServices}, etc)
	 * and also the default impl ({@link ClientAPIForDefaultServices}) specified at the [apiAppCode].client.properties.xml
	 * @param packageToLookForClientApiAggregatorType
	 * @return
	 */
	public Class<? extends ClientAPI> findClientAPI(String packageToLookForClientApiAggregatorType) {
		// Find all client apis
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(packageToLookForClientApiAggregatorType);	
		pckgs.add(ClientAPI.class.getPackage().getName());
		Set<Class<? extends ClientAPI>> clientAPIImplTypes = ServicesPackages.findSubTypesAt(ClientAPI.class,
																						    pckgs,
																						    this.getClass().getClassLoader());
		if (CollectionUtils.isNullOrEmpty(clientAPIImplTypes)) throw new IllegalStateException(Throwables.message("NO types extending {} was found at {}",
																											  ClientAPI.class,packageToLookForClientApiAggregatorType));
		Collection<Class<? extends ClientAPI>> clientApiTypes = FluentIterable.from(clientAPIImplTypes)
																	 .filter(new Predicate<Class<? extends ClientAPI>>() {
																					@Override
																					public boolean apply(final Class<? extends ClientAPI> clientAPIType) {
																						return ReflectionUtils.isInstanciable(clientAPIType);	// ignore interfaces or abstract types
																					}
																	 		 })
																	 .toSet();
		if (CollectionUtils.isNullOrEmpty(clientApiTypes)) throw new IllegalStateException(Throwables.message("NO instanciable types extending {} was found at {}",
																										  ClientAPI.class,packageToLookForClientApiAggregatorType));
		if (clientApiTypes.size() != 1) throw new IllegalStateException(Throwables.message("More than a single {} type was found at {}",
																					   ClientAPI.class,packageToLookForClientApiAggregatorType));
		return Iterables.getOnlyElement(clientApiTypes);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROXY AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Uses the client api type to guess the proxy aggregator type
	 * Remember that client api type extends {@link ClientAPIImplBase} and must be parameterized with
	 * a type extending {@link ServiceProxiesAggregator}
	 * <pre class='brush:java'>
	 * 		public class MyClientApi
	 * 			 extends ClientAPIBase<MyClientProxyAggregator> {
	 * 			...
	 * 		} 
	 * </pre>
	 * @param clientAPIType
	 * @return
	 */
	@SuppressWarnings({ "unchecked","static-method" })
	public Class<? extends ServiceProxiesAggregator> findClientAPIProxyAggregatorType(final Class<? extends ClientAPI> clientAPIType) {
		ParameterizedType t = (ParameterizedType)clientAPIType.getGenericSuperclass(); // ClientAPIBase<T extends ServiceProxiesAggregator>
		Class<?> clazz = (Class<?>)t.getActualTypeArguments()[0]; // Class<? extends ServiceProxiesAggregator>
		if (clazz == null) throw new IllegalArgumentException(clientAPIType + " MUST extends " + ClientAPIImplBase.class + " with a type parameter that extends " + ServiceProxiesAggregator.class);
		Class<? extends ServiceProxiesAggregator> proxyType = (Class<? extends ServiceProxiesAggregator>)clazz;
		return proxyType;
	}
	public Class<? extends ServiceProxiesAggregator> findClientAPIProxyAggregatorType(final String packageToLookForServiceProxiesAggregatorType) {
		// Find all ServiceProxiesAggregatorImpl interface subtypes
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(packageToLookForServiceProxiesAggregatorType);	
		pckgs.add(ServiceProxiesAggregator.class.getPackage().getName());
		Set<Class<? extends ServiceProxiesAggregator>> proxyImplTypes = ServicesPackages.findSubTypesAt(ServiceProxiesAggregator.class,
																										pckgs,
																										this.getClass().getClassLoader());
		if (CollectionUtils.isNullOrEmpty(proxyImplTypes)) throw new IllegalStateException(Throwables.message("NO type extending {} was found at package {}",
																											  ServiceProxiesAggregator.class,
																											  packageToLookForServiceProxiesAggregatorType));
		Collection<Class<? extends ServiceProxiesAggregator>> proxyAggrTypes = FluentIterable.from(proxyImplTypes)
																					 .filter(new Predicate<Class<? extends ServiceProxiesAggregator>>() {
																									@Override
																									public boolean apply(final Class<? extends ServiceProxiesAggregator> proxyAggregatorImplType) {
																										return ReflectionUtils.isInstanciable(proxyAggregatorImplType);	// ignore interfaces
																									}
																					 		 })
																					 .toSet();
		if (CollectionUtils.isNullOrEmpty(proxyAggrTypes)) throw new IllegalStateException(Throwables.message("NO instanciable type extending {} was found at package {}",
																											  ServiceProxiesAggregator.class,
																											  packageToLookForServiceProxiesAggregatorType));
		if (proxyAggrTypes.size() != 1) throw new IllegalStateException(Throwables.message("More than a single {} type was found at {}",
																						   ServiceProxiesAggregator.class,packageToLookForServiceProxiesAggregatorType));
		return Iterables.getOnlyElement(proxyAggrTypes);
	}
}
