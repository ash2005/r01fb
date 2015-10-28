package r01f.guid;


/**
 * Interface to be implemented by types in charge to supply GUIDs
 */
public interface GUIDDispenser {
    /**
     * Gets a guid
     * @return the generated guid or null if one cannot be supplied
     */
    public String generateGUID();
}
