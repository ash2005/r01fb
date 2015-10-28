package r01f.guid;



/**
 * Interfaz que han de cumplir las clases que se encargan de la persistencia de uids
 */
public interface HighLowGUIDPersist {
    /**
     * Carga el valor de High en el objeto UID que se pasa.
     * @param   dispDef: Definicion del dispenser de guids
     * @return  El nuevo valor de high
     */
    public HighLowKey getHighKeyValue(GUIDDispenserDef dispDef);  
    /**
     * Actualiza el valor de un guid
     * @param dispDef: La definicion del guid a actualizar
     * @param highKey: El nuevo valor del high
     * @return true si se actualiza bien y false si no es asi
     */
    public boolean updateGUID(GUIDDispenserDef dispDef,HighLowKey highKey);
   
}
