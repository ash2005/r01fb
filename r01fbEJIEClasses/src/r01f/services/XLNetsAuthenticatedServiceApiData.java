package r01f.services;

import org.w3c.dom.Document;

/**
 * API data for XLNets authenticated services
 */
public interface XLNetsAuthenticatedServiceApiData {
	/**
	 * Returns the XLNets auth token
	 * @return
	 */
	public Document getXLNetsAuthToken();
}
