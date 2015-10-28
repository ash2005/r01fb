package r01f.aspects.dirtytrack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;
import r01f.types.dirtytrack.interfaces.ChangesTrackableMap;


/**
 * Inyecta el interfaz {@link DirtyStateTrackable} y su comportamiento definidos en el aspecto {@link DirtyStateTrackableAspect},
 * {@link ChangestTrackableMapAspect} y {@link ChangesTrackableCollectionAspect} a todas las clases anotadas con 
 * {@link ConvertToDirtyStateTrackable}
 * 
 * Para hacer que un tipo implemente el interfaz {@link DirtyStateTrackable} simplemente hay que anotar la clase 
 * con @ConvertToDirtyStateTrackable 
 */
privileged public aspect ConvertToDirtyStateTrackableAspect {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE
/////////////////////////////////////////////////////////////////////////////////////////	
//	declare parents : @ConvertToDirtyStateTrackable * implements DirtyStateTrackable;
	/**
	 * Hacer que todos los tipos anotados con @ConvertToDirtyStateTrackable y que NO extiendan de Map, List o Set implementen
	 * el interfaz DirtyStateTrackable (cuyo comportamiento está en {@link DirtyStateTrackableAspect})
	 */
	declare parents : @ConvertToDirtyStateTrackable !(Map+ || List+ || Set+) && !DirtyStateTrackable+ implements DirtyStateTrackable;
	/**
	 * Hacer que todos los tipos anotados con @ConvertToDirtyStateTrackable y que extiendan de Map implementen
	 * el interfaz ChangesTrackableCollection (cuyo comportamiento está en {@link ChangesTrackableCollectionAspect})
	 */
	declare parents : @ConvertToDirtyStateTrackable Map+ && !ChangesTrackableMap+ implements ChangesTrackableMap;
	/**
	 * Hacer que todos los tipos anotados con @ConvertToDirtyStateTrackable y que extiendan de Collection implementen
	 * el interfaz ChangesTrackableCollection (cuyo comportamiento está en {@link ChangesTrackableCollectionAspect})
	 */
	declare parents : @ConvertToDirtyStateTrackable Collection+ && !ChangesTrackableCollection+ implements ChangesTrackableCollection;
	/**
	 * En caso de colisión los aspectos de los mapas, listas y sets tienen precedencia
	 */
	declare precedence : ChangestTrackableMapAspect,ChangestTrackableCollectionAspect,ChangestTrackableListAspect,DirtyStateTrackableAspect;
}
