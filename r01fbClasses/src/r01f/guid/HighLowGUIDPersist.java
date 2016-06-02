package r01f.guid;



/**
 * Interface to be implemented by types that persist the guid generator state
 */
public interface HighLowGUIDPersist {
    /**
     * Returns the high value
     * @param   dispDef
     * @return  
     */
    public HighLowKey getHighKeyValue(GUIDDispenserDef dispDef);  
    /**
     * Updates the high value
     * @param dispDef
     * @param highKey
     * @return true if the high value was updated, false otherwise
     */
    public boolean updateGUID(GUIDDispenserDef dispDef,HighLowKey highKey);
   
}
