package r01f.services.client.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
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
	private final AppCode _apiAppCode;

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
		// Find all ClientAPI interface subtypes
		List<URL> apiUrls = new ArrayList<URL>();
		apiUrls.addAll(ClasspathHelper.forPackage(ServicesPackages.apiAggregatorPackage(_apiAppCode)));	// xxx.client.servicesproxy
		apiUrls.addAll(ClasspathHelper.forPackage(ClientAPI.class.getPackage().getName()));		
		Reflections ref = new Reflections(apiUrls);
		Collection<Class<? extends ClientAPI>> clientAPITypes = ref.getSubTypesOf(ClientAPI.class);
		
		if (CollectionUtils.isNullOrEmpty(clientAPITypes)) throw new IllegalStateException(Throwables.message("NO types extending {} was found at {}",ClientAPI.class,ServicesPackages.apiAggregatorPackage(_apiAppCode)));
		
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
	public  Collection<Class<? extends ServiceProxiesAggregator>> findClientAPIProxyAggregatorTypes() {
		// Find all ServiceProxiesAggregatorImpl interface subtypes
		List<String> proxyPckgs = Lists.newArrayListWithExpectedSize(2);
		proxyPckgs.add(ServicesPackages.serviceProxyPackage(_apiAppCode));	// xxx.client.servicesproxy
		proxyPckgs.add(ServiceProxiesAggregator.class.getPackage().getName());
		Set<Class<? extends ServiceProxiesAggregator>> proxyImplTypes = ServicesPackages.findSubTypesAt(ServiceProxiesAggregator.class,
																										proxyPckgs);
		
		if (CollectionUtils.isNullOrEmpty(proxyImplTypes)) throw new IllegalStateException(Throwables.message("NO type extending {} was found at package {}",
																											  ServiceProxiesAggregator.class,
																											  ServicesPackages.serviceProxyPackage(_apiAppCode)));
				
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
