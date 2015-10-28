package r01f.types.weburl;

import java.io.Serializable;

/**
 * Represents a resource url path
 */
public interface WebUrlQueryStringParam 
		 extends Serializable {
	/**
	 * @return true if the param contains data
	 */
	public boolean hasData();
	/**
	 * @return the param name
	 */
	public String getName();
	/**
	 * @return the param value
	 */
	public String getValue();
	/**
	 * @return the param value
	 */
	public String valueAsString();
	/**
	 * @return the param value url encoded
	 */
	public String valueAsStringUrlEncoded();
	/**
	 * The param as name=value with the value encoded
	 * @return
	 */
	public String asStringUrlEncoded();
	/**
	 * The param as name=value
	 * @return
	 */
	public String asString();
	/**
	 * The param as name=value with the value encoded as specified by the param
	 * @param encodeValues
	 * @return
	 */
	public String asString(boolean encodeValues);
}
