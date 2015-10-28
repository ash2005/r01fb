package com.google.gwt.user.client.ui;

import lombok.RequiredArgsConstructor;
import r01f.model.ModelObjectSummary;
import r01f.view.CanBePainted;
import r01f.view.CanPaint;
import r01f.view.ViewComponent;
import r01f.view.ViewObject;

/**
 * A {@link ModelObjectSummary} wrapper to make it paintable (transform it to a {@link CanBePainted} {@link ViewObject})
 */
@RequiredArgsConstructor
public class CanBePaintedModelObjectSummary
  implements CanBePainted,
  			 r01f.model.facets.view.HasCaption {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object's summary
	 */
	private final ModelObjectSummary _modelObjectSummary;
	/**
	 * The {@link ViewComponent} where this {@link ViewObject} is painted
	 */
	private CanPaint _viewComponent;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void paintedInto(final CanPaint painter) {
		_viewComponent = painter;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getDataToStoreAtViewComponent() {
		return (T) _modelObjectSummary.getOid();		// store the oid at the view component
	}
	@Override @SuppressWarnings("unchecked")
	public <V extends CanPaint> V getViewComponentWhereItsPainted() {
		return (V)_viewComponent;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getCaption() {
		return _modelObjectSummary.getSummary()
								  .asLangIndependent()
								  .asString();
	}

}
