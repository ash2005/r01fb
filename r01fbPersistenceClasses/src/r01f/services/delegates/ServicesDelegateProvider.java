package r01f.services.delegates;

import javax.inject.Provider;

import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;

public abstract class ServicesDelegateProvider<D extends ServiceInterface> 
	       implements Provider<D> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public abstract D get(final UserContext userContext);
	
	@Override
	public D get() {
		return this.get(null);	// no user context
	}
}
