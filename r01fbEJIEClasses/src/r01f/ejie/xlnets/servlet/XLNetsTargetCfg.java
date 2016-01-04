/*
 * Created on 09-jul-2004
 *
 * @author IE00165H
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets.servlet;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;
import r01f.locale.LanguageTexts;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.util.types.collections.CollectionUtils;

/**
 * Modela la configuracion de seguridad de una URI
 */
@Accessors(prefix="_")
     class XLNetsTargetCfg 
implements Serializable {
	
    private static final long serialVersionUID = -3619681298463900886L;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
///////////////////////////////////////////////////////////////////////////////////////////
    public enum ResourceAccess 
     implements EnumExtended<ResourceAccess> {
    	RESTRICT,
    	ALLOW;
    	
    	private static EnumExtendedWrapper<ResourceAccess> _enums = new EnumExtendedWrapper<ResourceAccess>(ResourceAccess.values());
		@Override
		public boolean isIn(ResourceAccess... els) {
			return _enums.isIn(this,els);
		}
		@Override
		public boolean is(ResourceAccess el) {
			return _enums.is(this,el);
		}
		public static ResourceAccess fromName(final String name) {
			return _enums.fromName(name);
		}
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter private String _uriPattern = null;						// Expresion regular que machea la uri a la que se aplica la seguridad
    @Getter @Setter private ResourceAccess _kind = ResourceAccess.RESTRICT;	// Tipo restrictivo por defecto
    @Getter @Setter private Map<String,ResourceCfg> _resources = null;	// Elementos de los que hay que verificar la seguridad

///////////////////////////////////////////////////////////////////////////////////////////
//  GET & SET
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve un recurso
     * @param resourceOID: El oid del recurso
     * @return El objeto R01FResourceCtx con la configuracion del recurso
     */
    public ResourceCfg getResource(final String resourceOID) {
        return _resources != null ? _resources.get(resourceOID) 
        						  : null;
    }
    @Override
    public String toString() {
        StringExtended sb = Strings.create();
        sb.addCustomizedIfParamNotNull("Target uri={}\n",_uriPattern);
        if (CollectionUtils.hasData(_resources)) {
            sb.add("->Elementos:\n");
            for (Iterator<ResourceCfg> it = _resources.values().iterator(); it.hasNext();) {
                sb.add(it.next().toString());
                if (it.hasNext()) sb.add("\n");
            }
            sb.add("\n");
        }
        return sb.toString();
    }

/////////////////////////////////////////////////////////////////////////////////////////
//  INNER CLASS QUE REPRESENTA UN ITEM DEL RECURSO
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Elemento de configuracion de seguridad de un recurso
     */
    @Accessors(prefix="_")
    @RequiredArgsConstructor
    public class ResourceCfg implements Serializable{
        private static final long serialVersionUID = -5044952456280782506L;
        
        @Getter private final String _oid;
        @Getter private final String _type;
        @Getter private final boolean _mandatory;
        @Getter private final LanguageTexts _name;
        
        @Override
        public String toString() {
            return Strings.create()
            			  .addCustomized("\t({}) {}: {} {}",
            					    	 _type,_oid,_name,(_mandatory == true ? " mandatory":""))
            			  .asString();
        }
    }
}
