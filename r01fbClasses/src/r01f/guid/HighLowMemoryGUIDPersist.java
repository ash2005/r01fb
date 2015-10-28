package r01f.guid;

import lombok.NoArgsConstructor;



/**
 * Persistencia en memoria de GUIDs
 *      Implementa el interfaz GUIDPersist almacenando en memoria el 
 *      valor de la parte high de la clave y la definicion del GUID
 */
@NoArgsConstructor
public class HighLowMemoryGUIDPersist 
  implements HighLowGUIDPersist {
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
	private GUIDDispenserDef _dispDef;		// Definición del dispenser
    private HighLowKey _highKey;           	// Valor actual de la parte high

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////        
 
    
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ UIDPersist
///////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public HighLowKey getHighKeyValue(final GUIDDispenserDef dispDef) {
        return _highKey != null ? _highKey
        						: new HighLowKey(Integer.parseInt(_dispDef.getProperty("highKeyBytes")));
    }
    @Override
    public boolean updateGUID(final GUIDDispenserDef dispDef,final HighLowKey highKey) {
        _highKey = highKey;
        return true;
    }    
}
