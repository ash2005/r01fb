package r01f.internal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.configproperties.ConfigPropertiesGuiceModule;
import r01f.guid.GUIDDispenserGuiceModule;
import r01f.locale.I18NGuiceModule;
import r01f.marshalling.MarsallerGuiceModule;
import r01f.marshalling.json.JSonMarshallerGuiceModule;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

@Slf4j
@EqualsAndHashCode				// This is important for guice modules
@NoArgsConstructor
public class R01FBootstrapGuiceModule 
  implements Module {
///////////////////////////////////////////////////////////////////////////////////////////
//  CONFIGURE
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(Binder binder) {
		// Some debugging
		if (log.isTraceEnabled()) {
			binder.bindListener(Matchers.any(),
								new ProvisionListener() {
					@Override @SuppressWarnings("rawtypes") 
					public void onProvision(final ProvisionInvocation provision) {
						log.trace(">> Guice provisioning: {}",provision.getBinding());
					}
			});
		}
        log.warn("[START] R01F Bootstraping ________________________________");
		
		//binder.requireExplicitBindings();	// All the injected members MUST be defined at the guice modules
		
		binder.install(new XMLPropertiesGuiceModule());		// XMLProperties
		binder.install(new ConfigPropertiesGuiceModule());	// Configs
		binder.install(new I18NGuiceModule());				// I18N
        binder.install(new GUIDDispenserGuiceModule());		// GUIDDispenser
        binder.install(new MarsallerGuiceModule());			// Marshaller
        binder.install(new JSonMarshallerGuiceModule());	// Marshalling de JSON
        log.warn("  [END] R01F Bootstraping ________________________________");
	}
}
