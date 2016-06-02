package r01f.marshalling;

/**
 * Interface for the types that holds a model object's {@link Marshaller}
 */
public interface HasModelObjectsMarshaller {
	/**
	 * @return a model object's {@link Marshaller}
	 */
	public Marshaller getModelObjectsMarshaller();
}
