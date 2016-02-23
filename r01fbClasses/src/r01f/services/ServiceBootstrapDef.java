package r01f.services;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.AppAndComponent;
import r01f.services.client.internal.ServiceToImplAndProxyDef;
import r01f.services.core.internal.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.internal.EJBImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.core.internal.ServletImplementedServicesCoreGuiceModuleBase;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServiceBootstrapDef {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * API app code
	 */
	@Getter private final AppAndComponent _apiAppAndModule;
	/**
	 * Core app code and module
	 */
	@Getter private final AppAndComponent _coreAppCodeAndModule;
	/**
	 * Default proxy impl (REST, EJB, etc)
	 */
	@Getter private final ServicesImpl _defaultProxyImpl;
	/**
	 * Type of the core bootstrap modules
	 */
	@Getter private Collection<Class<ServicesCoreBootstrapGuiceModule>> _coreBeanBootstrapModuleTypes;
	/**
	 * Type of the REST core bootstrap modules
	 */
	@Getter private Collection<Class<ServicesCoreBootstrapGuiceModule>> _coreRESTBootstrapModuleTypes;
	/**
	 * Type of the Servlet core bootstrap modules
	 */
	@Getter private Collection<Class<ServicesCoreBootstrapGuiceModule>> _coreServletBootstrapModuleTypes;
	/**
	 * Type of the EJB core bootstrap modules
	 */
	@Getter private Collection<Class<ServicesCoreBootstrapGuiceModule>> _coreEJBBootstrapModuleTypes;
	/**
	 * The service interface types to impl and proxy binding def
	 */
	@Getter private Collection<ServiceToImplAndProxyDef<? extends ServiceInterface>> _serviceInterfacesToImplAndProxiesDefs;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public void addCoreBeanBootstrapModuleType(final Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase> moduleType) {
		if (_coreBeanBootstrapModuleTypes == null) _coreBeanBootstrapModuleTypes = Lists.newArrayList();
		Object moduleTypeObj = moduleType;
		Class<ServicesCoreBootstrapGuiceModule> beanModuleType = (Class<ServicesCoreBootstrapGuiceModule>)moduleTypeObj;
		_coreBeanBootstrapModuleTypes.add(beanModuleType);
	}
	@SuppressWarnings("unchecked")
	public void addCoreRESTBootstrapModuleType(final Class<? extends RESTImplementedServicesCoreGuiceModuleBase> moduleType) {
		if (_coreRESTBootstrapModuleTypes == null) _coreRESTBootstrapModuleTypes = Lists.newArrayList();
		Object moduleTypeObj = moduleType;
		Class<ServicesCoreBootstrapGuiceModule> restModuleType = (Class<ServicesCoreBootstrapGuiceModule>)moduleTypeObj;
		_coreRESTBootstrapModuleTypes.add(restModuleType);
	}
	@SuppressWarnings("unchecked")
	public void addCoreEJBBootstrapModuleType(final Class<? extends EJBImplementedServicesCoreGuiceModuleBase> moduleType) {
		if (_coreEJBBootstrapModuleTypes == null) _coreEJBBootstrapModuleTypes = Lists.newArrayList();
		Object moduleTypeObj = moduleType;
		Class<ServicesCoreBootstrapGuiceModule> ejbModuleType = (Class<ServicesCoreBootstrapGuiceModule>)moduleTypeObj; 
		_coreEJBBootstrapModuleTypes.add(ejbModuleType);
	}
	@SuppressWarnings("unchecked")
	public void addCoreServletBootstrapModuleType(final Class<? extends ServletImplementedServicesCoreGuiceModuleBase> moduleType) {
		if (_coreServletBootstrapModuleTypes == null) _coreServletBootstrapModuleTypes = Lists.newArrayList();
		Object moduleTypeObj = moduleType;
		Class<ServicesCoreBootstrapGuiceModule> servletModuleType = (Class<ServicesCoreBootstrapGuiceModule>)moduleTypeObj; 
		_coreServletBootstrapModuleTypes.add(servletModuleType);
	}
	public void setServiceInterfacesToImplAndProxiesDefs(final Collection<ServiceToImplAndProxyDef<? extends ServiceInterface>> serviceInterfacesToImplAndProxiesDefs) {
		if (CollectionUtils.isNullOrEmpty(serviceInterfacesToImplAndProxiesDefs)) {
//			throw new IllegalStateException(Throwables.message("The core module {} is NOT accesible via a client-API service interface: there's NO client API service interface to impl and/or proxy binding for {}; check that the {} types @{} annotation appCode and module attributes are correct (they MUST match the ones in {}.client.properties.xml)",
//															   _coreAppCodeAndModule,
//															   _coreAppCodeAndModule,
//															   ServiceInterface.class.getName(),ServiceInterfaceFor.class.getSimpleName(),
//															   _coreAppCodeAndModule.getAppCode()));
		}
		_serviceInterfacesToImplAndProxiesDefs = serviceInterfacesToImplAndProxiesDefs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * do NOT install the REST core buide modules (they're binded at ServicesMainGuiceBootstrap, otherwise they're not visible
     * to the outside world and Guice Servlet filter cannot see REST resources)
	 * @return an instance of every guice module that must be installed into a private module to be isolated from other modules
	 */
	public Collection<ServicesCoreBootstrapGuiceModule> getPrivateBootstrapGuiceModuleInstances() {
		Collection<ServicesCoreBootstrapGuiceModule> outModuleInstances = Lists.newArrayList();
		if (CollectionUtils.hasData(_coreBeanBootstrapModuleTypes)) {
			Collection<ServicesCoreBootstrapGuiceModule> modInstances = _createModuleInstancesOf(_coreBeanBootstrapModuleTypes);
			outModuleInstances.addAll(modInstances);
		}
		if (CollectionUtils.hasData(_coreEJBBootstrapModuleTypes)) {
			Collection<? extends ServicesCoreBootstrapGuiceModule> modInstances = _createModuleInstancesOf(_coreEJBBootstrapModuleTypes);
			outModuleInstances.addAll(modInstances);
		}
		return outModuleInstances;
	}
	public Collection<ServicesCoreBootstrapGuiceModule> getPublicBootstrapGuiceModuleInstances() {
		Collection<ServicesCoreBootstrapGuiceModule> outModuleInstances = Lists.newArrayList();
		if (CollectionUtils.hasData(_coreRESTBootstrapModuleTypes)) {
			Collection<? extends ServicesCoreBootstrapGuiceModule> modInstances = _createModuleInstancesOf(_coreRESTBootstrapModuleTypes);
			outModuleInstances.addAll(modInstances);
		}
		if (CollectionUtils.hasData(_coreServletBootstrapModuleTypes)) {
			Collection<? extends ServicesCoreBootstrapGuiceModule> modInstances = _createModuleInstancesOf(_coreServletBootstrapModuleTypes);
			outModuleInstances.addAll(modInstances);
		}
		return outModuleInstances;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static Collection<ServicesCoreBootstrapGuiceModule> _createModuleInstancesOf(final Collection<Class<ServicesCoreBootstrapGuiceModule>> modulesTypes) {
		return FluentIterable.from(modulesTypes)
							 .transform(new Function<Class<ServicesCoreBootstrapGuiceModule>,ServicesCoreBootstrapGuiceModule>() {
												@Override 
												public ServicesCoreBootstrapGuiceModule apply(final Class<ServicesCoreBootstrapGuiceModule> type) {
													return (ServicesCoreBootstrapGuiceModule)ServiceBootstrapGuiceModuleUtils.createGuiceModuleInstance(type);
												}
							 			})
							 .toList();
	}
}
