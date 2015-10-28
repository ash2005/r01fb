package r01f.guid;



/**
 * Interface to be implemented by a {@link GUIDDispenser}
 * This interface is normally EXTENDED in the implementation (see {@link SimpleGUIDDispenser})
 * or {@link HighLowGUIDDispenser}) 
 *   	static interface SimpleGUIDDispenserFactory 
 *               extends GUIDDispenserFlavourFactory {
 *   	
 *   	}
 * this way every implementation of {@link GUIDDispenser} has it's OWN creation interface
 * but with the same methods
 */
public interface GUIDDispenserFlavourFactory {
	/**
	 * Creates a dispenser from a definition
	 * @param def the dispenser definition
	 * @return the created dispenser
	 */
	public GUIDDispenser factoryFor(GUIDDispenserDef def);
}
