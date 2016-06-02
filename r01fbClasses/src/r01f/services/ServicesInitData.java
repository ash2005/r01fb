package r01f.services;

import java.util.Collection;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.services.ServiceIDs.ClientApiAppAndModule;
import r01f.services.ServiceIDs.CoreAppAndModule;
import r01f.services.client.ClientAPI;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;

/**
 * Contains necessary data to initialize the services guice injector
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public class ServicesInitData {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	private final ClientApiAppAndModule _clientAppAndModule;
	@Getter	private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapType;
	@Getter	private final String _packageToLookForServiceInterfaces;
	@Getter	private final Map<CoreAppAndModule,ServicesImpl> _coreAppAndModulesDefProxies;
	@Getter	private final Class<? extends ClientAPI> _apiType;
	
	public Collection<CoreAppAndModule> getCoreAppAndModules() {
		return _coreAppAndModulesDefProxies != null ? _coreAppAndModulesDefProxies.keySet() : null;
	}
}
