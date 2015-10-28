package r01f.marshalling;

/**
 * Exception thrown by the {@link Marshaller} 
 */
public class MarshallerException 
     extends RuntimeException {
	
    private static final long serialVersionUID = -1329474484762120728L;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////    

	public MarshallerException() {
		super();
	}
	public MarshallerException(final String msg) {
		super(msg);
	}
	public MarshallerException(final Throwable otherEx) {
		super(otherEx);
	}
	public MarshallerException(final String msg,
							   final Throwable otherEx) {
		super(msg,otherEx);
	}
}
