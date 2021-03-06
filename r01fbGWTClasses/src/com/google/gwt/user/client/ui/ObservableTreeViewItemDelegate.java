package com.google.gwt.user.client.ui;

import java.util.Collection;

import lombok.RequiredArgsConstructor;
import r01f.patterns.CommandOn;
import r01f.patterns.reactive.ForUpdateObserver;
import r01f.patterns.reactive.ObservableBase;
import r01f.patterns.reactive.ObservableForUpdate;
import r01f.patterns.reactive.Observer;
import r01f.util.types.collections.CollectionUtils;
import r01f.view.LazyLoadedViewObserver;
import r01f.view.ObservableLazyLoadedView;
import r01f.view.SelectableViewComponent;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@RequiredArgsConstructor
     class ObservableTreeViewItemDelegate 
   extends ObservableBase
implements ObservableLazyLoadedView,
		   ObservableForUpdate {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final TreeViewItem _wrappedTreeViewItem;

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void notifiyObserversAboutNeedOfLazyLoadedData() {
		CollectionUtils.executeOn(this.observersOfType(LazyLoadedViewObserver.class),
								  new CommandOn<Observer>() {
											@Override
											public void executeOn(final Observer obs) {											
												LazyLoadedViewObserver viewObserver = (LazyLoadedViewObserver)obs;
												viewObserver.onLazyLoadedDataNeeded();
											}
								  });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void notifyObserversAboutUpdate() {
		CollectionUtils.executeOn(this.observersOfType(ForUpdateObserver.class),
								  new CommandOn<Observer>() {
											@Override @SuppressWarnings("unchecked")
											public void executeOn(final Observer obs) {	
												ForUpdateObserver<SelectableViewComponent> viewObserver = (ForUpdateObserver<SelectableViewComponent>)obs;
												viewObserver.onUpdate(_wrappedTreeViewItem);
											}
								  });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <O extends Observer> Collection<Observer> observersOfType(final Class<O> observerType) {
		Collection<Observer> outObservers = null;
		if (CollectionUtils.hasData(this.allObservers())) {
			outObservers = Collections2.filter(this.allObservers(),
											   new Predicate<Observer>() {
														@Override
														public boolean apply(final Observer obs) {
															// The ObservableBase implementation of observersOfType
															// needs the use of reflection, not supported by GWT
															if (observerType == LazyLoadedViewObserver.class
															 && obs instanceof LazyLoadedViewObserver) {
																return true;
															} else if (observerType == ForUpdateObserver.class
															        && obs instanceof ForUpdateObserver) {
																return true;
															}
															return false;
														}
												});
		}
		return outObservers;
	}
}
