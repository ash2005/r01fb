package com.google.gwt.user.client.ui;

import r01f.model.facets.hierarchy.HasChildren;
import r01f.view.CanBePainted;
import r01f.view.ViewObject;



public interface HasTreeViewItems<P extends TreeViewItemsContainCapable> 
	     extends HasChildren<TreeViewItem> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds an item to the item container
	 * @param item the item
	 * @return the {@link TreeView} item container to enable fluent item insertion
	 */
	public HasTreeViewItems<P> addItem(final TreeViewItem item);
	/**
	 * Adds a {@link ViewObject} to the item container
	 * @param canBePainted
	 * @return the {@link TreeView} item container to enable fluent item insertion
	 */
	public void addItem(final CanBePainted canBePainted);
	/**
	 * Inserts a child tree item at the specified index containing the specified {@link ViewObject}.
	 * @param canBePainted the paintable {@link ViewObject} to be added
	 * @param index the index where the item will be inserted
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public TreeViewItem insertItemAt(final CanBePainted canBePainted,
									 final int index);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes an item
	 * @param isItem
	 */
	public void removeItem(final TreeViewItem isItem);
}
