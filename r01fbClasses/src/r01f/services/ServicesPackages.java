package r01f.services;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.internal.ServicesClientAPIFinder;
import r01f.services.client.internal.ServicesClientBootstrapModulesFinder;
import r01f.services.client.internal.ServicesClientInterfaceToImplAndProxyFinder;
import r01f.services.core.ServicesCore;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.core.internal.ServicesCoreBootstrapModulesFinder;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceInterfaceFor;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
public class ServicesPackages {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds subTypes of a given type scanning at given packages
	 * Used at:
	 * 		{@link ServicesClientAPIFinder}#findClientAPIProxyAggregatorTypes()
	 * 		{@link ServicesCoreBootstrapModulesFinder}#_findCoreBootstrapGuiceModuleTypesByAppModule
	 * 		{@link ServicesClientBootstrapModulesFinder}
	 * 		{@link ServicesClientInterfaceToImplAndProxyFinder}.ServiceInterfaceImplementingTypes
	 * @param superType
	 * @param pckgNames
	 * @return
	 */
	public static <T> Set<Class<? extends T>> findSubTypesAt(final Class<T> superType,
													  		 final List<String> pckgNames,
													  		 final ClassLoader otherClassLoader) {
		Set<Class<? extends T>> outSubTypes = null;
		
		log.info("...finding subtypes of {} at packages {} (BEWARE that every type between the type to be found and the supertype MUST be accesible in the package names list)",superType,pckgNames);
		List<URL> pckgUrls = _urlsForPackages(pckgNames,
											  otherClassLoader);
		
		if (CollectionUtils.isNullOrEmpty(pckgUrls)) {
			log.error("Could NOT get any URL for packages {} from any classloader!!!",pckgNames);
			// The org.reflections' ClasspathHelper.forPackage method, at the end does: 
	        // 		for (ClassLoader classLader : ClasspathHelper.classLoaders()) {
			//			Enumeration<URL> urls = classLoader.getResources(<change package dots for />);
			//			if (urls != null) convert the enumeration into a Collection<URL> and return
			// 		}
			// BUT for an unknown reason, when the package is INSIDE a JAR at APP-INF/lib,
			// classLoader.getResources(pckgName) returns null
			// WHAT TO DO???
			// see:
			//		http://www.javaworld.com/article/2077352/java-se/smartly-load-your-properties.html
			//		http://stackoverflow.com/questions/676250/different-ways-of-loading-a-file-as-an-inputstream
			// 		https://github.com/Atmosphere/atmosphere/issues/1229
			//		http://middlewaresnippets.blogspot.com.es/2011/05/class-loading-and-application-packaging.html
			//		

		} else {
			// The usual case (at least in tomcat) is that the package resources URLs can be found 
			// and org.Reflections can be used
			Reflections typeScanner = new Reflections(new ConfigurationBuilder()		// Reflections library NEEDS to have both the interface containing package and the implementation containing package
																.setUrls(pckgUrls)		// see https://code.google.com/p/reflections/issues/detail?id=53
																.setScanners(new SubTypesScanner(true)));
			outSubTypes = typeScanner.getSubTypesOf(superType);
		}
		// When deploying at a WLS, the classes are inside a jar file whose URL is zip:/dominio_wls/servers/server1/tmp/_WL_user/myEAR/n5ymxm/APP-INF/lib/{appCode}Classes.jar!/{appCode}/client/internal  
		// (see org.reflections.vfs.Vfs.DefaultUrlTypes) 
		// ... so the scanned URLs looses the package part {appCode}Classes.jar!/{appCode}/client/internal and ALL jar classes are scanned
		if (CollectionUtils.hasData(outSubTypes)) {
			outSubTypes = FluentIterable.from(outSubTypes)
										.filter(new Predicate<Class<? extends T>>() {
														@Override
														public boolean apply(final Class<? extends T> subType) {
															boolean inPackage = false;
															for (String pckg : pckgNames) {
																if (subType.getPackage().getName().startsWith(pckg)) {
																	inPackage = true;
																	break;
																}
															}
															return inPackage;
														}
												})
										.toSet();
		}
		return outSubTypes;
	}
	private static ClassLoader[] _scanClassLoaders(final ClassLoader otherClassLoader) {
		ClassLoader[] outClassLoaders =	ClasspathHelper.classLoaders(ClasspathHelper.staticClassLoader(),
											 						 ClasspathHelper.contextClassLoader(),
											 						 otherClassLoader);
		return outClassLoaders;
	}		
	private static List<URL> _urlsForPackages(final List<String> pckgNames,
											  final ClassLoader otherClassLoader) {
		if (CollectionUtils.isNullOrEmpty(pckgNames)) throw new IllegalArgumentException();
		List<URL> outUrls = Lists.newLinkedList();
		for (String pckgName : pckgNames) {
			outUrls.addAll(_urlsForPackage(pckgName,
										   otherClassLoader));
		}
		return outUrls;
	}
	private static Collection<URL> _urlsForPackage(final String pckg,
                                                   final ClassLoader otherClassLoader) {
		ClassLoader[] classLoaders = _scanClassLoaders(otherClassLoader);
         
		// org.reflections.ClasspathHelper seems to return ONLY the jar or path containing the given package
		// ... so the package MUST be added back to the url to minimize scan time and unneeded class loading
        Collection<URL> outUrls = ClasspathHelper.forPackage(pckg,
                                                             classLoaders);
        if (CollectionUtils.hasData(outUrls)) {
        	outUrls = FluentIterable.from(outUrls)
                                    .transform(new Function<URL,URL>() {
														@Override
														public URL apply(final URL url) {
															try {
																URL fullUrl = new URL(url.toString() + _resourceName(pckg));
																log.trace("URL to be scanned: {}",fullUrl);
														        return fullUrl;
														    } catch(Throwable th) {
														    	th.printStackTrace(System.out);
														    }
														    return url;
														}
                                               })
                                    .toList();
         }
         return outUrls;
	}
    private static String _resourceName(final String name) {
        if (name == null) return null;
        String resourceName = name.replace(".", "/")
        						  .replace("\\", "/");
        if (resourceName.startsWith("/")) resourceName = resourceName.substring(1);
        return resourceName;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String clientGuiceModulePackage(final AppAndComponent apiAppAndComponent) {
		return DEF_CLIENT_APP_COMPPONENT.equals(apiAppAndComponent.getAppComponent()) ? Strings.customized("{}.client.internal",
																										   apiAppAndComponent.getAppCode())
																					  : Strings.customized("{}.client.{}.internal",
																							  			   apiAppAndComponent.getAppCode(),apiAppAndComponent.getAppComponent());
																					  		
	}
	public static String serviceInterfacePackage(final AppAndComponent apiAppAndComponent) {
		return DEF_CLIENT_APP_COMPPONENT.equals(apiAppAndComponent.getAppComponent()) ? Strings.customized("{}.api.interfaces",
																								   		   apiAppAndComponent.getAppCode())
					  																  : Strings.customized("{}.api.{}.interfaces",
					  																		  			   apiAppAndComponent.getAppCode(),apiAppAndComponent.getAppComponent());
	}
	public static String serviceProxyPackage(final AppAndComponent apiAppAndComponent) {
		return DEF_CLIENT_APP_COMPPONENT.equals(apiAppAndComponent.getAppComponent()) ? Strings.customized("{}.client.servicesproxy",
					  																					   apiAppAndComponent.getAppCode())
					  																  : Strings.customized("{}.client.{}.servicesproxy",
					  																					   apiAppAndComponent.getAppCode(),apiAppAndComponent.getAppComponent());
	}
	public static String apiAggregatorPackage(final AppAndComponent apiAppAndComponent) {
		return DEF_CLIENT_APP_COMPPONENT.equals(apiAppAndComponent.getAppComponent()) ? Strings.customized("{}.client.api",
					  																					   apiAppAndComponent.getAppCode())
																					  : Strings.customized("{}.client.{}.api",
					  																					   apiAppAndComponent.getAppCode(),apiAppAndComponent.getAppComponent());
	}
	public static AppComponent DEF_CLIENT_APP_COMPPONENT = AppComponent.forId("client");
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String coreGuiceModulePackage(final AppCode coreAppCode) {
		return Strings.of("{}.internal")
					  .customizeWith(coreAppCode.getId())
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
		return _appComponentFromCoreBootstrapModuleType(coreBootstrapType,
														null,
														false);
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @param suffix
	 * @return
	 */
	public static AppComponent appComponentFromCoreBootstrapModuleTypeOrNull(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType,
																			 final String suffix) {
		return _appComponentFromCoreBootstrapModuleType(coreBootstrapType,
														suffix,
														false);
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @return
	 */
	public static AppComponent appComponentFromCoreBootstrapModuleTypeOrThrow(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType) {
		return _appComponentFromCoreBootstrapModuleType(coreBootstrapType,
														null,
														true);
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @param suffix
	 * @return
	 */
	public static AppComponent appComponentFromCoreBootstrapModuleTypeOrThrow(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType,
																			  final String suffix) {
		return _appComponentFromCoreBootstrapModuleType(coreBootstrapType,
														suffix,
														true);
	}
	/**
	 * Returns the appComponent from the services core bootstrap type
	 * @param coreBootstrapType
	 * @return
	 */
	private static AppComponent _appComponentFromCoreBootstrapModuleType(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapType,
																		 final String suffix,
																		 final boolean strict) {
		ServicesCore serviceCoreAnnot = ReflectionUtils.typeAnnotation(coreBootstrapType,
																	   ServicesCore.class);
		String modId = serviceCoreAnnot != null ? serviceCoreAnnot.moduleId()
												: null;
		if (strict && Strings.isNullOrEmpty(modId)) throw new IllegalStateException(Throwables.message("{} core bootstrap type is NOT annotated with @{} or the annotation's moduleId property is NOT set",
																					 		 		   coreBootstrapType,ServicesCore.class.getName()));
		if (modId == null) return null;
		
		return suffix != null ? AppComponent.forId(modId + "." + suffix)
							  : AppComponent.forId(modId);
		
	}
}
