package r01f.types;

import java.io.Serializable;
import java.util.LinkedList;


/**
 * Interface for every path types
 */
public interface IsPath 
		 extends CanBeRepresentedAsString,
		 		 Serializable {
	/**
	 * Returns the path elements as a {@link LinkedList}
	 * @return
	 */
	public LinkedList<String> getPathElements();
	/**
	 * @return the path as a relative String (does not start with /)
	 */
	public String asRelativeString();
	/**
	 * @return the path as an absolute String (starts with /)
	 */
	public String asAbsoluteString();
	/**
	 * Returns the path as a String prepending the parent path
	 * @param parentPath the parent path
	 * @return the path as a String
	 */
	public <P extends IsPath> String asStringFrom(final P parentPath);
}
