/*
 * Created on 26-jul-2004
 * 
 * @author IE00165H
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Usuario
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class XLNetsUser implements Serializable {
    private static final long serialVersionUID = 7643780567483567591L;
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////        
    @Getter @Setter private String _oid = "user-unknown";
    @Getter @Setter private boolean _loginApp;
    @Getter @Setter private String _login;
    @Getter @Setter private String _persona;
    @Getter @Setter private String _puesto;
    @Getter @Setter private String _name = "user-unknown";
    @Getter @Setter private String _dni;
    @Getter @Setter private String _home;
    @Getter @Setter private Language _language;
    @Getter @Setter private String _ip;
    @Getter @Setter private Map<String,String> _attributes = null;		// Atributos del usuario (dni, login, puesto)
    
///////////////////////////////////////////////////////////////////////////////////////////
//  GET & SET
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve un atributo del usuario (dni, login, puesto, etc)
     * @param attrName: El nombre del atributo
     * @return: El atributo (String)
     */
    public String getAttribute(final String attrName) {
        if (attrName == null) return null;
        return CollectionUtils.hasData(_attributes) ? _attributes.get(attrName)
        											: null;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  VALIDEZ
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Comprueba si el objeto es valido
     * @return: true si el objeto es valido y false si no es asín
     */
    public boolean isValid() {
        if (Strings.isNullOrEmpty(_login)) return false;
        if (Strings.isNullOrEmpty(_oid)) return false;
        return true;
    } 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		String userDbg = Strings.create()
								  .addCustomizedIfParamNotNull("         DNI: {}\n",_dni)
								  .addCustomizedIfParamNotNull("       Login: {}\n",_login)
								  .addCustomizedIfParamNotNull("     Persona: {}\n",_persona)
								  .addCustomizedIfParamNotNull("      Puesto: {}\n",_puesto)
								  .addCustomizedIfParamNotNull("      Idioma: {}\n",_language)
								  .addCustomizedIfParamNotNull("          IP: {}\n",_ip)
								  .addCustomizedIfParamNotNull("   Login App: {}\n",_loginApp)
								  .addCustomizedIfParamNotNull("        Home: {}\n",_home)
								.asString();
	    return userDbg;
	}    
}
