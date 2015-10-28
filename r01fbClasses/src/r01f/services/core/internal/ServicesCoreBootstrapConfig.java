package r01f.services.core.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.services.ServicesImpl;

/**
 * Config for a service bootstrap guice module
 * @see ServicesCoreBootstrap
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServicesCoreBootstrapConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter final AppCode _appCode;
	@Getter final AppComponent _module;
	@Getter final ServicesImpl _impl;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesImpl getDefaultImpl() {
		return _impl != null ? _impl : ServicesImpl.Bean;	// Bean services by default
	}
}
