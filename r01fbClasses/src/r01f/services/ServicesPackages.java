package r01f.services;

import r01f.exceptions.Throwables;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.reflection.ReflectionUtils;
import r01f.services.core.ServicesCore;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceInterfaceFor;
import r01f.util.types.Strings;

public class ServicesPackages {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String clientGuiceModulePackage(final AppCode appCode) {
		return Strings.of("{}.client.internal")
				  	  .customizeWith(appCode.getId())
				  	  .asString();
	}
	public static String coreGuiceModulePackage(final AppCode appCode) {
		return Strings.of("{}.internal")
					  .customizeWith(appCode.getId())
					  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String serviceInterfacePackage(final AppCode apiAppCode) {
		return Strings.of("{}.api.interfaces")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String serviceProxyPackage(final AppCode apiAppCode) {
		return Strings.of("{}.client.servicesproxy")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String apiAggregatorPackage(final AppCode apiAppCode) {
		return Strings.of("{}.client.api")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String servicesCorePackage(final AppCode coreAppCode) {
		return Strings.of("{}.services")
					  .customizeWith(coreAppCode)
					  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <S extends ServiceInterface> AppAndComponent appAndModuleFromServiceInterfaceType(final Class<S> serviceInterface) {
		ServiceInterfaceFor serviceIfaceForAnnot = ReflectionUtils.typeAnnotation(serviceInterface,ServiceInterfaceFor.class);
		if (serviceIfaceForAnnot == null
		 || Strings.isNullOrEmpty(serviceIfaceForAnnot.appCode())
		 || Strings.isNullOrEmpty(serviceIfaceForAnnot.module())) {
			throw new IllegalStateException(Throwables.message("Service interface {} is NOT annotated with @{} or the appCode / module annotation's attributes are NOT set",
															    serviceInterface,ServiceInterfaceFor.class));
		}
		AppCode coreAppCode = AppCode.forId(serviceIfaceForAnnot.appCode());
		AppComponent module = AppComponent.forId(serviceIfaceForAnnot.module());
		return AppAndComponent.composedBy(coreAppCode,module);
	}
	/**
	 * Returns the appCode from the services core bootstrap type
	 * @param coreBootstrapType
	 * @return
	 */
	public static AppCode appCodeFromCoreBootstrapModuleType(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType) {
		return AppCode.forId(coreBootstrapType.getPackage().getName().split("\\.")[0]);		// the appCode is extracted from the package
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @return
	 */
	public static AppComponent appComponentFromCoreBootstrapModuleTypeOrNull(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType) {
		ServicesCore serviceCoreAnnot = ReflectionUtils.typeAnnotation(coreBootstrapType,ServicesCore.class);
		return serviceCoreAnnot != null && Strings.isNOTNullOrEmpty(serviceCoreAnnot.moduleId()) ? AppComponent.forId(serviceCoreAnnot.moduleId())
																								 : null;
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @return
	 */
	public static AppComponent appComponentFromCoreBootstrapModuleTypeOrThrow(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType) {
		AppComponent outComponent = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrNull(coreBootstrapType);
		if (outComponent == null) throw new IllegalStateException(Throwables.message("{} core bootstrap type is NOT annotated with @{} or the annotation's moduleId property is NOT set",
																					 coreBootstrapType,ServicesCore.class.getName()));
		return outComponent;
	}
}
