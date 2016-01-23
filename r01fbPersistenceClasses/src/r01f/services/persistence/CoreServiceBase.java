package r01f.services.persistence;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.eventbus.EventBus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.events.HasEventBus;
import r01f.exceptions.Throwables;
import r01f.internal.BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.CoreService;
import r01f.services.delegates.ServicesDelegateProvider;
import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Core service base
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class CoreServiceBase 
  		   implements CoreService,		// it's a core service
  		   			  HasEventBus {		// it contains an event bus
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This properties are set at ServicesCoreBootstrapGuiceModuleBase type
	 */
	@Inject @XMLPropertiesComponent("services")
	@Getter protected XMLPropertiesForAppComponent _serviceProperties;
	/**
	 * EventBus 
	 * IMPORTANT! The event listeners are subscribed at {@link BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase}
	 * 			  The subscription takes place when an event listener is configured at the guice moduel (see XXServicesBootstrapGuiceModule)
	 */
	@Inject
	@Getter protected EventBus _eventBus;
/////////////////////////////////////////////////////////////////////////////////////////
//	DELEGATE PROVIDER
// 	A provider is used since typically a new persistence delegate is created at every
//	service impl method call to create a fresh new EntityManager 
//	Note that a fresh new EntityManger is needed in every service impl method call
//	in order to avoid a single EntityManager that would cause transactional and
// 	concurrency issues
//	When at a delegate method services from another entity are needed (maybe to do some
//	validations), create a new delegate for the other entity reusing the current delegate
//	state (mainly the EntityManager), this way the transactional state is maintained:
//		public class CRUDServicesDelegateForX
//			 extends CRUDServicesForModelObjectDelegateBase<XOID,X> {
//			...	
//			public CRUDResult<M> someMethod(..) {
//				....
//				CRUDServicesDelegateForY yDelegate = new CRUDServicesDelegateForY(this);	// reuse the transactional state
//				yDelegate.doSomething();
//				...
//			
//		}
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract Provider<? extends ServiceInterface> getDelegateProvider();
	
	@SuppressWarnings({ "unchecked","unused" })
	public <S extends ServiceInterface> S createDelegateAs(final UserContext userContext,
														   final Class<S> servicesType) {
		if ( !(this.getDelegateProvider() instanceof ServicesDelegateProvider) ) throw new UnsupportedOperationException(Throwables.message("The services delegate {} type MUST be an instance of {} in order to use the user context",
																																			this.getDelegateProvider().getClass(),ServicesDelegateProvider.class));
		ServicesDelegateProvider<S> servicesDelegateProvider = (ServicesDelegateProvider<S>)this.getDelegateProvider();
		return servicesDelegateProvider.get(userContext);
	}
	@SuppressWarnings({ "unchecked","unused" })
	public <S extends ServiceInterface> S createDelegateAs(final Class<S> servicesType) {
		return (S)this.getDelegateProvider().get();
	}
}
