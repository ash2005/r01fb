package r01f.model.facets.hierarchy;


/**
 * Model an hierarchical object
 * @param <T> the type
 */
public interface IsHierarchical<T> 
		 extends HasSingleParent<T>,
		 		 HasChildren<T>,
		 		 HasSiblings<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return the branch root object
     */
    public T branchRoot();
    /**
     * Returns true if this object is descendant of the provided ancestor
     * @param ancestor the ancestor
     * @return
     */
    public boolean isDescendantOf(T ancestor);
}
