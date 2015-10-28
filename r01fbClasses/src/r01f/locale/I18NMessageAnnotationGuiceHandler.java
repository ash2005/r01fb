package r01f.locale;

import javax.inject.Inject;

import r01f.bundles.ResourceBundleControl;
import r01f.bundles.ResourceBundleControlBuilder;
import r01f.bundles.ResourceBundleMissingKeyBehaviour;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.reflection.ReflectionUtils;
import r01f.reflection.ReflectionUtils.FieldAnnotated;
import r01f.resources.ResourcesLoaderDef;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.resources.ResourcesLoaderDefLocation;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertyLocation;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Handle que se encarga de escuchar los eventos que lanza Guice justo ANTES de que devuelva un objeto
 * En este caso, se "inspecciona" el objeto para ver si hay que inyectar un bundle de mensajes.
 * 
 * Cuando una clase "necesita" un bundle de mensajes, basta con:
 * <pre>
 * 	1.- Anotar un miembro del tipo {@link I18NService} con la anotacion @I18NMessageBundle (ver {@link I18NService})
 *  2.- Solicitar a guice la creación de la clase
 * </pre>
 * En caso de que guice se encargue de la creación de la clase que necesita un bundle, se llama al método hear de
 * esta clase para que se encargue de inyectar con el bundle.
 */
public class I18NMessageAnnotationGuiceHandler 
  implements TypeListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Inject 
	private XMLProperties _xmlProperties;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public <I> void hear(final TypeLiteral<I> type,
    					 final TypeEncounter<I> encounter) {
		// Ver si la clase inyectada tiene algún miembro anotado con @I18NMessageBundle
		// y en ese caso inyecta el bundle correspondiente en el miembro
        final FieldAnnotated<I18NMessageBundleService>[] fieldsAnnotatedWithI18N = ReflectionUtils.fieldsAnnotated(type.getRawType(),
        																										   I18NMessageBundleService.class);

        if (fieldsAnnotatedWithI18N != null && fieldsAnnotatedWithI18N.length > 0) {      
            // registrar un inyector de miembros en las clases (ver I18NGuiceModule): binder.bindListener(Matchers.any(),new I18NMessageAnnotationGuiceHandler());
            // ... es decir, para cualquier clase inyectada por Guice, se mira a ver si tiene la anotación @I18NMessageAnnotationGuiceHandler
            //     y se inyectan aquí sus miembros
            encounter.register(new MembersInjector<I>() {
            	// Annonymous Inner Type of MemberInjector...
                @Override
                public void injectMembers(I instance) {
                	// Inyectar el I18NService a cada miembro anotado con @I18NMessageBundle
                    for (FieldAnnotated<I18NMessageBundleService> fieldAnnotatedWitI18N : fieldsAnnotatedWithI18N) {
                    	// Verificar que el miembro es de tipo I18NService
                        if (!fieldAnnotatedWitI18N.getField().getType().isAssignableFrom(I18NService.class)) {                        	
                        	throw new IllegalStateException(Strings.of("Field {} of type {} must be of type {} to be injected with a {}")
																   .customizeWith(fieldAnnotatedWitI18N.getField().getName(),instance.getClass().getCanonicalName(),I18NService.class.getName(),I18NService.class.getName())
																   .asString());
                        }
                    	
                        // Obtener el nombre del bundle a inyectar (si no viene en la anotación se toma el nombre de la clase)
                        I18NMessageBundleService i18nServiceAnnotation = fieldAnnotatedWitI18N.getAnnotation();
                        String[] theBundleChain = i18nServiceAnnotation.chain();
                        if (theBundleChain != null && theBundleChain.length > 0) {
                        	for (String c : theBundleChain) {
                        		if (c.length() == 0) c = type.getRawType().getName().replace('.', '/');		// Poner como valor por defecto el nombre de la clase
                        		if (c.startsWith("/")) c = c.substring(1); 									// asegurarse de que la ruta es relativa (se usa ClassPathLoader)                       		
                        	}
                        }
                        if (!_isValidBundleChain(theBundleChain)) throw new IllegalStateException(Strings.of("Field {} of type {} annotated with {} must have a 'chain' attribute of type String[] with the names of the Bundles of the chain")
                        																				 .customizeWith(fieldAnnotatedWitI18N.getField().getName(),instance.getClass().getCanonicalName(),I18NService.class.getName())
                        																				 .asString());
                        // Obtener la localización de la definición del loading/reloading de recursos en un 
                        // fichero properties
                        ResourcesLoaderDefLocation resLoadDefLocAnnotation = fieldAnnotatedWitI18N.getField().getAnnotation(ResourcesLoaderDefLocation.class);
                        if (resLoadDefLocAnnotation == null) throw new IllegalStateException(Strings.of("Field {} of type {} annotated with {} must define the XMLProperties file location of the resources loading/reloading definition using a {} nested-annotation")
        																							.customizeWith(fieldAnnotatedWitI18N.getField().getName(),instance.getClass().getCanonicalName(),I18NService.class.getName(),ResourcesLoaderDefLocation.class.getName())
        																							.asString());
            			ResourcesLoaderDef resLoadDef = _extractResourcesLoaderDefFromAnnotation(_xmlProperties,
            																					 resLoadDefLocAnnotation);
                        ResourceBundleControl resBundleControl = ResourceBundleControlBuilder.forLoadingDefinition(resLoadDef);
                        
                        // Obtener el comportamiento en caso de no encontrarse la clave
                        ResourceBundleMissingKeyBehaviour theMissingKeyBehaviour = i18nServiceAnnotation.missingKeyBehaviour();
                        
                        // Obtener un I18NService de la factoría y asignarlo al miembro (comprobando previamente que el miembro es del tipo I18NService
                        I18NService service = I18NServiceBuilder.create(resBundleControl)
                        										.forBundleChain(theBundleChain)
                        										.withMissingKeyBehaviour(theMissingKeyBehaviour);
                        ReflectionUtils.setFieldValue(instance,fieldAnnotatedWitI18N.getField(),service,false);
                    }
                }
                
            });
        }
    }
	static ResourcesLoaderDef _extractResourcesLoaderDefFromAnnotation(final XMLProperties xmlProperties,
																	   final ResourcesLoaderDefLocation resLoadDefLocAnnotation) {
        AppCode appCode = AppCode.forId(resLoadDefLocAnnotation.appCode());
        AppComponent component = AppComponent.forId(resLoadDefLocAnnotation.component());
        Path xPath = Path.of(resLoadDefLocAnnotation.xPath());
        
        XMLPropertyLocation xmlPropLoc = XMLPropertyLocation.createFor(appCode,component,xPath);
        ResourcesLoaderDef outDef = ResourcesLoaderDefBuilder.forDefinitionAt(xmlProperties,xmlPropLoc);
        return outDef;
	}
	static final boolean _isValidBundleChain(final String[] theBundleChain) {
		if (theBundleChain == null || theBundleChain.length == 0) return false;
		boolean outValid = true;
		for (String bundle : theBundleChain) {
			if (Strings.isNullOrEmpty(bundle)) {
				outValid = false;
				break;
			}
		}
		return outValid;
	}

}
