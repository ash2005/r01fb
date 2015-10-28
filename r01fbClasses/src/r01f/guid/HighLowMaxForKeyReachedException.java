package r01f.guid;


  class HighLowMaxForKeyReachedException
extends Exception {
	
    private static final long serialVersionUID = -1396750553384529281L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    public HighLowMaxForKeyReachedException() {
        super();
    }
    public HighLowMaxForKeyReachedException(Exception cause) {
        super(cause);
    }
    public HighLowMaxForKeyReachedException(String msg) {
        super(msg);
    }
    public HighLowMaxForKeyReachedException(String msg, Exception cause) {
        super(msg, cause);
    }
}
