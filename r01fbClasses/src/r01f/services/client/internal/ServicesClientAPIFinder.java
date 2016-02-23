package r01f.services.client.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.guids.AppAndComponent;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesPackages;
import r01f.services.client.ClientAPI;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.util.types.collections.CollectionUtils;

@RequiredArgsConstructor
public class ServicesClientAPIFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final AppAndComponent _apiAppAndModule;

/////////////////////////////////////////////////////////////////////////////////////////
//  CLIENT API AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link Set} that contains {@link ClientAPIDef} objects that relates the {@link ClientAPI} interfaces 
	 * with it's implementations ({@link ClientAPIForBeanServices}, {@link ClientAPIForEJBServices}, {@link ClientAPIForRESTServices}, etc)
	 * and also the default impl ({@link ClientAPIForDefaultServices}) specified at the [apiAppCode].client.properties.xml
	 * @param apiAppCode
	 * @return
	 */
	public Collection<Class<? extends ClientAPI>> findClientAPIs() {
		// Find all client apis
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(ServicesPackages.apiAggregatorPackage(_apiAppAndModule));	
		pckgs.add(ClientAPI.class.getPackage().getName());
		Set<Class<? extends ClientAPI>> clientAPITypes = ServicesPackages.findSubTypesAt(ClientAPI.class,
																						 pckgs,
																						 this.getClass().getClassLoader());
		if (CollectionUtils.isNullOrEmpty(clientAPITypes)) throw new IllegalStateException(Throwables.message("NO types extending {} was found at {}",
																											  ClientAPI.class,ServicesPackages.apiAggregatorPackage(_apiAppAndModule)));
		return FluentIterable.from(clientAPITypes)
							 .filter(new Predicate<Class<? extends ClientAPI>>() {
											@Override
											public boolean apply(final Class<? extends ClientAPI> clientAPIType) {
												return ReflectionUtils.isInstanciable(clientAPIType);	// ignore interfaces or abstract types
											}
							 		 })
							 .toSet();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROXY AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	public Collection<Class<? extends ServiceProxiesAggregator>> findClientAPIProxyAggregatorTypes() {
		// Find all ServiceProxiesAggregatorImpl interface subtypes
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(ServicesPackages.serviceProxyPackage(_apiAppAndModule));	
		pckgs.add(ServiceProxiesAggregator.class.getPackage().getName());
		Set<Class<? extends ServiceProxiesAggregator>> proxyImplTypes = ServicesPackages.findSubTypesAt(ServiceProxiesAggregator.class,
																										pckgs,
																										this.getClass().getClassLoader());
		if (CollectionUtils.isNullOrEmpty(proxyImplTypes)) throw new IllegalStateException(Throwables.message("NO type extending {} was found at package {}",
																											  ServiceProxiesAggregator.class,
																											  ServicesPackages.serviceProxyPackage(_apiAppAndModule)));
		return FluentIterable.from(proxyImplTypes)
							 .filter(new Predicate<Class<? extends ServiceProxiesAggregator>>() {
											@Override
											public boolean apply(final Class<? extends ServiceProxiesAggregator> proxyAggregatorImplType) {
												return ReflectionUtils.isInstanciable(proxyAggregatorImplType);	// ignore interfaces
											}
							 		 })
							 .toSet();
	}
}
