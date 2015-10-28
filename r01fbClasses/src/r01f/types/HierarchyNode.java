package r01f.types;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.facets.hierarchy.IsHierarchical;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

/**
 * A node in an hierarchical structure of T objects
 * @param <T>
 */
@Accessors(prefix="_")
public class HierarchyNode<T>
  implements IsHierarchical<HierarchyNode<T>>,		// has an hierarchical structure 
  			 Serializable {
	
	private static final long serialVersionUID = 6955527630723451277L;
/////////////////////////////////////////////////////////////////////////////////////////
//  DATA ASSOCIATED WITH THE NODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private T _data;
/////////////////////////////////////////////////////////////////////////////////////////
//  HIERARCHY
/////////////////////////////////////////////////////////////////////////////////////////
	private HierarchyNode<T> _parent;
	private List<HierarchyNode<T>> _children;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public HierarchyNode() {
	}
	public HierarchyNode(final T data) {
		this();
		_data = data;
	}
	public static <T> HierarchyNode<T> create(final T data) {
		return new HierarchyNode<T>(data);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void setDirectAncestor(final HierarchyNode<T> father) {
		_parent = father;
	}
	@Override
	public HierarchyNode<T> getDirectAncestor() {
		return _parent;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public HierarchyNode<T> addChild(final HierarchyNode<T> child) {
		int position = CollectionUtils.hasData(_children) ? _children.size() : 0;
		return this.insertChildAt(child,position);
	}
	@Override
	public HierarchyNode<T> insertChildAt(final HierarchyNode<T> child,final int index) {
		if (_children == null) _children = Lists.newArrayList();
		_children.add(index,child);
		return child;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void removeChild(final HierarchyNode<T> child) {
		if (_children != null) _children.remove(child);
	}
	@Override
	public void removeChildAt(final int index) {
		if (_children != null) _children.remove(index);
	}
	@Override
	public void removeAllChilds() {
		if (_children != null) _children.clear();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasChildren() {
		return CollectionUtils.hasData(_children);
	}
	@Override
	public HierarchyNode<T> getChildAt(final int index) {
		return _children != null ? _children.get(index)
								 : null;
	}
	@Override
	public int getChildCount() {
		return _children != null ? _children.size()
								 : 0;
	}
	@Override
	public int getChildIndex(final HierarchyNode<T> child) {
		return _children != null ? _children.indexOf(child)
								 : -1;
	}
	@Override
	public Collection<HierarchyNode<T>> getChildren() {
		return _children;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public List<HierarchyNode<T>> getSiblings() {
		if (_parent == null) return null;
		Collection<HierarchyNode<T>> allParentChilds = _parent.getChildren();
		return Lists.getSiblings((List<HierarchyNode<T>>)allParentChilds,
							     this);
	}
	@Override
	public List<HierarchyNode<T>> getSiblingsBefore() {
		if (_parent == null) return null;
		Collection<HierarchyNode<T>> allParentChilds = _parent.getChildren();
		return Lists.getSiblingsBefore((List<HierarchyNode<T>>)allParentChilds,
									   this);
	}
	@Override
	public HierarchyNode<T> getPrevSibling() {
		if (_parent == null) return null;
		Collection<HierarchyNode<T>> allParentChilds = _parent.getChildren();
		return Lists.getPrevSibling((List<HierarchyNode<T>>)allParentChilds,
									this);
	}
	@Override
	public List<HierarchyNode<T>> getSiblingsAfter() {
		if (_parent == null) return null;
		Collection<HierarchyNode<T>> allParentChilds = _parent.getChildren();
		return Lists.getSiblingsAfter((List<HierarchyNode<T>>)allParentChilds,
									  this);
	}
	@Override
	public HierarchyNode<T> getNextSibling() {
		if (_parent == null) return null;
		Collection<HierarchyNode<T>> allParentChilds = _parent.getChildren();
		return Lists.getNextSibling((List<HierarchyNode<T>>)allParentChilds,
								 	this);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public HierarchyNode<T> branchRoot() {
		HierarchyNode<T> currNode = this;
		HierarchyNode<T> currNodeParent = this.getDirectAncestor();
		while (currNodeParent != null) {
			currNode = currNodeParent;
			currNodeParent = currNode.getDirectAncestor();
		}
		return currNode;
	}
	@Override
	public boolean isDescendantOf(final HierarchyNode<T> ancestor) {
		boolean outIsDescendant = false;
		HierarchyNode<T> currNodeParent = this.getDirectAncestor();
		while (currNodeParent != null) {
			if (currNodeParent == ancestor) {
				outIsDescendant = true;
				break;
			}
			currNodeParent = currNodeParent.getDirectAncestor();
		}
		return outIsDescendant;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean containsChildWithData(final T data) {
		return this.getDescendantWithData(data,
								   		  this.getChildren(),
								   		  false) != null;		// not recursive 
	}
	public boolean containsDescendantWithData(final T data) {
		return this.getDescendantWithData(data,
								   		  this.getChildren(),
								   		  true) != null;		// recursive
	}
	public HierarchyNode<T> getChildWithData(final T data) {
		return this.getDescendantWithData(data,
								   		  this.getChildren(),
								   		  false);				// not recursive
	}
	public HierarchyNode<T> getDescendantWithData(final T data,
									    		  final Collection<HierarchyNode<T>> nodes,
									    		  final boolean recurse) {
		HierarchyNode<T> outNode = null;
		if (CollectionUtils.hasData(nodes)) {
			for (HierarchyNode<T> node : nodes) {
				if (node.getData().equals(data)) {
					outNode = node;
					break;
				}
				if (recurse) {
					outNode = this.getDescendantWithData(data,
										   	      		 node.getChildren(),
										   	      		 true);
					if (outNode != null) break;
				}
			}
		}
		return outNode;
	}
}
