package r01f.persistence;

import r01f.httpclient.HttpResponse.HttpResponseCode;

/**
 * A performed persistence-related operation
 * Note that the performed operation is NOT always the same as the requested one
 * (ie: an update could be requested by the client BUT the record didn't exist so a creation is performed)
 */
public enum PersistencePerformedOperation {
	LOADED,
	CREATED,
	UPDATED,
	DELETED,
	NOT_MODIFIED,
	FOUND;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Guess the supposed performed operation based on the requested one
	 * Usually the performed operation depends on the requested one, BUT some times, it 
	 * depends on the server data, ie, if the client request a CREATION but the entity
	 * already exists at the server, the performed operation can be UPDATED instead of the
	 * supposed one (CREATED)
	 * @param requestedOp
	 * @return
	 */
	public static PersistencePerformedOperation from(final PersistenceRequestedOperation requestedOp) {
		PersistencePerformedOperation outPerformedOp = null;
		if (requestedOp == PersistenceRequestedOperation.LOAD) {
			outPerformedOp = LOADED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.CREATE) {
			outPerformedOp = CREATED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.UPDATE) {
			outPerformedOp = UPDATED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.DELETE) {
			outPerformedOp = DELETED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.FIND) {
			outPerformedOp = FOUND;
		}
		else {
			throw new IllegalArgumentException("Illegal combination of PersistenceRequestedOperation and HttpResponseCode. This is a DEVELOPER mistake!");
		}
		return outPerformedOp;
 
	}
	/**
	 * Guess the performed operation based on the requested operation and the HTTP response code
	 * @param requestedOp
	 * @param httpResponseCode
	 * @return
	 */
	public static PersistencePerformedOperation from(final PersistenceRequestedOperation requestedOp,
													 final HttpResponseCode httpResponseCode) {
		PersistencePerformedOperation outPerformedOp = null;
		if (requestedOp == PersistenceRequestedOperation.LOAD) {
			outPerformedOp = LOADED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.CREATE) {
			outPerformedOp = CREATED;
			
		} 
		else if (requestedOp == PersistenceRequestedOperation.UPDATE) {
			if (httpResponseCode == HttpResponseCode.NOT_MODIFIED) {
				outPerformedOp = NOT_MODIFIED;
			} else {
				outPerformedOp = UPDATED;
			}
		} 
		else if (requestedOp == PersistenceRequestedOperation.DELETE) {
			outPerformedOp = DELETED;
		} 
		else if (requestedOp == PersistenceRequestedOperation.FIND) {
			outPerformedOp = FOUND;
		} 
		else {
			throw new IllegalArgumentException("Illegal combination of PersistenceRequestedOperation and HttpResponseCode. This is a DEVELOPER mistake!");
		}
		return outPerformedOp;
	}
}