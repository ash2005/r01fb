/*
 * Created on 26-jul-2004
 * 
 * @author IE00165H
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsBuilder;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.util.types.Strings;

/**
 * Perfil de usuario de un contexto de autorizacion
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class XLNetsProfile 
  implements Serializable {
	
    private static final long serialVersionUID = 5291624938517488437L;
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////        
    @Getter @Setter private String _oid = "prof-unknown";
    @Getter @Setter private LanguageTexts _name = null;

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param newOid
     * @param newNameES
     * @param newNameEU
     */
    public XLNetsProfile(final String newOid,final String newNameES,final String newNameEU) {
        _oid = newOid;
        _name = LanguageTextsBuilder.createMapBacked()
        							.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
		        					.addForLang(Language.SPANISH,newNameES)
		        					.addForLang(Language.BASQUE,newNameEU)
			        				.build();
    }
    
///////////////////////////////////////////////////////////////////////////////////////////
//  VALIDEZ
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Comprueba si el objeto es valido
     * @return: true si el objeto es valido y false si no es asín
     */
    public boolean isValid() {
        if (Strings.isNullOrEmpty(_oid)) return false;
        if (_name == null || _name.getFor(Language.SPANISH) == null || _name.getFor(Language.BASQUE) == null) return false;
        return true;
    }    
}
