package r01f.guid;

import lombok.NoArgsConstructor;



/**
 * GUIDs memory persistence
 * Implements {@link HighLowGUIDPersist} interface storing at memory the high part of  
 * the key
 */
@NoArgsConstructor
public class HighLowMemoryGUIDPersist 
  implements HighLowGUIDPersist {
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
	private GUIDDispenserDef _dispDef;		// dispenser definition
    private HighLowKey _highKey;           	// guid actual high key part 

///////////////////////////////////////////////////////////////////////////////////////////
//  UIDPersist
///////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public HighLowKey getHighKeyValue(final GUIDDispenserDef dispDef) {
        return _highKey != null ? _highKey
        						: new HighLowKey(Integer.parseInt(_dispDef.getProperty("highKeyBytes")));
    }
    @Override
    public boolean updateGUID(final GUIDDispenserDef dispDef,
    						  final HighLowKey highKey) {
        _highKey = highKey;
        return true;
    }    
}
