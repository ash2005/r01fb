package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Element;

public interface TreeViewItemsContainCapable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if the container is an item
	 */
	public boolean isItem();
	/**
	 * @return true if the container is a tree
	 */
	public boolean isTree();
	/**
	 * @return the {@link HasTreeViewItems} as a {@link TreeViewItem}
	 */
	public TreeViewItem asTreeViewItem();
	/**
	 * @return the {@link HasTreeViewItems} as a {@link TreeView}
	 */
	public TreeView asTreeView();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Element} that will contain all children
	 */
	public Element getChildContainerElement();
	/**
	 * Prepare the item to contain children, normally creating the child container element
	 */
	public void prepareToAdoptChildren();
	/**
	 * All the child items gone... normally remove the child container element
	 */
	public void allChildrenGone();
}
